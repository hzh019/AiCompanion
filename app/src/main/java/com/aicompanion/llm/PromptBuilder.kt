package com.aicompanion.llm

import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aicompanion.data.local.entity.MemoryEntity
import com.aicompanion.data.local.entity.MessageEntity
import com.aicompanion.data.local.entity.PersonalityEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PromptBuilder @Inject constructor() {

    companion object {
        private const val MAX_CONTEXT_MESSAGES = 20
        private const val MAX_MEMORIES = 5
        private const val MAX_SYSTEM_PROMPT_TOKENS = 2000
    }

    fun buildSystemPrompt(
        personality: PersonalityEntity,
        memories: List<MemoryEntity>
    ): String {
        val sb = StringBuilder()

        // Character identity
        sb.appendLine("你是${personality.characterName}，${personality.relationship}。")
        sb.appendLine("性格特征：${personality.personalityTraits}")
        sb.appendLine("说话风格：${personality.speakingStyle}")

        if (personality.backstory.isNotBlank()) {
            sb.appendLine("背景故事：${personality.backstory}")
        }

        if (personality.userNickname.isNotBlank()) {
            sb.appendLine("你称呼用户为：${personality.userNickname}")
        }

        // Override or behavioral rules
        val override = personality.systemPromptOverride
        if (override != null && override.isNotBlank()) {
            sb.clear()
            sb.append(override)
        } else {
            sb.appendLine()
            sb.appendLine("【行为准则】")
            sb.appendLine("- 用自然、真实的语气交流，像真人一样，不要像机器人")
            sb.appendLine("- 回复要简洁精炼，控制在1-3句话内（除非用户要求详细回答）")
            sb.appendLine("- 可以适当使用语气词和表情文字，但不要过度")
            sb.appendLine("- 偶尔主动关心用户，根据时间和上下文发起话题")
            sb.appendLine("- 称呼用户为${personality.userNickname.ifBlank { "亲爱的" }}")
            sb.appendLine("- 用${if (personality.language == "zh-CN") "中文" else "English"}交流")
        }

        // Inject memories
        if (memories.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("【关于用户的重要记忆】")
            memories.take(MAX_MEMORIES).forEach { memory ->
                sb.appendLine("- ${memory.content}")
            }
        }

        return sb.toString()
    }

    fun buildMessages(
        recentMessages: List<MessageEntity>,
        currentUserMessage: String
    ): List<ChatMessage> {
        val result = mutableListOf<ChatMessage>()

        // Add recent context (last N messages, oldest first)
        val contextMessages = recentMessages.takeLast(MAX_CONTEXT_MESSAGES)
        contextMessages.forEach { msg ->
            val role = when (msg.role) {
                "user" -> ChatRole.User
                "assistant" -> ChatRole.Assistant
                else -> ChatRole.System
            }
            result.add(ChatMessage(role = role, content = msg.content))
        }

        // Add current user message
        result.add(ChatMessage(role = ChatRole.User, content = currentUserMessage))

        return result
    }
}
