package com.aicompanion.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.aicompanion.data.local.entity.MemoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {

    @Query("SELECT * FROM memories WHERE is_active = 1 ORDER BY importance DESC, created_at DESC")
    fun getAllActive(): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories WHERE category = :category AND is_active = 1")
    suspend fun searchByCategory(category: String): List<MemoryEntity>

    @Query("SELECT * FROM memories WHERE content LIKE '%' || :keyword || '%' AND is_active = 1")
    suspend fun searchByContent(keyword: String): List<MemoryEntity>

    @Query("SELECT * FROM memories WHERE is_active = 1 ORDER BY importance DESC LIMIT :limit")
    suspend fun getTopByImportance(limit: Int): List<MemoryEntity>

    @Insert
    suspend fun insert(memory: MemoryEntity)

    @Update
    suspend fun update(memory: MemoryEntity)

    @Query("UPDATE memories SET is_active = 0 WHERE id = :id")
    suspend fun softDelete(id: String)

    @Query("SELECT * FROM memories WHERE is_active = 1 AND (last_accessed_at IS NULL OR last_accessed_at < :threshold)")
    suspend fun getMemoriesNeedingConsolidation(threshold: Long): List<MemoryEntity>
}
