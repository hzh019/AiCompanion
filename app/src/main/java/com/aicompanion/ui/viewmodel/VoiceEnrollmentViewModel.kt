package com.aicompanion.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aicompanion.audio.AudioCaptureManager
import com.aicompanion.voice.EnrollmentState
import com.aicompanion.voice.EnrollmentStep
import com.aicompanion.voice.VoiceEnrollmentManager
import com.aicompanion.voice.VoiceActivityDetector
import com.aicompanion.voice.VadState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VoiceEnrollmentViewModel @Inject constructor(
    private val voiceEnrollmentManager: VoiceEnrollmentManager,
    private val audioCaptureManager: AudioCaptureManager
) : ViewModel() {

    val enrollmentState: StateFlow<EnrollmentState> = voiceEnrollmentManager.state

    private val _rmsLevel = MutableStateFlow(0f)
    val rmsLevel: StateFlow<Float> = _rmsLevel.asStateFlow()

    fun startEnrollment(phraseCount: Int = 3) {
        voiceEnrollmentManager.startEnrollment(phraseCount)
        audioCaptureManager.startRecording()

        viewModelScope.launch {
            audioCaptureManager.audioFlow.collect { frame ->
                // Calculate RMS for UI visualization
                var sum = 0.0
                for (sample in frame) {
                    val normalized = sample.toDouble() / Short.MAX_VALUE
                    sum += normalized * normalized
                }
                _rmsLevel.value = kotlin.math.sqrt(sum / frame.size).toFloat()
            }
        }
    }

    fun recordPhrase() {
        viewModelScope.launch {
            // Collect audio for ~3 seconds for the phrase
            val frames = mutableListOf<ShortArray>()
            val job = viewModelScope.launch {
                audioCaptureManager.audioFlow.collect { frame ->
                    frames.add(frame)
                    if (frames.size >= 75) { // ~3 seconds at 40ms per frame
                        throw kotlinx.coroutines.CancellationException("Phrase complete")
                    }
                }
            }

            // Wait for collection
            kotlinx.coroutines.delay(3000)
            job.cancel()

            // Combine frames and add as sample
            val totalSamples = frames.sumOf { it.size }
            val combined = ShortArray(totalSamples)
            var offset = 0
            for (frame in frames) {
                System.arraycopy(frame, 0, combined, offset, frame.size)
                offset += frame.size
            }

            voiceEnrollmentManager.addSample(combined)
        }
    }

    fun finalizeEnrollment(name: String = "主人") {
        viewModelScope.launch {
            voiceEnrollmentManager.finalizeEnrollment(name)
            audioCaptureManager.stopRecording()
        }
    }

    fun reset() {
        voiceEnrollmentManager.reset()
        audioCaptureManager.stopRecording()
        _rmsLevel.value = 0f
    }

    override fun onCleared() {
        super.onCleared()
        audioCaptureManager.stopRecording()
    }
}
