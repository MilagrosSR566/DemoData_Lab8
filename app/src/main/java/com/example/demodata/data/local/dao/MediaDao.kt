package com.example.demodata.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.demodata.data.local.entity.MediaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {

    @Insert
    suspend fun insert(item: MediaEntity): Long

    @Query("SELECT * FROM media ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media WHERE type = :type ORDER BY timestamp DESC")
    fun observeByType(type: String): Flow<List<MediaEntity>>

    @Query("SELECT COUNT(*) FROM media WHERE type = 'PHOTO'")
    fun observePhotoCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM media WHERE type = 'VIDEO'")
    fun observeVideoCount(): Flow<Int>

    @Delete
    suspend fun delete(item: MediaEntity)
}