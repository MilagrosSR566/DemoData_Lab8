package com.example.demodata.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.demodata.data.local.dao.AudioDao
import com.example.demodata.data.local.dao.GpsGoogleDao
import com.example.demodata.data.local.dao.GpsSensorsDao
import com.example.demodata.data.local.dao.MediaDao
import com.example.demodata.data.local.entity.AudioEntity
import com.example.demodata.data.local.entity.GpsGoogleEntity
import com.example.demodata.data.local.entity.GpsSensorsEntity
import com.example.demodata.data.local.entity.MediaEntity

@Database(
    entities = [
        GpsGoogleEntity::class,
        GpsSensorsEntity::class,
        MediaEntity::class,
        AudioEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class DemoDataDatabase : RoomDatabase() {

    abstract fun gpsGoogleDao(): GpsGoogleDao
    abstract fun gpsSensorsDao(): GpsSensorsDao
    abstract fun mediaDao(): MediaDao
    abstract fun audioDao(): AudioDao

    companion object {
        @Volatile private var INSTANCE: DemoDataDatabase? = null

        fun getInstance(context: Context): DemoDataDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    DemoDataDatabase::class.java,
                    "fleet.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}