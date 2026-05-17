package com.aicompanion.voice

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

enum class VadState {
    SILENCE, SPEECH_START, SPEECH, SPEECH_END
}

@Singleton
class VoiceActivityDetector @Inject constructor() {
    companion object {
        private const val ENERGY_THRESHOLD = 0.02f       // RMS energy threshold
        private const val SPEECH_START_FRAMES = 3          // Consecutive frames to confirm start
        private const val SPEECH_END_FRAMES = 15           // Consecutive silence frames to confirm end
        private const val MIN_UTTERANCE_FRAMES = 10        // Minimum utterance length (avoid noise triggers)
    }

    private var state: VadState = VadState.SILENCE
    private var speechFrameCount = 0
    private var silenceFrameCount = 0
    private var utteranceFrames = mutableListOf<ShortArray>()

    fun processFrame(frame: ShortArray): VadState {
        val energy = calculateRmsEnergy(frame)
        val isSpeech = energy > ENERGY_THRESHOLD

        when (state) {
            VadState.SILENCE -> {
                if (isSpeech) {
                    speechFrameCount++
                    utteranceFrames.add(frame)
                    if (speechFrameCount >= SPEECH_START_FRAMES) {
                        state = VadState.SPEECH_START
                        speechFrameCount = 0
                    }
                } else {
                    speechFrameCount = 0
                }
            }
            VadState.SPEECH_START -> {
                state = VadState.SPEECH
                utteranceFrames.add(frame)
            }
            VadState.SPEECH -> {
                utteranceFrames.add(frame)
                if (!isSpeech) {
                    silenceFrameCount++
                    if (silenceFrameCount >= SPEECH_END_FRAMES) {
                        // Only emit end if minimum utterance length met
                        if (utteranceFrames.size >= MIN_UTTERANCE_FRAMES) {
                            state = VadState.SPEECH_END
                        } else {
                            reset()
                        }
                    }
                } else {
                    silenceFrameCount = 0
                }
            }
            VadState.SPEECH_END -> {
                // Will be reset by caller after consuming utterance
            }
        }
        return state
    }

    fun getUtteranceFrames(): List<ShortArray> = utteranceFrames.toList()

    fun reset() {
        state = VadState.SILENCE
        speechFrameCount = 0
        silenceFrameCount = 0
        utteranceFrames.clear()
    }

    fun getState(): VadState = state

    private fun calculateRmsEnergy(frame: ShortArray): Float {
        var sum = 0.0
        for (sample in frame) {
            val normalized = sample.toDouble() / Short.MAX_VALUE
            sum += normalized * normalized
        }
        return sqrt(sum / frame.size).toFloat()
    }
}
