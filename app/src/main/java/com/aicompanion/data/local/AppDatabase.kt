package com.aicompanion.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aicompanion.data.local.dao.ConversationDao
import com.aicompanion.data.local.dao.MemoryDao
import com.aicompanion.data.local.dao.MessageDao
import com.aicompanion.data.local.dao.PersonalityDao
import com.aicompanion.data.local.dao.ProactiveTriggerDao
import com.aicompanion.data.local.dao.VoiceProfileDao
import com.aicompanion.data.local.entity.ConversationEntity
import com.aicompanion.data.local.entity.MemoryEntity
import com.aicompanion.data.local.entity.MessageEntity
import com.aicompanion.data.local.entity.PersonalityEntity
import com.aicompanion.data.local.entity.ProactiveTriggerEntity
import com.aicompanion.data.local.entity.VoiceProfileEntity

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        MemoryEntity::class,
        VoiceProfileEntity::class,
        PersonalityEntity::class,
        ProactiveTriggerEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun memoryDao(): MemoryDao
    abstract fun voiceProfileDao(): VoiceProfileDao
    abstract fun personalityDao(): PersonalityDao
    abstract fun proactiveTriggerDao(): ProactiveTriggerDao
}
