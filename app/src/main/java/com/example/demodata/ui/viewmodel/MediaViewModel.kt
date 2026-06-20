package com.example.demodata.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.demodata.data.local.FileStorageManager
import com.example.demodata.data.local.entity.MediaEntity
import com.example.demodata.data.repository.MediaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class MediaViewModel(
    private val mediaRepository: MediaRepository,
    private val fileStorage: FileStorageManager
) : ViewModel() {

    val mediaList = mediaRepository.allMedia.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    val photoCount = mediaRepository.photoCount.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        0
    )

    val videoCount = mediaRepository.videoCount.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        0
    )

    fun newPhotoFile(): File = fileStorage.newPhotoFile()
    fun newVideoFile(): File = fileStorage.newVideoFile()

    fun onPhotoCaptured(filePath: String, widthPx: Int, heightPx: Int) {
        viewModelScope.launch {
            mediaRepository.registerPhoto(filePath, widthPx, heightPx)
        }
    }

    fun onVideoCaptured(filePath: String, durationMs: Long) {
        viewModelScope.launch {
            mediaRepository.registerVideo(filePath, durationMs)
        }
    }

    fun delete(item: MediaEntity) {
        viewModelScope.launch {
            mediaRepository.delete(item)
        }
    }

    class Factory(
        private val mediaRepository: MediaRepository,
        private val fileStorage: FileStorageManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MediaViewModel(mediaRepository, fileStorage) as T
    }
}