package com.aicompanion.memory

import com.aicompanion.data.local.entity.MemoryEntity
import com.aicompanion.data.repository.MemoryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryExtractor @Inject constructor(
    private val memoryRepository: MemoryRepository
) {
    suspend fun extractMemories(
        userMessage: String,
        assistantResponse: String,
        conversationId: String
    ) {
        // Simple keyword-based memory extraction for MVP
        // Full LLM-based extraction will be added in Phase 5

        val combinedText = "$userMessage $assistantResponse"

        // Extract preferences
        if (combinedText.contains("喜欢") || combinedText.contains("爱吃") || combinedText.contains("最爱")) {
            extractPreference(combinedText, conversationId)
        }

        // Extract personal facts
        if (combinedText.contains("我的") || combinedText.contains("我是") || combinedText.contains("我叫")) {
            extractPersonalFact(combinedText, conversationId)
        }

        // Extract events/plans
        if (combinedText.contains("明天") || combinedText.contains("今天") || combinedText.contains("周末")
            || combinedText.contains("生日") || combinedText.contains("打算")) {
            extractEvent(combinedText, conversationId)
        }
    }

    private suspend fun extractPreference(text: String, conversationId: String) {
        val memory = MemoryEntity(
            category = "preference",
            content = "从对话中提取：$text".take(500),
            importance = 0.6f,
            sourceConversationId = conversationId
        )
        memoryRepository.insertMemory(memory)
    }

    private suspend fun extractPersonalFact(text: String, conversationId: String) {
        val memory = MemoryEntity(
            category = "personal_fact",
            content = "从对话中提取：$text".take(500),
            importance = 0.7f,
            sourceConversationId = conversationId
        )
        memoryRepository.insertMemory(memory)
    }

    private suspend fun extractEvent(text: String, conversationId: String) {
        val memory = MemoryEntity(
            category = "event",
            content = "从对话中提取：$text".take(500),
            importance = 0.5f,
            sourceConversationId = conversationId
        )
        memoryRepository.insertMemory(memory)
    }
}
