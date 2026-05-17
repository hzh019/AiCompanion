package com.aicompanion.voice

import android.content.Context
import com.aicompanion.data.local.entity.VoiceProfileEntity
import com.aicompanion.data.repository.VoiceProfileRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class EnrollmentStep {
    READY,
    RECORDING_PHRASE,
    PROCESSING,
    COMPLETE,
    ERROR
}

data class EnrollmentState(
    val step: EnrollmentStep = EnrollmentStep.READY,
    val currentPhraseIndex: Int = 0,
    val totalPhrases: Int = 0,
    val currentPhraseText: String = "",
    val recordedSamples: Int = 0,
    val message: String = ""
)

@Singleton
class VoiceEnrollmentManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val voiceProfileRepository: VoiceProfileRepository
) {
    companion object {
        val ENROLLMENT_PHRASES = listOf(
            "你好，我是这台手机的主人",
            "今天的天气真不错",
            "我想和你聊聊天",
            "帮我设置一个提醒",
            "晚安，明天见"
        )

        const val MIN_SAMPLES = 3
        const val EMBEDDING_DIM = 256
        const val SIMILARITY_THRESHOLD = 0.65f
    }

    private val _state = MutableStateFlow(EnrollmentState())
    val state: StateFlow<EnrollmentState> = _state.asStateFlow()

    private val audioSamples = mutableListOf<ShortArray>()

    fun startEnrollment(totalPhrases: Int = MIN_SAMPLES) {
        audioSamples.clear()
        _state.value = EnrollmentState(
            step = EnrollmentStep.RECORDING_PHRASE,
            currentPhraseIndex = 0,
            totalPhrases = totalPhrases,
            currentPhraseText = ENROLLMENT_PHRASES[0],
            message = "请朗读以下文字"
        )
    }

    fun addSample(audioData: ShortArray): Boolean {
        if (_state.value.step != EnrollmentStep.RECORDING_PHRASE) return false

        audioSamples.add(audioData)
        val nextIndex = _state.value.currentPhraseIndex + 1

        if (nextIndex >= _state.value.totalPhrases) {
            _state.value = EnrollmentState(
                step = EnrollmentStep.PROCESSING,
                currentPhraseIndex = nextIndex,
                totalPhrases = _state.value.totalPhrases,
                recordedSamples = audioSamples.size,
                message = "正在分析你的声音..."
            )
            return true
        }

        _state.value = EnrollmentState(
            step = EnrollmentStep.RECORDING_PHRASE,
            currentPhraseIndex = nextIndex,
            totalPhrases = _state.value.totalPhrases,
            currentPhraseText = ENROLLMENT_PHRASES[nextIndex % ENROLLMENT_PHRASES.size],
            recordedSamples = audioSamples.size,
            message = "请继续朗读"
        )
        return true
    }

    suspend fun finalizeEnrollment(name: String = "Owner"): Boolean {
        return try {
            // For MVP: generate a deterministic placeholder embedding
            // Real implementation: use sherpa-onnx speaker embedding model
            val embedding = computeAverageEmbedding()

            val profile = VoiceProfileEntity(
                name = name,
                embedding = embedding,
                samplesCount = audioSamples.size
            )

            voiceProfileRepository.insertProfile(profile)
            voiceProfileRepository.setActiveProfile(profile.id)

            _state.value = EnrollmentState(
                step = EnrollmentStep.COMPLETE,
                recordedSamples = audioSamples.size,
                message = "声音注册成功！"
            )
            true
        } catch (e: Exception) {
            _state.value = EnrollmentState(
                step = EnrollmentStep.ERROR,
                message = "注册失败：${e.message}"
            )
            false
        }
    }

    /**
     * Compute average embedding from recorded samples.
     * Placeholder implementation — real version uses sherpa-onnx.
     */
    private fun computeAverageEmbedding(): FloatArray {
        // Placeholder: generate a simple feature vector from audio statistics
        val embedding = FloatArray(EMBEDDING_DIM)
        var sampleCount = 0

        for (sample in audioSamples) {
            if (sample.isNotEmpty()) {
                val energy = sample.map { (it.toDouble() / Short.MAX_VALUE) }.average().toFloat()
                // Spread energy across embedding dimensions with a pseudo-random pattern
                for (i in embedding.indices) {
                    embedding[i] += energy * kotlin.math.sin((i + sampleCount) * 0.1).toFloat()
                }
                sampleCount++
            }
        }

        if (sampleCount > 0) {
            for (i in embedding.indices) {
                embedding[i] /= sampleCount.toFloat()
            }
        }

        // Normalize to unit vector
        val magnitude = kotlin.math.sqrt(embedding.map { it * it }.sum())
        if (magnitude > 0) {
            for (i in embedding.indices) {
                embedding[i] /= magnitude
            }
        }

        return embedding
    }

    fun reset() {
        audioSamples.clear()
        _state.value = EnrollmentState()
    }
}
