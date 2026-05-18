package com.aicompanion.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aicompanion.conversation.ConversationState
import com.aicompanion.data.local.entity.MessageEntity
import com.aicompanion.data.repository.ConversationRepository
import com.aicompanion.data.repository.MemoryRepository
import com.aicompanion.llm.DeepSeekClient
import com.aicompanion.llm.PromptBuilder
import com.aicompanion.personality.PersonalityManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

data class ConversationUiState(
    val conversationState: ConversationState = ConversationState.Idle,
    val partialText: String = "",
    val responseText: String = "",
    val messages: List<MessageEntity> = emptyList(),
    val isListening: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ConversationViewModel @Inject constructor(
    private val app: Application,
    private val deepSeekClient: DeepSeekClient,
    private val promptBuilder: PromptBuilder,
    private val personalityManager: PersonalityManager,
    private val memoryRepository: MemoryRepository,
    private val conversationRepository: ConversationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConversationUiState())
    val uiState: StateFlow<ConversationUiState> = _uiState.asStateFlow()

    private val _textInput = MutableStateFlow("")
    val textInput: StateFlow<String> = _textInput.asStateFlow()

    private var tts: TextToSpeech? = null

    init {
        viewModelScope.launch {
            personalityManager.ensureDefaultPersonality()
        }
        tts = TextToSpeech(app) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.CHINESE
            }
        }
    }

    fun updateTextInput(text: String) {
        _textInput.value = text
    }

    fun onVoiceResult(text: String?) {
        _uiState.update { it.copy(isListening = false) }
        if (!text.isNullOrBlank()) {
            _textInput.value = text
            sendTextMessage()
        }
    }

    fun sendTextMessage() {
        val text = _textInput.value.trim()
        if (text.isBlank()) return

        _textInput.value = ""
        _uiState.update { it.copy(partialText = text, errorMessage = null) }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(conversationState = ConversationState.Thinking) }

                val personality = personalityManager.getActivePersonality()
                val systemPrompt = if (personality != null) {
                    val memories = memoryRepository.getTopByImportance(5)
                    promptBuilder.buildSystemPrompt(personality, memories)
                } else {
                    "You are a caring AI companion. Respond naturally in Chinese."
                }

                val recentMessages = _uiState.value.messages.takeLast(20)
                val chatMessages = promptBuilder.buildMessages(recentMessages, text)

                val userMsg = MessageEntity(
                    conversationId = getOrCreateConversationId(),
                    role = "user",
                    content = text
                )
                _uiState.update { it.copy(messages = it.messages + userMsg) }

                val responseBuilder = StringBuilder()
                deepSeekClient.chatCompletionStream(systemPrompt, chatMessages)
                    .collect { token ->
                        responseBuilder.append(token)
                        _uiState.update { it.copy(responseText = responseBuilder.toString()) }
                    }

                val finalResponse = responseBuilder.toString().ifBlank { "嗯，我在听呢..." }

                val assistantMsg = MessageEntity(
                    conversationId = getOrCreateConversationId(),
                    role = "assistant",
                    content = finalResponse
                )
                _uiState.update {
                    it.copy(
                        messages = it.messages + assistantMsg,
                        conversationState = ConversationState.Idle,
                        partialText = "",
                        responseText = ""
                    )
                }

                conversationRepository.insertMessage(userMsg)
                conversationRepository.insertMessage(assistantMsg)

                // Speak response aloud
                speak(finalResponse)

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        conversationState = ConversationState.Idle,
                        errorMessage = "发送失败：${e.message}"
                    )
                }
            }
        }
    }

    fun createVoiceIntent(): Intent {
        _uiState.update { it.copy(isListening = true) }
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "说吧...")
        }
    }

    private fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts_${System.currentTimeMillis()}")
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        tts?.shutdown()
    }

    private var conversationId: String? = null
    private fun getOrCreateConversationId(): String {
        if (conversationId == null) conversationId = UUID.randomUUID().toString()
        return conversationId!!
    }
}

// Extension for MutableStateFlow update
fun <T> MutableStateFlow<T>.update(transform: (T) -> T) {
    value = transform(value)
}
