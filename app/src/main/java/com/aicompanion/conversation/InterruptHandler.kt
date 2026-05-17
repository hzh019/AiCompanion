package com.aicompanion.conversation

import com.aicompanion.voice.VoiceActivityDetector
import com.aicompanion.voice.VadState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InterruptHandler @Inject constructor(
    private val voiceActivityDetector: VoiceActivityDetector
) {
    private val _isInterrupted = MutableStateFlow(false)
    val isInterrupted: StateFlow<Boolean> = _isInterrupted.asStateFlow()

    private var isMonitoring = false
    private var onInterrupt: (() -> Unit)? = null

    fun startMonitoring(onInterrupt: () -> Unit) {
        this.onInterrupt = onInterrupt
        isMonitoring = true
        _isInterrupted.value = false
    }

    fun processAudioFrame(frame: ShortArray): Boolean {
        if (!isMonitoring) return false

        val vadState = voiceActivityDetector.processFrame(frame)
        if (vadState == VadState.SPEECH_START || vadState == VadState.SPEECH) {
            _isInterrupted.value = true
            isMonitoring = false
            voiceActivityDetector.reset()
            onInterrupt?.invoke()
            return true
        }
        return false
    }

    fun stopMonitoring() {
        isMonitoring = false
        onInterrupt = null
        voiceActivityDetector.reset()
    }
}
