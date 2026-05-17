package com.aicompanion.data.repository

import com.aicompanion.data.local.dao.MemoryDao
import com.aicompanion.data.local.entity.MemoryEntity
import kotlinx.coroutines.flow.Flow

class MemoryRepository(private val memoryDao: MemoryDao) {

    fun getAllActive(): Flow<List<MemoryEntity>> {
        return memoryDao.getAllActive()
    }

    suspend fun searchByCategory(category: String): List<MemoryEntity> {
        return memoryDao.searchByCategory(category)
    }

    suspend fun searchByContent(keyword: String): List<MemoryEntity> {
        return memoryDao.searchByContent(keyword)
    }

    suspend fun getTopByImportance(limit: Int): List<MemoryEntity> {
        return memoryDao.getTopByImportance(limit)
    }

    suspend fun insert(memory: MemoryEntity) {
        memoryDao.insert(memory)
    }

    suspend fun update(memory: MemoryEntity) {
        memoryDao.update(memory)
    }

    suspend fun softDelete(id: String) {
        memoryDao.softDelete(id)
    }

    suspend fun getMemoriesNeedingConsolidation(threshold: Long): List<MemoryEntity> {
        return memoryDao.getMemoriesNeedingConsolidation(threshold)
    }
}
