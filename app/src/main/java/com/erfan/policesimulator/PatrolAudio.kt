package com.erfan.policesimulator

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * A small native patrol soundscape. It synthesizes the siren, engine hum, footstep ticks,
 * and impact thump at runtime; no third-party recordings or media decoding are required.
 *
 * AudioTrack lives on a dedicated worker thread and is muted/paused with the Activity lifecycle.
 */
internal class PatrolAudio {
    private val sampleRate = 22_050
    private val framesPerChunk = 1_024

    @Volatile private var active = false
    @Volatile private var released = false
    @Volatile private var siren = false
    @Volatile private var speed = 0f
    @Volatile private var walking = false
    @Volatile private var impactSerial = 0
    @Volatile private var track: AudioTrack? = null
    @Volatile private var worker: Thread? = null

    @Synchronized
    fun setActive(value: Boolean) {
        if (released) return
        active = value
        if (value && worker?.isAlive != true) {
            worker = Thread(::audioLoop, "PoliceSimulator-Audio").apply {
                priority = Thread.NORM_PRIORITY
                isDaemon = true
                start()
            }
        } else if (!value) {
            try { track?.pause() } catch (_: IllegalStateException) { }
        }
    }

    fun update(vehicleSpeed: Float, sirenEnabled: Boolean, walkingNow: Boolean) {
        speed = abs(vehicleSpeed).coerceIn(0f, 30f)
        siren = sirenEnabled
        walking = walkingNow
    }

    fun playImpact() {
        impactSerial++
    }

    @Synchronized
    fun release() {
        released = true
        active = false
        worker?.interrupt()
        try { track?.pause() } catch (_: IllegalStateException) { }
        try { track?.release() } catch (_: IllegalStateException) { }
        track = null
        worker = null
    }

    private fun audioLoop() {
        var localTrack: AudioTrack? = null
        try {
            val format = AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            val minBytes = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(framesPerChunk * 4)
            localTrack = AudioTrack.Builder()
                .setAudioAttributes(attributes)
                .setAudioFormat(format)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .setBufferSizeInBytes(max(minBytes, framesPerChunk * 4))
                .build()
            if (localTrack.state != AudioTrack.STATE_INITIALIZED) return
            localTrack.setVolume(0.58f)
            track = localTrack

            val pcm = ShortArray(framesPerChunk)
            var frame = 0L
            var sirenPhase = 0.0
            var enginePhase = 0.0
            var footPhase = 0.0
            var lastImpactSerial = impactSerial
            var impactFramesLeft = 0
            var noiseState = 0x13579BDF

            while (!released && !Thread.currentThread().isInterrupted) {
                if (!active) {
                    if (localTrack.playState == AudioTrack.PLAYSTATE_PLAYING) {
                        localTrack.pause()
                        localTrack.flush()
                    }
                    Thread.sleep(40L)
                    continue
                }
                if (localTrack.playState != AudioTrack.PLAYSTATE_PLAYING) localTrack.play()

                val sirenNow = siren
                val speedNow = speed
                val walkingNow = walking
                val impactNow = impactSerial
                if (impactNow != lastImpactSerial) {
                    lastImpactSerial = impactNow
                    impactFramesLeft = sampleRate / 7
                }

                for (i in pcm.indices) {
                    var sample = 0.0
                    val time = frame.toDouble() / sampleRate
                    if (sirenNow) {
                        val sweep = 0.5 - 0.5 * kotlin.math.cos(2.0 * PI * time / 1.35)
                        val frequency = 610.0 + 570.0 * sweep
                        sirenPhase += 2.0 * PI * frequency / sampleRate
                        sample += 0.19 * sin(sirenPhase) + 0.055 * sin(sirenPhase * 2.0)
                    }
                    if (speedNow > 0.65f) {
                        val throttle = min(1.0, speedNow / 18.0).toDouble()
                        val frequency = 48.0 + speedNow * 3.2
                        enginePhase += 2.0 * PI * frequency / sampleRate
                        sample += (0.025 + throttle * 0.065) * (sin(enginePhase) + 0.28 * sin(enginePhase * 2.0))
                        sample += 0.009 * throttle * sin(enginePhase * 0.47)
                    }
                    if (walkingNow) {
                        footPhase += 1.0 / sampleRate
                        val phase = footPhase % 0.42
                        if (phase < 0.055) {
                            val envelope = exp(-phase * 78.0)
                            noiseState = noiseState * 1_664_525 + 1_013_904_223
                            val noise = ((noiseState ushr 8) / 8_388_608.0) - 1.0
                            sample += noise * envelope * 0.035
                        }
                    } else {
                        footPhase = 0.0
                    }
                    if (impactFramesLeft > 0) {
                        val decay = impactFramesLeft.toDouble() / (sampleRate / 7.0)
                        noiseState = noiseState * 1_664_525 + 1_013_904_223
                        val noise = ((noiseState ushr 8) / 8_388_608.0) - 1.0
                        sample += noise * decay * 0.24
                        impactFramesLeft--
                    }
                    pcm[i] = (sample.coerceIn(-0.92, 0.92) * Short.MAX_VALUE).toInt().toShort()
                    frame++
                }
                localTrack.write(pcm, 0, pcm.size, AudioTrack.WRITE_BLOCKING)
            }
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        } catch (_: IllegalStateException) {
            // Audio output may be unavailable on emulators or when the OS revokes the stream.
        } catch (_: SecurityException) {
            // Keep rendering playable if the device rejects the optional audio stream.
        } finally {
            if (track === localTrack) track = null
            try {
                if (localTrack?.playState == AudioTrack.PLAYSTATE_PLAYING) localTrack.stop()
            } catch (_: IllegalStateException) { }
            try { localTrack?.release() } catch (_: IllegalStateException) { }
        }
    }
}
