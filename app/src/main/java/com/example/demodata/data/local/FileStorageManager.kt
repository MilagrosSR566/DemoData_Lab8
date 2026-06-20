package com.example.demodata.data.local

import android.content.Context
import java.io.File

class FileStorageManager(private val context: Context) {

    private val photosDir = File(context.filesDir, "photos").apply { mkdirs() }
    private val videosDir = File(context.filesDir, "videos").apply { mkdirs() }
    private val audiosDir = File(context.filesDir, "audios").apply { mkdirs() }

    fun newPhotoFile(): File =
        File(photosDir, "photo_${System.currentTimeMillis()}.jpg")

    fun newVideoFile(): File =
        File(videosDir, "video_${System.currentTimeMillis()}.mp4")

    fun newAudioFile(extension: String = "m4a"): File =
        File(audiosDir, "audio_${System.currentTimeMillis()}.$extension")

    fun deleteFile(path: String): Boolean =
        File(path).takeIf { it.exists() }?.delete() ?: false

    fun fileSize(path: String): Long = File(path).length()
}