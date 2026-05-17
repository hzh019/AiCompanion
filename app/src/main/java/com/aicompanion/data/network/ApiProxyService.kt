package com.aicompanion.data.network

import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit interface for the backend proxy API.
 * The backend proxy handles auth tokens, usage quotas, and
 * optionally pre-processes prompts before forwarding to DeepSeek.
 *
 * TODO: Define actual endpoints when the backend proxy is ready.
 */
interface ApiProxyService {

    @POST("v1/chat/completions")
    suspend fun chatCompletion(
        @Body request: ChatCompletionRequest
    ): ChatCompletionResponse
}

/**
 * Placeholder request/response models for the proxy API.
 * TODO: Finalize the schema with the backend team.
 */
data class ChatCompletionRequest(
    val model: String,
    val messages: List<Message>,
    val max_tokens: Int = 1024,
    val temperature: Double = 0.9,
    val stream: Boolean = false
)

data class Message(
    val role: String,
    val content: String
)

data class ChatCompletionResponse(
    val id: String,
    val choices: List<Choice>,
    val usage: Usage?
)

data class Choice(
    val index: Int,
    val message: Message,
    val finish_reason: String?
)

data class Usage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
)
