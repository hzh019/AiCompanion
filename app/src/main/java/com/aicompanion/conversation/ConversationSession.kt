package com.aicompanion.conversation

import com.aicompanion.data.local.entity.MessageEntity
import java.util.UUID

data class ConversationSession(
    val conversationId: String = UUID.randomUUID().toString(),
    val messages: MutableList<MessageEntity> = mutableListOf(),
    val isActive: Boolean = true,
    val currentPartialText: String = "",       // For streaming STT display
    val currentResponseText: String = "",      // For streaming LLM display
    val isAlwaysListening: Boolean = false
) {
    fun addMessage(message: MessageEntity) {
        messages.add(message)
    }

    fun getRecentMessages(count: Int = 20): List<MessageEntity> {
        return messages.takeLast(count)
    }
}
