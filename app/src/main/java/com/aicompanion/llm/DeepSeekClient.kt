package com.aicompanion.llm

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeepSeekClient @Inject constructor() {

    private val apiKey = "sk-23deea69cfe1481984c9b549089205d2"
    private val baseUrl = "https://api.deepseek.com/v1/chat/completions"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    fun chatCompletionStream(
        systemPrompt: String,
        messages: List<Map<String, String>>,
        maxTokens: Int = 1024,
        temperature: Double = 0.9
    ): Flow<String> = flow {
        val messagesArray = JSONArray()
        messagesArray.put(JSONObject().apply {
            put("role", "system")
            put("content", systemPrompt)
        })
        messages.forEach { msg ->
            messagesArray.put(JSONObject().apply {
                put("role", msg["role"] ?: "user")
                put("content", msg["content"] ?: "")
            })
        }

        val body = JSONObject().apply {
            put("model", "deepseek-chat")
            put("messages", messagesArray)
            put("max_tokens", maxTokens)
            put("temperature", temperature)
            put("top_p", 0.95)
            put("stream", true)
        }

        val requestBody = body.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(baseUrl)
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .post(requestBody)
            .build()

        val response = withContext(Dispatchers.IO) {
            client.newCall(request).execute()
        }

        if (!response.isSuccessful) {
            response.close()
            throw Exception("DeepSeek API error: ${response.code} ${response.message}")
        }

        val reader = withContext(Dispatchers.IO) {
            response.body?.byteStream()?.bufferedReader()
        } ?: run {
            response.close()
            throw Exception("Empty response body")
        }

        reader.use { br ->
            br.forEachLine { line ->
                if (line.startsWith("data: ")) {
                    val json = line.removePrefix("data: ").trim()
                    if (json == "[DONE]") return@forEachLine
                    try {
                        val obj = JSONObject(json)
                        val choices = obj.optJSONArray("choices")
                        val delta = choices?.optJSONObject(0)?.optJSONObject("delta")
                        val content = delta?.optString("content", "") ?: ""
                        if (content.isNotEmpty()) {
                            emit(content)
                        }
                    } catch (_: Exception) { }
                }
            }
        }
        response.close()
    }

    suspend fun countTokens(text: String): Int {
        val chineseChars = text.count { it.code in 0x4E00..0x9FFF }
        val otherChars = text.length - chineseChars
        return (chineseChars * 1.5 + otherChars * 0.3).toInt()
    }
}
