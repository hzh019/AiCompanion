package com.aicompanion.memory

import com.aicompanion.data.local.entity.MemoryEntity
import com.aicompanion.data.repository.MemoryRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryRetriever @Inject constructor(
    private val memoryRepository: MemoryRepository
) {
    /**
     * Retrieve top-K most relevant memories for the current conversation context.
     * Uses keyword overlap scoring + importance + recency.
     */
    suspend fun retrieveRelevant(
        query: String,
        topK: Int = 5
    ): List<MemoryEntity> {
        val allMemories = memoryRepository.getAllActive().firstOrNull() ?: return emptyList()

        val now = System.currentTimeMillis()

        return allMemories
            .map { memory ->
                val score = calculateRelevanceScore(query, memory, now)
                memory to score
            }
            .filter { it.second > 0f }
            .sortedByDescending { it.second }
            .take(topK)
            .map { it.first }
    }

    private fun calculateRelevanceScore(
        query: String,
        memory: MemoryEntity,
        now: Long
    ): Float {
        var score = 0f

        // 1. Keyword overlap score (0-0.5)
        val queryWords = query.split(" ", "，", "。", "！", "？", "、", "\n")
            .filter { it.length >= 1 }
        val memoryWords = memory.content.split(" ", "，", "。", "！", "？", "、", "\n")
            .filter { it.length >= 1 }

        if (queryWords.isNotEmpty()) {
            val overlapCount = queryWords.count { qw ->
                memoryWords.any { mw -> mw.contains(qw) || qw.contains(mw) }
            }
            score += (overlapCount.toFloat() / queryWords.size) * 0.4f
        }

        // 2. Category-based bonus
        when (memory.category) {
            "personal_fact" -> score += 0.15f
            "preference" -> score += 0.1f
            "relationship" -> score += 0.1f
            "event" -> {
                // Events get bonus only if they might be recent/relevant
                score += 0.05f
            }
        }

        // 3. Importance factor (0-0.2)
        score += memory.importance * 0.2f

        // 4. Recency bonus (0-0.15)
        val daysSinceLastAccess = if (memory.lastAccessedAt != null) {
            (now - memory.lastAccessedAt) / (1000 * 60 * 60 * 24f)
        } else {
            30f // Never accessed
        }
        if (daysSinceLastAccess < 7) {
            score += 0.15f * (1f - daysSinceLastAccess / 7f)
        }

        // 5. Access count bonus (0-0.1)
        score += (memory.accessCount.coerceAtMost(10) / 100f)

        return score.coerceIn(0f, 1f)
    }
}
