package com.example.demodata.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demodata.data.local.entity.GpsGoogleEntity
import com.example.demodata.data.local.entity.GpsSensorsEntity
import com.example.demodata.data.repository.GpsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.ViewModelProvider

// Estructura intermedia para emparejar ambos registros en un mismo instante
data class ComparativeGpsRecord(
    val timestamp: Long,
    val google: GpsGoogleEntity?,
    val sensors: GpsSensorsEntity?
)

class GpsViewModel(private val gpsRepository: GpsRepository) : ViewModel() {

    val googlePoints = gpsRepository.googlePoints.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )

    val sensorsPoints = gpsRepository.sensorsPoints.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )

    // Combina ambos canales asíncronos en una sola lista unificada
    val comparativeHistory = combine(
        gpsRepository.googlePoints,
        gpsRepository.sensorsPoints
    ) { gList, sList ->
        val allTimestamps = (gList.map { it.timestamp } + sList.map { it.timestamp })
            .distinct()
            .sortedDescending()

        allTimestamps.map { ts ->
            ComparativeGpsRecord(
                timestamp = ts,
                google  = gList.find { it.timestamp == ts },
                sensors = sList.find { it.timestamp == ts }
            )
        }
    }
        .flowOn(Dispatchers.Default)   // Envía el cálculo pesado fuera del hilo de la UI
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    companion object {
        fun Factory(gpsRepository: GpsRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return GpsViewModel(gpsRepository) as T
                }
            }
    }
}