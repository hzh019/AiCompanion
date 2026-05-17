package com.aicompanion.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "personalities")
data class PersonalityEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "character_name") val characterName: String,
    @ColumnInfo(name = "relationship") val relationship: String = "girlfriend",
    @ColumnInfo(name = "personality_traits") val personalityTraits: String = "",
    @ColumnInfo(name = "speaking_style") val speakingStyle: String = "",
    @ColumnInfo(name = "backstory") val backstory: String = "",
    @ColumnInfo(name = "user_nickname") val userNickname: String = "",
    @ColumnInfo(name = "language") val language: String = "zh-CN",
    @ColumnInfo(name = "voice_id") val voiceId: String? = null,
    @ColumnInfo(name = "system_prompt_override") val systemPromptOverride: String? = null,
    @ColumnInfo(name = "is_active") val isActive: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "avatar_uri") val avatarUri: String? = null
)
