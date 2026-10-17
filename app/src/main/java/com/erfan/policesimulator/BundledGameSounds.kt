package com.erfan.policesimulator

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import java.util.concurrent.ConcurrentHashMap

/**
 * Lightweight one-shot playback for bundled CC0 OGG assets.
 * SoundPool decoding is asynchronous; requests are queued until a sample is loaded.
 */
internal class BundledGameSounds(context: Context) {
    private val pool = SoundPool.Builder()
        .setMaxStreams(6)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val sounds = ConcurrentHashMap<String, Int>()
    private val loaded = ConcurrentHashMap<Int, Boolean>()
    private val pending = ConcurrentHashMap<Int, Int>()
    @Volatile private var released = false

    init {
        pool.setOnLoadCompleteListener { _, sampleId, status ->
            loaded[sampleId] = status == 0
            if (status == 0) {
                val count = pending.remove(sampleId) ?: 0
                repeat(count.coerceAtMost(3)) { pool.play(sampleId, 0.7f, 0.7f, 1, 0, 1f) }
            } else pending.remove(sampleId)
        }
        load(context, "footstep", "audio/kenney/footstep_concrete_000.ogg")
        load(context, "metal", "audio/kenney/impactMetal_medium_000.ogg")
        load(context, "glass", "audio/kenney/impactGlass_light_000.ogg")
        load(context, "confirm", "audio/kenney/confirmation_001.ogg")
        load(context, "select", "audio/kenney/select_001.ogg")
        load(context, "error", "audio/kenney/error_008.ogg")
    }

    private fun load(context: Context, key: String, path: String) {
        try {
            context.assets.openFd(path).use { descriptor ->
                val id = pool.load(descriptor, 1)
                sounds[key] = id
            }
        } catch (_: Exception) {
            // Missing/corrupt optional sample must never prevent the game from starting.
        }
    }

    fun play(key: String, volume: Float = 0.72f) {
        if (released) return
        val id = sounds[key] ?: return
        val level = volume.coerceIn(0f, 1f)
        if (loaded[id] == true) pool.play(id, level, level, 1, 0, 1f)
        else if (loaded[id] == null) pending.compute(id) { _, old -> ((old ?: 0) + 1).coerceAtMost(3) }
    }

    fun release() {
        if (released) return
        released = true
        pending.clear()
        sounds.clear()
        loaded.clear()
        pool.release()
    }
}
