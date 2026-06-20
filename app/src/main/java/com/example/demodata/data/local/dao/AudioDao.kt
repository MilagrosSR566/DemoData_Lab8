package com.example.demodata.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.demodata.data.local.entity.AudioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioDao {

    @Insert
    suspend fun insert(item: AudioEntity): Long

    @Query("SELECT * FROM audio ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<AudioEntity>>

    @Query("SELECT COUNT(*) FROM audio")
    fun observeCount(): Flow<Int>

    @Delete
    suspend fun delete(item: AudioEntity)
}