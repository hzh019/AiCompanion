package com.aicompanion.llm

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeepSeekClient @Inject constructor() {

    // TODO: Replace with BuildConfig field or secure storage
    private val apiKey = "sk-23deea69cfe1481984c9b549089205d2"

    private val openAI by lazy {
        OpenAI(
            token = apiKey,
            host = OpenAIConfig.Host(baseUrl = "https://api.deepseek.com/v1"),
            timeout = com.aallam.openai.client.Timeout(
                socket = 60_000
            )
        )
    }

    @OptIn(BetaOpenAI::class)
    fun chatCompletionStream(
        systemPrompt: String,
        messages: List<ChatMessage>,
        maxTokens: Int = 1024,
        temperature: Double = 0.9
    ): Flow<String> {
        val allMessages = listOf(
            ChatMessage(role = ChatRole.System, content = systemPrompt)
        ) + messages

        val request = ChatCompletionRequest(
            model = ModelId("deepseek-chat"),
            messages = allMessages,
            maxTokens = maxTokens,
            temperature = temperature,
            topP = 0.95
        )

        return openAI.chat.chatCompletionsStream(request)
            .map { chunk ->
                chunk.choices.firstOrNull()?.delta?.content ?: ""
            }
    }

    suspend fun countTokens(text: String): Int {
        // Approximate: Chinese chars ~1.5 tokens, English words ~1.3 tokens
        val chineseChars = text.count { it.code in 0x4E00..0x9FFF }
        val otherChars = text.length - chineseChars
        return (chineseChars * 1.5 + otherChars * 0.3).toInt()
    }
}
