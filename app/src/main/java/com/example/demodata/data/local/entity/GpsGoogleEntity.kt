package com.example.demodata.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gps_google")
data class GpsGoogleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val latitude: Double?,      // null si FLP no logra fijar posición
    val longitude: Double?,     // null si FLP no logra fijar posición
    val accuracy: Float?,       // precisión horizontal en metros; null si no disponible
    val speed: Float? = null,   // m/s — null si el dispositivo no lo reporta
    val bearing: Float? = null, // grados desde el norte
    val timestamp: Long         // System.currentTimeMillis() en UTC
)
