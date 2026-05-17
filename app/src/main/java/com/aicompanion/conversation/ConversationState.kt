package com.aicompanion.conversation

sealed interface ConversationState {
    /** Idle — waiting for user to start speaking */
    data object Idle : ConversationState

    /** Actively listening to user's voice */
    data object Listening : ConversationState

    /** Processing audio — VAD detection, speaker verification, STT */
    data object Processing : ConversationState

    /** LLM is generating a response */
    data object Thinking : ConversationState

    /** Playing TTS audio response */
    data object Speaking : ConversationState

    /** An error occurred */
    data class Error(val message: String) : ConversationState
}
