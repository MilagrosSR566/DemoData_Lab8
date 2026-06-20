package com.example.demodata.data.repository

import com.example.demodata.data.local.FileStorageManager
import com.example.demodata.data.local.dao.MediaDao
import com.example.demodata.data.local.entity.MediaEntity
import com.example.demodata.data.local.entity.MediaType
import kotlinx.coroutines.flow.Flow

class MediaRepository(
    private val mediaDao: MediaDao,
    private val fileStorage: FileStorageManager
) {
    val allMedia: Flow<List<MediaEntity>> = mediaDao.observeAll()
    val photoCount: Flow<Int> = mediaDao.observePhotoCount()
    val videoCount: Flow<Int> = mediaDao.observeVideoCount()

    suspend fun registerPhoto(
        filePath: String,
        widthPx: Int,
        heightPx: Int
    ): Long = mediaDao.insert(
        MediaEntity(
            filePath  = filePath,
            type      = MediaType.PHOTO.name,
            sizeBytes = fileStorage.fileSize(filePath),
            widthPx   = widthPx,
            heightPx  = heightPx,
            timestamp = System.currentTimeMillis()
        )
    )

    suspend fun registerVideo(
        filePath: String,
        durationMs: Long
    ): Long = mediaDao.insert(
        MediaEntity(
            filePath   = filePath,
            type       = MediaType.VIDEO.name,
            sizeBytes  = fileStorage.fileSize(filePath),
            durationMs = durationMs,
            timestamp  = System.currentTimeMillis()
        )
    )

    suspend fun delete(item: MediaEntity) {
        fileStorage.deleteFile(item.filePath)
        mediaDao.delete(item)
    }
}