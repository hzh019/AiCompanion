package com.aicompanion.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.aicompanion.data.local.entity.VoiceProfileEntity

@Dao
interface VoiceProfileDao {

    @Query("SELECT * FROM voice_profiles WHERE is_active = 1 LIMIT 1")
    suspend fun getActiveProfile(): VoiceProfileEntity?

    @Query("SELECT * FROM voice_profiles WHERE id = :id")
    suspend fun getById(id: String): VoiceProfileEntity?

    @Insert
    suspend fun insert(profile: VoiceProfileEntity)

    @Update
    suspend fun update(profile: VoiceProfileEntity)

    @Delete
    suspend fun delete(profile: VoiceProfileEntity)

    @Query("UPDATE voice_profiles SET is_active = 0")
    suspend fun clearAllActive()

    @Query("UPDATE voice_profiles SET is_active = 1 WHERE id = :id")
    suspend fun setActive(id: String)
}
