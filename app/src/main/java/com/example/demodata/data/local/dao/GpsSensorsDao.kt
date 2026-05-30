package com.example.demodata.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.demodata.data.local.entity.GpsSensorsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GpsSensorsDao {
    @Insert
    suspend fun insert(item: GpsSensorsEntity): Long

    @Query("SELECT * FROM gps_sensors ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<GpsSensorsEntity>>

    @Query("SELECT COUNT(*) FROM gps_sensors")
    fun observeCount(): Flow<Int>

    @Query("DELETE FROM gps_sensors")
    suspend fun deleteAll()
}