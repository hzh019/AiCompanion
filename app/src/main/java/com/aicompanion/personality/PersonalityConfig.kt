package com.aicompanion.personality

data class PersonalityConfig(
    val name: String = "",
    val characterName: String = "",
    val relationship: String = "girlfriend",
    val personalityTraits: String = "",
    val speakingStyle: String = "",
    val backstory: String = "",
    val userNickname: String = "",
    val language: String = "zh-CN",
    val voiceId: String? = null,
    val systemPromptOverride: String? = null
)
