package com.aicompanion.di

import androidx.room.Room
import com.aicompanion.data.local.AppDatabase
import com.aicompanion.data.local.dao.ConversationDao
import com.aicompanion.data.local.dao.MemoryDao
import com.aicompanion.data.local.dao.MessageDao
import com.aicompanion.data.local.dao.PersonalityDao
import com.aicompanion.data.local.dao.ProactiveTriggerDao
import com.aicompanion.data.local.dao.VoiceProfileDao
import com.aicompanion.data.repository.ConversationRepository
import com.aicompanion.data.repository.MemoryRepository
import com.aicompanion.data.repository.PersonalityRepository
import com.aicompanion.data.repository.VoiceProfileRepository
import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(application: Application): AppDatabase =
        Room.databaseBuilder(
            application,
            AppDatabase::class.java,
            "ai_companion.db"
        )
            .fallbackToDestructiveMigration()
            .build()

    // --- DAOs ---

    @Provides
    @Singleton
    fun provideConversationDao(database: AppDatabase): ConversationDao =
        database.conversationDao()

    @Provides
    @Singleton
    fun provideMessageDao(database: AppDatabase): MessageDao =
        database.messageDao()

    @Provides
    @Singleton
    fun provideMemoryDao(database: AppDatabase): MemoryDao =
        database.memoryDao()

    @Provides
    @Singleton
    fun provideVoiceProfileDao(database: AppDatabase): VoiceProfileDao =
        database.voiceProfileDao()

    @Provides
    @Singleton
    fun providePersonalityDao(database: AppDatabase): PersonalityDao =
        database.personalityDao()

    @Provides
    @Singleton
    fun provideProactiveTriggerDao(database: AppDatabase): ProactiveTriggerDao =
        database.proactiveTriggerDao()

    // --- Repositories ---

    @Provides
    @Singleton
    fun provideConversationRepository(
        conversationDao: ConversationDao,
        messageDao: MessageDao
    ): ConversationRepository =
        ConversationRepository(conversationDao, messageDao)

    @Provides
    @Singleton
    fun provideMemoryRepository(dao: MemoryDao): MemoryRepository =
        MemoryRepository(dao)

    @Provides
    @Singleton
    fun providePersonalityRepository(dao: PersonalityDao): PersonalityRepository =
        PersonalityRepository(dao)

    @Provides
    @Singleton
    fun provideVoiceProfileRepository(dao: VoiceProfileDao): VoiceProfileRepository =
        VoiceProfileRepository(dao)
}
