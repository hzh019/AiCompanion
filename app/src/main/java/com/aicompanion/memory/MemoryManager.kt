package com.aicompanion.memory

import com.aicompanion.data.local.entity.MemoryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryManager @Inject constructor(
    private val memoryExtractor: MemoryExtractor,
    private val memoryRetriever: MemoryRetriever,
    private val memoryConsolidator: MemoryConsolidator
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Extract memories from a conversation turn (async, non-blocking).
     */
    fun extractFromTurn(
        userMessage: String,
        assistantResponse: String,
        conversationId: String
    ) {
        scope.launch {
            memoryExtractor.extractMemories(userMessage, assistantResponse, conversationId)
        }
    }

    /**
     * Retrieve relevant memories for a query.
     */
    suspend fun getRelevantMemories(query: String, topK: Int = 5): List<MemoryEntity> {
        return memoryRetriever.retrieveRelevant(query, topK)
    }

    /**
     * Run memory consolidation (should be called by WorkManager periodically).
     */
    suspend fun runConsolidation(): Int {
        return memoryConsolidator.consolidate()
    }
}
