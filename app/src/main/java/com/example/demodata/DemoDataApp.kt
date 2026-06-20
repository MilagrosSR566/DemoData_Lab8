package com.example.demodata

import android.app.Application
import com.example.demodata.data.local.FileStorageManager
import com.example.demodata.data.local.DemoDataDatabase
import com.example.demodata.data.repository.AudioRepository
import com.example.demodata.data.repository.GpsRepository
import com.example.demodata.data.repository.MediaRepository
import com.example.demodata.data.session.SessionManager

class DemoDataApp : Application() {

    val database     by lazy { DemoDataDatabase.getInstance(this) }
    val fileStorage  by lazy { FileStorageManager(this) }
    val sessionManager by lazy { SessionManager(this) }

    val gpsRepository by lazy {
        GpsRepository(database.gpsGoogleDao(), database.gpsSensorsDao())
    }
    val mediaRepository by lazy {
        MediaRepository(database.mediaDao(), fileStorage)
    }
    val audioRepository by lazy {
        AudioRepository(database.audioDao(), fileStorage)
    }
}