package com.aicompanion.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "memories",
    indices = [
        Index("category"),
        Index("importance"),
        Index("last_accessed_at"),
        Index("created_at")
    ]
)
data class MemoryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "content") val content: String,
    @ColumnInfo(name = "importance") val importance: Float = 0.5f,
    @ColumnInfo(name = "access_count") val accessCount: Int = 0,
    @ColumnInfo(name = "last_accessed_at") val lastAccessedAt: Long? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "source_message_id") val sourceMessageId: String? = null,
    @ColumnInfo(name = "source_conversation_id") val sourceConversationId: String? = null,
    @ColumnInfo(name = "is_active") val isActive: Boolean = true,
    @ColumnInfo(name = "decay_rate") val decayRate: Float = 0.01f,
    @ColumnInfo(name = "texture") val texture: String? = null
)
