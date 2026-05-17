package com.aicompanion.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "proactive_triggers")
data class ProactiveTriggerEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "trigger_type") val triggerType: String,
    @ColumnInfo(name = "config_json") val configJson: String,
    @ColumnInfo(name = "prompt_template") val promptTemplate: String,
    @ColumnInfo(name = "notification_title") val notificationTitle: String = "",
    @ColumnInfo(name = "notification_body_preview") val notificationBodyPreview: String = "",
    @ColumnInfo(name = "priority") val priority: Int = 5,
    @ColumnInfo(name = "is_enabled") val isEnabled: Boolean = true,
    @ColumnInfo(name = "last_triggered_at") val lastTriggeredAt: Long? = null,
    @ColumnInfo(name = "cooldown_minutes") val cooldownMinutes: Int = 60
)
