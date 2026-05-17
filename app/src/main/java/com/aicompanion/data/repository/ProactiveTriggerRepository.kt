package com.aicompanion.data.repository

import com.aicompanion.data.local.dao.ProactiveTriggerDao
import com.aicompanion.data.local.entity.ProactiveTriggerEntity
import kotlinx.coroutines.flow.Flow

class ProactiveTriggerRepository(private val triggerDao: ProactiveTriggerDao) {

    fun getAllEnabled(): Flow<List<ProactiveTriggerEntity>> {
        return triggerDao.getAllEnabled()
    }

    suspend fun insert(trigger: ProactiveTriggerEntity) {
        triggerDao.insert(trigger)
    }

    suspend fun update(trigger: ProactiveTriggerEntity) {
        triggerDao.update(trigger)
    }

    suspend fun delete(trigger: ProactiveTriggerEntity) {
        triggerDao.delete(trigger)
    }
}
