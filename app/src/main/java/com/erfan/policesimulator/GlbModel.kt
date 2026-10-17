package com.erfan.policesimulator

import android.content.res.AssetManager
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.charset.Charset

/**
 * Small glTF 2.0 binary (GLB) reader for the custom OpenGL ES 2.0 renderer.
 *
 * It deliberately supports static triangle meshes only; no game engine or native library is used.
 * POSITION, TEXCOORD_0, indexed triangles, material baseColorFactor, and node TRS/matrices are
 * handled. External image URIs are loaded separately by the renderer so missing optional art can
 * never prevent the procedural fallback scene from starting.
 */
internal data class GlbPrimitive(
    val vertices: FloatBuffer,
    val vertexCount: Int,
    val tint: FloatArray
)

internal data class GlbModel(val primitives: List<GlbPrimitive>)

internal data class GlbBinaryDocument(val json: String, val binary: ByteArray)

internal object GlbBinaryContainer {
    internal const val MAGIC = 0x46546C67
    private const val JSON_CHUNK = 0x4E4F534A
    private const val BIN_CHUNK = 0x004E4942
    private const val HEADER_SIZE = 12
    private const val CHUNK_HEADER_SIZE = 8

    fun parse(bytes: ByteArray): GlbBinaryDocument {
        require(bytes.size >= HEADER_SIZE + CHUNK_HEADER_SIZE) { "GLB header is truncated" }
        val input = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        require(input.getInt(0) == MAGIC) { "Invalid GLB magic" }
        require(input.getInt(4) == 2) { "Only glTF 2.0 GLB files are supported" }
        val declaredLength = input.getInt(8).toLong() and 0xffffffffL
        require(declaredLength in (HEADER_SIZE + CHUNK_HEADER_SIZE).toLong()..bytes.size.toLong()) {
            "Invalid GLB declared length"
        }

        var cursor = HEADER_SIZE
        var json: String? = null
        var binary = ByteArray(0)
        while (cursor < declaredLength.toInt()) {
            require(cursor + CHUNK_HEADER_SIZE <= declaredLength.toInt()) { "Truncated GLB chunk header" }
            val rawLength = input.getInt(cursor).toLong() and 0xffffffffL
            val type = input.getInt(cursor + 4)
            require(rawLength <= declaredLength - cursor - CHUNK_HEADER_SIZE) { "GLB chunk exceeds file bounds" }
            val length = rawLength.toInt()
            val start = cursor + CHUNK_HEADER_SIZE
            when (type) {
                JSON_CHUNK -> {
                    require(json == null) { "GLB contains multiple JSON chunks" }
                    json = String(bytes, start, length, Charsets.UTF_8)
                        .trimEnd(' ', '\u0000', '\r', '\n')
                }
                BIN_CHUNK -> {
                    require(binary.isEmpty()) { "GLB contains multiple binary chunks" }
                    binary = bytes.copyOfRange(start, start + length)
                }
            }
            cursor = start + length
        }
        require(cursor == declaredLength.toInt()) { "GLB chunk alignment is invalid" }
        require(!json.isNullOrBlank()) { "GLB JSON chunk is missing" }
        return GlbBinaryDocument(json!!, binary)
    }
}

internal object GlbModelLoader {
    private const val COMPONENT_FLOAT = 5126
    private const val COMPONENT_UNSIGNED_BYTE = 5121
    private const val COMPONENT_UNSIGNED_SHORT = 5123
    private const val COMPONENT_UNSIGNED_INT = 5125
    private const val MODE_TRIANGLES = 4
    private val UTF8: Charset = Charsets.UTF_8

    private data class RawPrimitive(
        val positions: FloatArray,
        val texCoords: FloatArray,
        val indices: IntArray,
        val tint: FloatArray
    )

    fun load(assets: AssetManager, assetPath: String): GlbModel {
        val bytes = assets.open(assetPath).use { it.readBytes() }
        val document = GlbBinaryContainer.parse(bytes)
        val root = JSONObject(document.json)
        val accessors = root.optJSONArray("accessors")
            ?: error("GLB has no accessors: $assetPath")
        val bufferViews = root.optJSONArray("bufferViews")
            ?: error("GLB has no bufferViews: $assetPath")
        val meshes = root.optJSONArray("meshes")
            ?: error("GLB has no meshes: $assetPath")
        require(document.binary.isNotEmpty()) { "GLB binary chunk is missing: $assetPath" }

        val bufferList = root.optJSONArray("buffers")
        if (bufferList != null && bufferList.length() > 0) {
            val requiredBytes = bufferList.getJSONObject(0).optInt("byteLength", 0)
            require(requiredBytes in 1..document.binary.size) {
                "GLB binary buffer length is invalid: $assetPath"
            }
        }

        val rawPrimitives = ArrayList<RawPrimitive>()
        var minX = Float.POSITIVE_INFINITY
        var minY = Float.POSITIVE_INFINITY
        var minZ = Float.POSITIVE_INFINITY
        var maxX = Float.NEGATIVE_INFINITY
        var maxY = Float.NEGATIVE_INFINITY
        var maxZ = Float.NEGATIVE_INFINITY

        fun appendMesh(meshIndex: Int, worldMatrix: FloatArray) {
            require(meshIndex in 0 until meshes.length()) { "GLB node references an invalid mesh" }
            val mesh = meshes.getJSONObject(meshIndex)
            val primitives = mesh.optJSONArray("primitives") ?: return
            for (primitiveIndex in 0 until primitives.length()) {
                val primitive = primitives.getJSONObject(primitiveIndex)
                if (primitive.optInt("mode", MODE_TRIANGLES) != MODE_TRIANGLES) continue
                val attributes = primitive.optJSONObject("attributes") ?: continue
                val positionAccessor = attributes.optInt("POSITION", -1)
                if (positionAccessor < 0) continue
                val sourcePositions = readFloatAccessor(
                    positionAccessor, 3, accessors, bufferViews, document.binary
                )
                val vertexTotal = sourcePositions.size / 3
                if (vertexTotal == 0) continue
                val uvAccessor = attributes.optInt("TEXCOORD_0", -1)
                val sourceUvs = if (uvAccessor >= 0) {
                    readFloatAccessor(uvAccessor, 2, accessors, bufferViews, document.binary)
                } else {
                    FloatArray(vertexTotal * 2)
                }
                require(sourceUvs.size / 2 == vertexTotal) { "GLB UV count does not match POSITION count" }
                val indicesAccessor = primitive.optInt("indices", -1)
                val indices = if (indicesAccessor >= 0) {
                    readIndexAccessor(indicesAccessor, accessors, bufferViews, document.binary)
                } else {
                    IntArray(vertexTotal) { it }
                }
                if (indices.isEmpty() || indices.size % 3 != 0) continue
                require(indices.all { it in 0 until vertexTotal }) { "GLB index is outside the vertex range" }

                val transformed = FloatArray(sourcePositions.size)
                var vertex = 0
                while (vertex < vertexTotal) {
                    val px = sourcePositions[vertex * 3]
                    val py = sourcePositions[vertex * 3 + 1]
                    val pz = sourcePositions[vertex * 3 + 2]
                    val tx = worldMatrix[0] * px + worldMatrix[4] * py + worldMatrix[8] * pz + worldMatrix[12]
                    val ty = worldMatrix[1] * px + worldMatrix[5] * py + worldMatrix[9] * pz + worldMatrix[13]
                    val tz = worldMatrix[2] * px + worldMatrix[6] * py + worldMatrix[10] * pz + worldMatrix[14]
                    transformed[vertex * 3] = tx
                    transformed[vertex * 3 + 1] = ty
                    transformed[vertex * 3 + 2] = tz
                    minX = minOf(minX, tx); maxX = maxOf(maxX, tx)
                    minY = minOf(minY, ty); maxY = maxOf(maxY, ty)
                    minZ = minOf(minZ, tz); maxZ = maxOf(maxZ, tz)
                    vertex++
                }
                rawPrimitives += RawPrimitive(
                    positions = transformed,
                    texCoords = sourceUvs,
                    indices = indices,
                    tint = materialTint(primitive.optInt("material", -1), root)
                )
            }
        }

        val nodes = root.optJSONArray("nodes")
        if (nodes != null && nodes.length() > 0) {
            val sceneIndex = root.optInt("scene", 0)
            val scenes = root.optJSONArray("scenes")
            val activeScene = if (scenes != null && sceneIndex in 0 until scenes.length()) {
                scenes.getJSONObject(sceneIndex)
            } else null
            val roots = activeScene?.optJSONArray("nodes")
            val visited = HashSet<Int>()

            fun visit(nodeIndex: Int, parentMatrix: FloatArray, depth: Int) {
                require(depth <= 64) { "GLB node hierarchy is too deep" }
                require(nodeIndex in 0 until nodes.length()) { "GLB scene references an invalid node" }
                if (!visited.add(nodeIndex)) return
                val node = nodes.getJSONObject(nodeIndex)
                val worldMatrix = multiplyMatrices(parentMatrix, nodeMatrix(node))
                val meshIndex = node.optInt("mesh", -1)
                if (meshIndex >= 0) appendMesh(meshIndex, worldMatrix)
                val children = node.optJSONArray("children")
                if (children != null) {
                    for (childIndex in 0 until children.length()) {
                        visit(children.getInt(childIndex), worldMatrix, depth + 1)
                    }
                }
            }

            val identity = identityMatrix()
            if (roots != null) {
                for (i in 0 until roots.length()) visit(roots.getInt(i), identity, 0)
            } else {
                // A few export tools omit the scenes array; keep such static files usable.
                for (i in 0 until nodes.length()) {
                    val node = nodes.getJSONObject(i)
                    if (node.optInt("mesh", -1) >= 0) visit(i, identity, 0)
                }
            }
        } else {
            // Fallback for minimal GLBs without nodes: meshes are already in model space.
            for (meshIndex in 0 until meshes.length()) appendMesh(meshIndex, identityMatrix())
        }

        require(rawPrimitives.isNotEmpty()) { "GLB contains no supported triangle meshes: $assetPath" }
        val spanX = (maxX - minX).coerceAtLeast(0.001f)
        val spanY = (maxY - minY).coerceAtLeast(0.001f)
        val spanZ = (maxZ - minZ).coerceAtLeast(0.001f)
        val centerX = (minX + maxX) * 0.5f
        val centerZ = (minZ + maxZ) * 0.5f

        val parts = rawPrimitives.map { raw ->
            val output = FloatArray(raw.indices.size * 5)
            var target = 0
            for (index in raw.indices) {
                val source = index * 3
                output[target++] = (raw.positions[source] - centerX) / spanX
                output[target++] = (raw.positions[source + 1] - minY) / spanY
                // Kenney car meshes face +Z; the patrol simulator's vehicle convention faces -Z.
                output[target++] = -(raw.positions[source + 2] - centerZ) / spanZ
                output[target++] = raw.texCoords[index * 2]
                output[target++] = 1f - raw.texCoords[index * 2 + 1]
            }
            val buffer = ByteBuffer.allocateDirect(output.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
            buffer.put(output).position(0)
            GlbPrimitive(buffer, raw.indices.size, raw.tint)
        }
        return GlbModel(parts)
    }

    private fun readFloatAccessor(
        accessorIndex: Int,
        componentCount: Int,
        accessors: JSONArray,
        views: JSONArray,
        binary: ByteArray
    ): FloatArray {
        require(accessorIndex in 0 until accessors.length()) { "Invalid GLB float accessor" }
        val accessor = accessors.getJSONObject(accessorIndex)
        require(accessor.optInt("componentType", -1) == COMPONENT_FLOAT) {
            "Only FLOAT attributes are supported in static GLB meshes"
        }
        val type = accessor.optString("type", "")
        val expectedType = when (componentCount) { 2 -> "VEC2"; 3 -> "VEC3"; 4 -> "VEC4"; else -> "" }
        require(type == expectedType) { "Unexpected GLB attribute type: $type" }
        val viewIndex = accessor.optInt("bufferView", -1)
        require(viewIndex in 0 until views.length()) { "GLB accessor has no bufferView" }
        val view = views.getJSONObject(viewIndex)
        val count = accessor.optInt("count", 0)
        require(count > 0) { "GLB attribute accessor is empty" }
        val accessorOffset = accessor.optInt("byteOffset", 0)
        val viewOffset = view.optInt("byteOffset", 0)
        val viewLength = view.optInt("byteLength", 0)
        val packedStride = componentCount * 4
        var stride = view.optInt("byteStride", packedStride)
        // Some Kenney UnityGLTF exports declare a global 12-byte stride for packed UVs.
        // If that stride would run past the view, recover the actual packed stride safely.
        if (accessorOffset.toLong() + (count - 1L) * stride + packedStride > viewLength.toLong()) {
            stride = packedStride
        }
        require(stride >= packedStride && accessorOffset >= 0 && viewOffset >= 0) {
            "Invalid GLB attribute stride or offset"
        }
        require(accessorOffset.toLong() + (count - 1L) * stride + packedStride <= viewLength.toLong()) {
            "GLB attribute accessor exceeds its bufferView"
        }
        require(viewOffset.toLong() + viewLength <= binary.size.toLong()) { "GLB bufferView exceeds binary data" }
        val input = ByteBuffer.wrap(binary).order(ByteOrder.LITTLE_ENDIAN)
        val out = FloatArray(count * componentCount)
        var destination = 0
        for (i in 0 until count) {
            val start = viewOffset + accessorOffset + i * stride
            for (component in 0 until componentCount) {
                out[destination++] = input.getFloat(start + component * 4)
            }
        }
        return out
    }

    private fun readIndexAccessor(
        accessorIndex: Int,
        accessors: JSONArray,
        views: JSONArray,
        binary: ByteArray
    ): IntArray {
        require(accessorIndex in 0 until accessors.length()) { "Invalid GLB index accessor" }
        val accessor = accessors.getJSONObject(accessorIndex)
        require(accessor.optString("type", "") == "SCALAR") { "GLB indices must be SCALAR" }
        val componentType = accessor.optInt("componentType", -1)
        val componentSize = when (componentType) {
            COMPONENT_UNSIGNED_BYTE -> 1
            COMPONENT_UNSIGNED_SHORT -> 2
            COMPONENT_UNSIGNED_INT -> 4
            else -> error("Unsupported GLB index component type: $componentType")
        }
        val viewIndex = accessor.optInt("bufferView", -1)
        require(viewIndex in 0 until views.length()) { "GLB index accessor has no bufferView" }
        val view = views.getJSONObject(viewIndex)
        val count = accessor.optInt("count", 0)
        require(count > 0) { "GLB index accessor is empty" }
        val accessorOffset = accessor.optInt("byteOffset", 0)
        val viewOffset = view.optInt("byteOffset", 0)
        val viewLength = view.optInt("byteLength", 0)
        var stride = view.optInt("byteStride", componentSize)
        if (accessorOffset.toLong() + (count - 1L) * stride + componentSize > viewLength.toLong()) {
            stride = componentSize
        }
        require(stride >= componentSize && accessorOffset >= 0 && viewOffset >= 0) {
            "Invalid GLB index stride or offset"
        }
        require(accessorOffset.toLong() + (count - 1L) * stride + componentSize <= viewLength.toLong()) {
            "GLB indices exceed their bufferView"
        }
        require(viewOffset.toLong() + viewLength <= binary.size.toLong()) { "GLB index bufferView exceeds binary data" }
        val input = ByteBuffer.wrap(binary).order(ByteOrder.LITTLE_ENDIAN)
        return IntArray(count) { i ->
            val offset = viewOffset + accessorOffset + i * stride
            when (componentType) {
                COMPONENT_UNSIGNED_BYTE -> input.get(offset).toInt() and 0xff
                COMPONENT_UNSIGNED_SHORT -> input.getShort(offset).toInt() and 0xffff
                else -> {
                    val value = input.getInt(offset)
                    require(value >= 0) { "GLB index exceeds supported integer range" }
                    value
                }
            }
        }
    }

    private fun materialTint(materialIndex: Int, root: JSONObject): FloatArray {
        val materials = root.optJSONArray("materials")
        if (materials == null || materialIndex !in 0 until materials.length()) {
            return floatArrayOf(1f, 1f, 1f)
        }
        val pbr = materials.getJSONObject(materialIndex).optJSONObject("pbrMetallicRoughness")
        val factor = pbr?.optJSONArray("baseColorFactor") ?: return floatArrayOf(1f, 1f, 1f)
        return floatArrayOf(
            factor.optDouble(0, 1.0).toFloat().coerceIn(0f, 1f),
            factor.optDouble(1, 1.0).toFloat().coerceIn(0f, 1f),
            factor.optDouble(2, 1.0).toFloat().coerceIn(0f, 1f)
        )
    }

    private fun nodeMatrix(node: JSONObject): FloatArray {
        val sourceMatrix = node.optJSONArray("matrix")
        if (sourceMatrix != null && sourceMatrix.length() == 16) {
            return FloatArray(16) { sourceMatrix.optDouble(it, 0.0).toFloat() }
        }
        val translation = node.optJSONArray("translation")
        val scale = node.optJSONArray("scale")
        val rotation = node.optJSONArray("rotation")
        val tx = translation.floatAt(0, 0f); val ty = translation.floatAt(1, 0f); val tz = translation.floatAt(2, 0f)
        val sx = scale.floatAt(0, 1f); val sy = scale.floatAt(1, 1f); val sz = scale.floatAt(2, 1f)
        val qx = rotation.floatAt(0, 0f); val qy = rotation.floatAt(1, 0f)
        val qz = rotation.floatAt(2, 0f); val qw = rotation.floatAt(3, 1f)
        val xx = qx * qx; val yy = qy * qy; val zz = qz * qz
        val xy = qx * qy; val xz = qx * qz; val yz = qy * qz
        val wx = qw * qx; val wy = qw * qy; val wz = qw * qz
        return floatArrayOf(
            (1f - 2f * (yy + zz)) * sx, (2f * (xy + wz)) * sx, (2f * (xz - wy)) * sx, 0f,
            (2f * (xy - wz)) * sy, (1f - 2f * (xx + zz)) * sy, (2f * (yz + wx)) * sy, 0f,
            (2f * (xz + wy)) * sz, (2f * (yz - wx)) * sz, (1f - 2f * (xx + yy)) * sz, 0f,
            tx, ty, tz, 1f
        )
    }

    private fun JSONArray?.floatAt(index: Int, fallback: Float): Float =
        this?.optDouble(index, fallback.toDouble())?.toFloat() ?: fallback

    private fun identityMatrix() = floatArrayOf(
        1f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f,
        0f, 0f, 1f, 0f,
        0f, 0f, 0f, 1f
    )

    /** Column-major 4x4 matrix product, matching glTF and OpenGL's matrix convention. */
    private fun multiplyMatrices(left: FloatArray, right: FloatArray): FloatArray {
        val out = FloatArray(16)
        for (column in 0..3) {
            for (row in 0..3) {
                var value = 0f
                for (k in 0..3) value += left[k * 4 + row] * right[column * 4 + k]
                out[column * 4 + row] = value
            }
        }
        return out
    }
}
