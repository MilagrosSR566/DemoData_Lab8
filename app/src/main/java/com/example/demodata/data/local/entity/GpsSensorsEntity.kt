package com.example.demodata.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gps_sensors")
data class GpsSensorsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val latitude: Double?,      // NULLABLE: null cuando no hay fix satelital
    val longitude: Double?,     // NULLABLE
    val provider: String,       // "gps", "network" o "passive"
    val altitude: Double? = null,
    val satellites: Int? = null,
    val timestamp: Long
)