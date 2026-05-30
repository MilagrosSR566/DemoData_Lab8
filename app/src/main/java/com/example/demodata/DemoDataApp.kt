package com.example.demodata

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.demodata.data.local.dao.GpsGoogleDao
import com.example.demodata.data.local.dao.GpsSensorsDao
import com.example.demodata.data.local.entity.GpsGoogleEntity
import com.example.demodata.data.local.entity.GpsSensorsEntity
import com.example.demodata.data.repository.GpsRepository
import com.example.demodata.data.session.SessionManager

// Definición rápida de la base de datos central de Room
@Database(entities = [GpsGoogleEntity::class, GpsSensorsEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gpsGoogleDao(): GpsGoogleDao
    abstract fun gpsSensorsDao(): GpsSensorsDao
}

class DemoDataApp : Application() {

    // Instancia única de la base de datos
    private val database by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "demodata_db")
            .fallbackToDestructiveMigration()
            .build()
    }

    // Instancias globales inyectadas de forma manual
    val gpsRepository by lazy {
        GpsRepository(database.gpsGoogleDao(), database.gpsSensorsDao())
    }

    val sessionManager by lazy {
        SessionManager(this)
    }
}