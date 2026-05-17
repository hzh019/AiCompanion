package com.aicompanion.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aicompanion.conversation.ConversationManager
import com.aicompanion.conversation.ConversationState
import com.aicompanion.data.local.entity.MessageEntity
import com.aicompanion.data.repository.ConversationRepository
import com.aicompanion.data.repository.MemoryRepository
import com.aicompanion.llm.DeepSeekClient
import com.aicompanion.llm.PromptBuilder
import com.aicompanion.personality.EmotionInjector
import com.aicompanion.personality.PersonalityManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ConversationUiState(
    val conversationState: ConversationState = ConversationState.Idle,
    val partialText: String = "",
    val responseText: String = "",
    val messages: List<MessageEntity> = emptyList(),
    val isAlwaysListening: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ConversationViewModel @Inject constructor(
    private val conversationManager: ConversationManager,
    private val deepSeekClient: DeepSeekClient,
    private val promptBuilder: PromptBuilder,
    private val personalityManager: PersonalityManager,
    private val memoryRepository: MemoryRepository,
    private val conversationRepository: ConversationRepository,
    private val emotionInjector: EmotionInjector
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConversationUiState())
    val uiState: StateFlow<ConversationUiState> = _uiState.asStateFlow()

    private val _textInput = MutableStateFlow("")
    val textInput: StateFlow<String> = _textInput.asStateFlow()

    init {
        // Observe conversation manager state
        viewModelScope.launch {
            conversationManager.state.collect { state ->
                _uiState.update { it.copy(conversationState = state) }
            }
        }
        viewModelScope.launch {
            conversationManager.partialText.collect { text ->
                _uiState.update { it.copy(partialText = text) }
            }
        }
        viewModelScope.launch {
            conversationManager.responseText.collect { text ->
                _uiState.update { it.copy(responseText = text) }
            }
        }

        // Ensure default personality exists
        viewModelScope.launch {
            personalityManager.ensureDefaultPersonality()
        }
    }

    fun updateTextInput(text: String) {
        _textInput.value = text
    }

    fun sendTextMessage() {
        val text = _textInput.value.trim()
        if (text.isBlank()) return

        _textInput.value = ""
        _uiState.update { it.copy(partialText = text, errorMessage = null) }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(conversationState = ConversationState.Thinking as ConversationState) }

                val personality = personalityManager.getActivePersonality()
                val systemPrompt = if (personality != null) {
                    val memories = memoryRepository.getTopByImportance(5)
                    promptBuilder.buildSystemPrompt(personality, memories)
                } else {
                    "You are a caring AI companion. Respond naturally in Chinese."
                }

                // Build messages from current chat history
                val recentMessages = _uiState.value.messages.takeLast(20)
                val chatMessages = promptBuilder.buildMessages(recentMessages, text)

                // Save user message
                val userMsg = MessageEntity(
                    conversationId = getOrCreateConversationId(),
                    role = "user",
                    content = text
                )
                _uiState.update { it.copy(messages = it.messages + userMsg) }

                // Stream LLM response
                val responseBuilder = StringBuilder()
                deepSeekClient.chatCompletionStream(systemPrompt, chatMessages)
                    .collect { token ->
                        responseBuilder.append(token)
                        _uiState.update { it.copy(responseText = responseBuilder.toString()) }
                    }

                val finalResponse = responseBuilder.toString().ifBlank { "嗯..." }

                // Save assistant message
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

                // Persist to database
                conversationRepository.insertMessage(userMsg)
                conversationRepository.insertMessage(assistantMsg)

                // Detect emotion
                val emotion = emotionInjector.detectEmotion(text)
                if (emotion.confidence > 0.5f) {
                    // Could add emotion indicator to UI
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        conversationState = ConversationState.Error("发送失败：${e.message}"),
                        errorMessage = "发送失败：${e.message}"
                    )
                }
            }
        }
    }

    fun startVoiceMode() {
        conversationManager.startListening(alwaysListening = false)
    }

    fun stopVoiceMode() {
        conversationManager.stopListening()
    }

    fun toggleAlwaysListening() {
        conversationManager.toggleAlwaysListening()
        _uiState.update { it.copy(isAlwaysListening = !it.isAlwaysListening) }
    }

    fun interruptSpeaking() {
        conversationManager.interrupt()
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private var conversationId: String? = null
    private fun getOrCreateConversationId(): String {
        if (conversationId == null) {
            conversationId = UUID.randomUUID().toString()
        }
        return conversationId!!
    }
}

// Extension for MutableStateFlow update
fun <T> MutableStateFlow<T>.update(transform: (T) -> T) {
    value = transform(value)
}
