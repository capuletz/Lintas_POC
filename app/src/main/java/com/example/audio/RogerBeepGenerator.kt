package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

object RogerBeepGenerator {

    private val audioScope = CoroutineScope(Dispatchers.Default)
    private const val SAMPLE_RATE = 44100

    /**
     * Plays a high-pitched radio squelch chirp when PTT is pressed (TX start).
     */
    fun playTxStartChirp() {
        audioScope.launch {
            try {
                // Quick 1200 Hz chirp for 50 ms
                val samples = generateTonePcm(frequency = 1200.0, durationMs = 50, amplitude = 0.6f)
                playPcm(samples)
            } catch (_: Exception) {
            }
        }
    }

    /**
     * Plays classic dual-tone Roger Beep when PTT is released (TX end).
     */
    fun playRogerBeep(baseFrequency: Int = 1050) {
        audioScope.launch {
            try {
                // Two-tone roger beep (Tone 1 then Tone 2)
                val tone1 = generateTonePcm(frequency = baseFrequency.toDouble(), durationMs = 70, amplitude = 0.7f)
                val pause = ShortArray((SAMPLE_RATE * 0.02).toInt()) // 20ms silence
                val tone2 = generateTonePcm(frequency = (baseFrequency * 0.75), durationMs = 80, amplitude = 0.7f)

                val combined = ShortArray(tone1.size + pause.size + tone2.size)
                System.arraycopy(tone1, 0, combined, 0, tone1.size)
                System.arraycopy(pause, 0, combined, tone1.size, pause.size)
                System.arraycopy(tone2, 0, combined, tone1.size + pause.size, tone2.size)

                playPcm(combined)
            } catch (_: Exception) {
            }
        }
    }

    /**
     * Plays short key click tone when navigating keypad.
     */
    fun playKeyClickTone() {
        audioScope.launch {
            try {
                val samples = generateTonePcm(frequency = 800.0, durationMs = 25, amplitude = 0.3f)
                playPcm(samples)
            } catch (_: Exception) {
            }
        }
    }

    private fun generateTonePcm(frequency: Double, durationMs: Int, amplitude: Float): ShortArray {
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)
        val angularFrequency = 2.0 * Math.PI * frequency / SAMPLE_RATE

        for (i in 0 until numSamples) {
            // Apply slight cosine envelope to prevent audio click at boundaries
            val envelope = when {
                i < 100 -> (i / 100.0).toFloat()
                i > numSamples - 100 -> ((numSamples - i) / 100.0).toFloat()
                else -> 1.0f
            }
            val sampleVal = (sin(angularFrequency * i) * Short.MAX_VALUE * amplitude * envelope).toInt()
            buffer[i] = sampleVal.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun playPcm(samples: ShortArray) {
        val bufferSize = samples.size * 2
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        track.write(samples, 0, samples.size)
        track.play()
        track.notificationMarkerPosition = samples.size
        track.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
            override fun onMarkerReached(t: AudioTrack?) {
                t?.release()
            }
            override fun onPeriodicNotification(t: AudioTrack?) {}
        })
    }
}
