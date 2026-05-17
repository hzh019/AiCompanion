package com.aicompanion.llm

data class ConversationTurn(
    val userInput: String,
    val assistantResponse: String,
    val emotions: List<String> = emptyList()
)

data class ExtractedMemory(
    val category: String,
    val content: String,
    val importance: Float = 0.5f
)
