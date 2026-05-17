package com.aicompanion.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.aicompanion.data.local.entity.ProactiveTriggerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProactiveTriggerDao {

    @Query("SELECT * FROM proactive_triggers WHERE is_enabled = 1 ORDER BY priority DESC")
    fun getAllEnabled(): Flow<List<ProactiveTriggerEntity>>

    @Insert
    suspend fun insert(trigger: ProactiveTriggerEntity)

    @Update
    suspend fun update(trigger: ProactiveTriggerEntity)

    @Delete
    suspend fun delete(trigger: ProactiveTriggerEntity)
}
