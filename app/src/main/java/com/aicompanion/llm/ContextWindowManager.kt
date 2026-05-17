package com.aicompanion.llm

import com.aicompanion.data.local.entity.MessageEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContextWindowManager @Inject constructor() {

    companion object {
        const val MAX_TOKENS = 4000  // Budget for conversation context
        const val RESERVED_TOKENS = 1000  // Reserved for system prompt + response
    }

    fun trimMessages(
        messages: List<MessageEntity>,
        maxTokens: Int = MAX_TOKENS
    ): List<MessageEntity> {
        if (messages.isEmpty()) return messages

        var tokenCount = 0
        val trimmed = mutableListOf<MessageEntity>()

        // Process from newest to oldest, keep messages that fit
        for (msg in messages.reversed()) {
            val estimatedTokens = estimateTokens(msg.content)
            if (tokenCount + estimatedTokens <= maxTokens) {
                trimmed.add(0, msg) // Insert at beginning to maintain order
                tokenCount += estimatedTokens
            } else {
                break // Stop when budget exceeded
            }
        }

        return trimmed
    }

    private fun estimateTokens(text: String): Int {
        val chineseChars = text.count { it.code in 0x4E00..0x9FFF }
        val otherChars = text.length - chineseChars
        return (chineseChars * 1.5 + otherChars * 0.3).toInt()
    }
}
