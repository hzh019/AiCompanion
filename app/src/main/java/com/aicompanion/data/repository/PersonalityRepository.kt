package com.aicompanion.data.repository

import com.aicompanion.data.local.dao.PersonalityDao
import com.aicompanion.data.local.entity.PersonalityEntity
import kotlinx.coroutines.flow.Flow

class PersonalityRepository(private val personalityDao: PersonalityDao) {

    suspend fun getActive(): PersonalityEntity? {
        return personalityDao.getActive()
    }

    fun getAll(): Flow<List<PersonalityEntity>> {
        return personalityDao.getAll()
    }

    suspend fun insert(personality: PersonalityEntity) {
        personalityDao.insert(personality)
    }

    suspend fun update(personality: PersonalityEntity) {
        personalityDao.update(personality)
    }

    suspend fun delete(personality: PersonalityEntity) {
        personalityDao.delete(personality)
    }

    suspend fun setActive(id: String) {
        personalityDao.clearAllActive()
        personalityDao.setActive(id)
    }
}
