package com.aicompanion.audio

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.NoiseSuppressor
import android.os.Process
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioCaptureManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val SAMPLE_RATE = 16000
        const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        const val FRAME_SIZE_MS = 40
        val FRAME_SIZE_SAMPLES: Int = SAMPLE_RATE * FRAME_SIZE_MS / 1000 // 640 samples per frame
    }

    private var audioRecord: AudioRecord? = null
    private var noiseSuppressor: NoiseSuppressor? = null
    private var echoCanceler: AcousticEchoCanceler? = null
    private val audioChannel = Channel<ShortArray>(Channel.BUFFERED)
    private var isRecording = false

    val audioFlow: Flow<ShortArray> = audioChannel.receiveAsFlow()

    @SuppressLint("MissingPermission")
    fun startRecording(): Boolean {
        if (isRecording) return true

        if (!hasPermission()) return false

        try {
            val minBufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT
            )
            val bufferSize = maxOf(minBufferSize * 2, FRAME_SIZE_SAMPLES * 2)

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                audioRecord?.release()
                audioRecord = null
                return false
            }

            // Enable noise suppression if available
            try {
                val sessionId = audioRecord!!.audioSessionId
                if (NoiseSuppressor.isAvailable()) {
                    noiseSuppressor = NoiseSuppressor.create(sessionId)
                    noiseSuppressor?.enabled = true
                }
                if (AcousticEchoCanceler.isAvailable()) {
                    echoCanceler = AcousticEchoCanceler.create(sessionId)
                    echoCanceler?.enabled = true
                }
            } catch (e: Exception) {
                // Gracefully handle if effects not available
            }

            audioRecord?.startRecording()
            isRecording = true

            // Start reading loop on a dedicated thread
            Thread({
                Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
                val buffer = ShortArray(FRAME_SIZE_SAMPLES)
                while (isRecording) {
                    val read = audioRecord?.read(buffer, 0, FRAME_SIZE_SAMPLES) ?: -1
                    if (read > 0) {
                        val frame = if (read == FRAME_SIZE_SAMPLES) {
                            buffer.copyOf()
                        } else {
                            buffer.copyOf(read)
                        }
                        audioChannel.trySend(frame)
                    }
                }
            }, "AudioCapture").start()

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun stopRecording() {
        isRecording = false
        try {
            noiseSuppressor?.release()
            echoCanceler?.release()
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            // Ignore
        }
        audioRecord = null
        noiseSuppressor = null
        echoCanceler = null
    }

    fun isActive(): Boolean = isRecording

    private fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
}
