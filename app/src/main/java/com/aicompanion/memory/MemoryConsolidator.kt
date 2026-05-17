package com.aicompanion.memory

import android.util.Log
import com.aicompanion.data.local.entity.MemoryEntity
import com.aicompanion.data.repository.MemoryRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryConsolidator @Inject constructor(
    private val memoryRepository: MemoryRepository
) {
    companion object {
        private const val TAG = "MemoryConsolidator"
        private const val DECAY_THRESHOLD = 0.05f  // Importance below this gets archived
        private const val MERGE_SIMILARITY = 0.7f   // Content similarity threshold for merging
    }

    /**
     * Run full consolidation pass. Returns number of changes made.
     * Should be called periodically (e.g., daily via WorkManager).
     */
    suspend fun consolidate(): Int {
        var changes = 0

        try {
            val allMemories = memoryRepository.getAllActive().firstOrNull() ?: return 0

            // 1. Apply time decay to all memories
            changes += applyDecay(allMemories)

            // 2. Merge similar memories
            changes += mergeSimilar(allMemories)

            // 3. Archive memories that dropped below threshold
            changes += archiveLowImportance()

        } catch (e: Exception) {
            Log.e(TAG, "Consolidation failed", e)
        }

        return changes
    }

    private suspend fun applyDecay(memories: List<MemoryEntity>): Int {
        var updated = 0
        val now = System.currentTimeMillis()
        val dayMs = 1000 * 60 * 60 * 24L

        for (memory in memories) {
            val daysSinceUpdate = (now - memory.updatedAt) / dayMs
            if (daysSinceUpdate > 1) {
                val decayAmount = memory.decayRate * daysSinceUpdate
                val newImportance = (memory.importance - decayAmount).coerceAtLeast(0f)

                if (kotlin.math.abs(newImportance - memory.importance) > 0.001f) {
                    memoryRepository.updateMemory(
                        memory.copy(
                            importance = newImportance,
                            updatedAt = now
                        )
                    )
                    updated++
                }
            }
        }

        return updated
    }

    private suspend fun mergeSimilar(memories: List<MemoryEntity>): Int {
        var merged = 0
        val processed = mutableSetOf<String>()

        for (i in memories.indices) {
            if (memories[i].id in processed) continue

            for (j in i + 1 until memories.size) {
                if (memories[j].id in processed) continue

                val similarity = calculateTextSimilarity(
                    memories[i].content,
                    memories[j].content
                )

                if (similarity >= MERGE_SIMILARITY) {
                    // Merge j into i (keep the more important one, or the older one)
                    val (keeper, merged_) = if (memories[i].importance >= memories[j].importance) {
                        memories[i] to memories[j]
                    } else {
                        memories[j] to memories[i]
                    }

                    // Update keeper with merged info
                    memoryRepository.updateMemory(
                        keeper.copy(
                            importance = maxOf(keeper.importance, merged_.importance) * 1.1f,
                            accessCount = keeper.accessCount + merged_.accessCount,
                            updatedAt = System.currentTimeMillis(),
                            content = if (keeper.content.length >= merged_.content.length)
                                keeper.content else merged_.content
                        )
                    )

                    // Archive the merged one
                    memoryRepository.softDeleteMemory(merged_.id)
                    processed.add(merged_.id)
                    merged++
                }
            }
        }

        return merged
    }

    private suspend fun archiveLowImportance(): Int {
        var archived = 0
        val memories = memoryRepository.getAllActive().firstOrNull() ?: return 0

        for (memory in memories) {
            if (memory.importance <= DECAY_THRESHOLD) {
                // Archive: set isActive to false
                memoryRepository.softDeleteMemory(memory.id)
                archived++
            }
        }

        return archived
    }

    /**
     * Simple Jaccard-like text similarity for Chinese text.
     */
    private fun calculateTextSimilarity(text1: String, text2: String): Float {
        val set1 = text1.toCharArray().toSet()
        val set2 = text2.toCharArray().toSet()

        if (set1.isEmpty() || set2.isEmpty()) return 0f

        val intersection = set1.intersect(set2).size
        val union = set1.union(set2).size

        return if (union > 0) intersection.toFloat() / union else 0f
    }
}
