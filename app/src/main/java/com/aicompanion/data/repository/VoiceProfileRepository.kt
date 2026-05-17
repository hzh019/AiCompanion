package com.aicompanion.data.repository

import com.aicompanion.data.local.dao.VoiceProfileDao
import com.aicompanion.data.local.entity.VoiceProfileEntity

class VoiceProfileRepository(private val voiceProfileDao: VoiceProfileDao) {

    suspend fun getActiveProfile(): VoiceProfileEntity? {
        return voiceProfileDao.getActiveProfile()
    }

    suspend fun getById(id: String): VoiceProfileEntity? {
        return voiceProfileDao.getById(id)
    }

    suspend fun insert(profile: VoiceProfileEntity) {
        voiceProfileDao.insert(profile)
    }

    suspend fun update(profile: VoiceProfileEntity) {
        voiceProfileDao.update(profile)
    }

    suspend fun delete(profile: VoiceProfileEntity) {
        voiceProfileDao.delete(profile)
    }

    // Alias methods
    suspend fun insertProfile(profile: VoiceProfileEntity) = insert(profile)
    suspend fun setActiveProfile(id: String) {
        voiceProfileDao.clearAllActive()
        voiceProfileDao.setActive(id)
    }
}
