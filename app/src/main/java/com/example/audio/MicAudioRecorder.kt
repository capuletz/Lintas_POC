package com.example.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sqrt

class MicAudioRecorder(
    private val onLevelChanged: (Float) -> Unit
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var recordJob: Job? = null
    private var audioRecord: AudioRecord? = null

    @SuppressLint("MissingPermission")
    fun startRecording(hasPermission: Boolean, gainBoost: Float = 1.2f) {
        stopRecording()

        if (!hasPermission) {
            // Provide simulated dynamic audio fluctuations when mic permission is not granted yet
            recordJob = scope.launch {
                var tick = 0
                while (isActive) {
                    val simVal = (25f + 35f * abs(kotlin.math.sin(tick * 0.4))).toFloat() * gainBoost
                    onLevelChanged(simVal.coerceIn(10f, 95f))
                    tick++
                    kotlinx.coroutines.delay(80)
                }
            }
            return
        }

        recordJob = scope.launch {
            val sampleRate = 16000
            val channelConfig = AudioFormat.CHANNEL_IN_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT
            val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat).coerceAtLeast(1024)

            try {
                val record = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    bufferSize
                )
                audioRecord = record

                if (record.state == AudioRecord.STATE_INITIALIZED) {
                    record.startRecording()
                    val audioBuffer = ShortArray(bufferSize)

                    while (isActive) {
                        val readSize = record.read(audioBuffer, 0, audioBuffer.size)
                        if (readSize > 0) {
                            var sumSquares = 0.0
                            for (i in 0 until readSize) {
                                val sample = audioBuffer[i].toDouble()
                                sumSquares += sample * sample
                            }
                            val rms = sqrt(sumSquares / readSize)
                            // Map typical RMS values (0 to ~8000) to 0..100 percentage
                            val normalizedLevel = ((rms / 80.0) * gainBoost).toFloat().coerceIn(0f, 100f)
                            onLevelChanged(normalizedLevel)
                        }
                        kotlinx.coroutines.delay(50)
                    }
                }
            } catch (_: Exception) {
                onLevelChanged(20f)
            } finally {
                cleanupAudioRecord()
            }
        }
    }

    fun stopRecording() {
        recordJob?.cancel()
        recordJob = null
        cleanupAudioRecord()
        onLevelChanged(0f)
    }

    private fun cleanupAudioRecord() {
        try {
            audioRecord?.let {
                if (it.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    it.stop()
                }
                it.release()
            }
        } catch (_: Exception) {
        }
        audioRecord = null
    }
}
