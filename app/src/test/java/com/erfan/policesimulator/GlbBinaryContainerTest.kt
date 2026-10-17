package com.erfan.policesimulator

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GlbBinaryContainerTest {
    @Test
    fun parsesJsonAndBinaryChunks() {
        val json = """{"asset":{"version":"2.0"},"scene":0}"""
        val binary = byteArrayOf(10, 20, 30, 0)
        val bytes = createGlb(json, binary)

        val result = GlbBinaryContainer.parse(bytes)

        assertTrue(result.json.contains("\"version\":\"2.0\""))
        assertArrayEquals(binary, result.binary)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsWrongMagic() {
        val bytes = createGlb("""{"asset":{"version":"2.0"}}""", byteArrayOf(1, 2, 3, 4))
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).putInt(0, 0)
        GlbBinaryContainer.parse(bytes)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsChunkThatRunsPastDeclaredLength() {
        val bytes = createGlb("""{"asset":{"version":"2.0"}}""", byteArrayOf(1, 2, 3, 4))
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).putInt(12, Int.MAX_VALUE)
        GlbBinaryContainer.parse(bytes)
    }

    private fun createGlb(json: String, binary: ByteArray): ByteArray {
        val jsonBytes = json.toByteArray(StandardCharsets.UTF_8)
        val jsonLength = (jsonBytes.size + 3) / 4 * 4
        val binaryLength = (binary.size + 3) / 4 * 4
        val totalLength = 12 + 8 + jsonLength + 8 + binaryLength
        val buffer = ByteBuffer.allocate(totalLength).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(GlbBinaryContainer.MAGIC)
        buffer.putInt(2)
        buffer.putInt(totalLength)
        buffer.putInt(jsonLength)
        buffer.putInt(0x4E4F534A)
        buffer.put(jsonBytes)
        repeat(jsonLength - jsonBytes.size) { buffer.put(0x20.toByte()) }
        buffer.putInt(binaryLength)
        buffer.putInt(0x004E4942)
        buffer.put(binary)
        repeat(binaryLength - binary.size) { buffer.put(0) }
        return buffer.array()
    }
}
