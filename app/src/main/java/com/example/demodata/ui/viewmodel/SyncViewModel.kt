package com.example.demodata.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.demodata.data.repository.AudioRepository
import com.example.demodata.data.repository.GpsRepository
import com.example.demodata.data.repository.MediaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class SyncCounts(
    val gpsGoogle: Int = 0,
    val gpsSensors: Int = 0,
    val photos: Int    = 0,
    val videos: Int    = 0,
    val audios: Int    = 0
) {
    val total: Int get() = gpsGoogle + gpsSensors + photos + videos + audios
}

class SyncViewModel(
    gpsRepository:   GpsRepository,
    mediaRepository: MediaRepository,
    audioRepository: AudioRepository
) : ViewModel() {

    val counts = combine(
        gpsRepository.googleCount,
        gpsRepository.sensorsCount,
        mediaRepository.photoCount,
        mediaRepository.videoCount,
        audioRepository.count
    ) { g, s, p, v, a ->
        SyncCounts(g, s, p, v, a)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        SyncCounts()
    )

    class Factory(
        private val gps:   GpsRepository,
        private val media: MediaRepository,
        private val audio: AudioRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SyncViewModel(gps, media, audio) as T
    }
}