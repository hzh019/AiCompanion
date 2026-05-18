package com.aicompanion.voice

import com.aicompanion.data.repository.VoiceProfileRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

data class VerificationResult(
    val isMatch: Boolean,
    val confidence: Float,
    val matchedProfileId: String? = null,
    val matchedProfileName: String? = null
)

@Singleton
class SpeakerIdentifier @Inject constructor(
    private val voiceProfileRepository: VoiceProfileRepository
) {
    companion object {
        private const val SIMILARITY_THRESHOLD = 0.65f
        private const val EMBEDDING_DIM = 256
    }

    /**
     * Verify if the given audio matches any enrolled voice profile.
     * For MVP, this uses a placeholder implementation.
     * Real implementation uses sherpa-onnx speaker embedding model.
     */
    suspend fun verify(audioFrames: List<ShortArray>): VerificationResult {
        val activeProfile = voiceProfileRepository.getActiveProfile()

        if (activeProfile == null) {
            // No enrolled profile — allow access (first-time user)
            return VerificationResult(
                isMatch = true,
                confidence = 0f,
                matchedProfileName = "未注册"
            )
        }

        // Extract embedding from audio (placeholder)
        val inputEmbedding = extractEmbedding(audioFrames)

        // Compare with stored embedding
        val similarity = cosineSimilarity(inputEmbedding, activeProfile.embedding)

        return VerificationResult(
            isMatch = similarity >= SIMILARITY_THRESHOLD,
            confidence = similarity,
            matchedProfileId = activeProfile.id.takeIf { similarity >= SIMILARITY_THRESHOLD },
            matchedProfileName = activeProfile.name.takeIf { similarity >= SIMILARITY_THRESHOLD }
        )
    }

    /**
     * Extract speaker embedding from audio frames.
     * Placeholder: computes energy-based pseudo-embedding.
     * Real impl: use sherpa-onnx SpeakerEmbeddingExtractor.
     */
    fun extractEmbedding(audioFrames: List<ShortArray>): FloatArray {
        val embedding = FloatArray(EMBEDDING_DIM)

        if (audioFrames.isEmpty()) return embedding

        // Compute energy statistics per frame for pseudo-embedding
        var frameIndex = 0
        for (frame in audioFrames) {
            if (frame.isEmpty()) continue
            val energy = frame.map {
                val normalized = it.toDouble() / Short.MAX_VALUE
                normalized * normalized
            }.average().toFloat()

            // Spread across embedding dimensions
            for (i in embedding.indices) {
                embedding[i] += energy * kotlin.math.sin((i + frameIndex) * 0.1).toFloat()
            }
            frameIndex++
        }

        if (frameIndex > 0) {
            for (i in embedding.indices) {
                embedding[i] /= frameIndex.toFloat()
            }
        }

        // Normalize to unit vector
        val magnitude = sqrt(embedding.map { it * it }.sum())
        if (magnitude > 0) {
            for (i in embedding.indices) {
                embedding[i] /= magnitude
            }
        }

        return embedding
    }

    /**
     * Compute cosine similarity between two embedding vectors.
     */
    fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        if (a.size != b.size) return 0f

        var dotProduct = 0f
        var normA = 0f
        var normB = 0f

        for (i in a.indices) {
            dotProduct += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }

        return if (normA > 0 && normB > 0) {
            dotProduct / (sqrt(normA) * sqrt(normB))
        } else {
            0f
        }
    }

    /**
     * Check if any voice profile is enrolled
     */
    suspend fun hasEnrolledProfile(): Boolean {
        return voiceProfileRepository.getActiveProfile() != null
    }
}
