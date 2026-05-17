package com.aicompanion.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.aicompanion.data.local.entity.PersonalityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonalityDao {

    @Query("SELECT * FROM personalities WHERE is_active = 1 LIMIT 1")
    suspend fun getActive(): PersonalityEntity?

    @Query("SELECT * FROM personalities ORDER BY created_at DESC")
    fun getAll(): Flow<List<PersonalityEntity>>

    @Insert
    suspend fun insert(personality: PersonalityEntity)

    @Update
    suspend fun update(personality: PersonalityEntity)

    @Delete
    suspend fun delete(personality: PersonalityEntity)

    @Query("UPDATE personalities SET is_active = 0")
    suspend fun clearAllActive()

    @Query("UPDATE personalities SET is_active = 1 WHERE id = :id")
    suspend fun setActive(id: String)
}
