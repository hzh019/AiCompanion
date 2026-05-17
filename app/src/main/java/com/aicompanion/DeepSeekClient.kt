package com.aicompanion

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.models.ModelId
import com.aallam.openai.client.OpenAI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Custom wrapper around the openai-kotlin client configured for DeepSeek.
 */
class DeepSeekClient(
    private val openAI: OpenAI,
    private val defaultModel: String = "deepseek-chat"
) {

    /**
     * Send a single chat completion request and return the assistant's reply text.
     */
    suspend fun chat(messages: List<Pair<String, String>>): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val chatMessages = messages.map { (role, content) ->
                    ChatMessage(
                        role = ChatRole(role),
                        content = content
                    )
                }

                val request = ChatCompletionRequest(
                    model = ModelId(defaultModel),
                    messages = chatMessages
                )

                val completion = openAI.chatCompletion(request)
                completion.choices.firstOrNull()?.message?.content
                    ?: throw IllegalStateException("No response from DeepSeek")
            }
        }

    /**
     * Stream chat completions and invoke the callback for each chunk.
     */
    suspend fun chatStream(
        messages: List<Pair<String, String>>,
        onChunk: (String) -> Unit
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val chatMessages = messages.map { (role, content) ->
                ChatMessage(
                    role = ChatRole(role),
                    content = content
                )
            }

            val request = ChatCompletionRequest(
                model = ModelId(defaultModel),
                messages = chatMessages
            )

            openAI.chatCompletions(request).collect { chunk ->
                chunk.choices.firstOrNull()?.delta?.content?.let { delta ->
                    onChunk(delta)
                }
            }
        }
    }
}
