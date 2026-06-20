package com.example.demodata.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media")
data class MediaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val filePath: String,          // ruta absoluta dentro de filesDir
    val type: String,              // "PHOTO" o "VIDEO" (usar MediaType.name)
    val sizeBytes: Long,
    val durationMs: Long? = null,  // solo videos; null para fotos
    val widthPx: Int? = null,
    val heightPx: Int? = null,
    val timestamp: Long
)

enum class MediaType { PHOTO, VIDEO }