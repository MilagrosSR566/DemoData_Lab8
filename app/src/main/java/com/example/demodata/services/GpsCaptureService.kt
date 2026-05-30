package com.example.demodata.services

import android.annotation.SuppressLint
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import com.example.demodata.DemoDataApp
import com.example.demodata.data.local.entity.GpsGoogleEntity
import com.example.demodata.data.local.entity.GpsSensorsEntity
import com.example.demodata.data.repository.GpsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

class GpsCaptureService : Service() {

    companion object {
        private const val INTERVAL_MS = 10_000L
        private const val SENSOR_TIMEOUT_MS = 5_000L
        private const val CHANNEL_ID = "gps_capture_channel"
        private const val NOTIFICATION_ID = 12345
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var captureJob: Job? = null

    private val gpsRepo by lazy { (application as DemoDataApp).gpsRepository }

    private val fusedClient by lazy { LocationServices.getFusedLocationProviderClient(this) }
    private val locationManager by lazy { getSystemService(Context.LOCATION_SERVICE) as LocationManager }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!hasLocationPermission()) { stopSelf(); return START_NOT_STICKY }

        if (captureJob == null) {
            captureJob = scope.launch {
                while (isActive) {
                    performCaptures()
                    delay(INTERVAL_MS)
                }
            }
        }
        return START_STICKY
    }

    // Agregamos la anotación para decirle al compilador que los permisos ya están validados estructuralmente
    @SuppressLint("MissingPermission")
    private suspend fun performCaptures() {
        val now = System.currentTimeMillis()

        // 1. Google FLP
        try {
            if (hasLocationPermission()) {
                val tokenSource = CancellationTokenSource()
                val loc: Location? = fusedClient
                    .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, tokenSource.token)
                    .await()

                loc?.let { locationItem ->
                    gpsRepo.saveGooglePoint(GpsGoogleEntity(
                        latitude = locationItem.latitude,
                        longitude = locationItem.longitude,
                        accuracy = locationItem.accuracy,
                        speed    = if (locationItem.hasSpeed()) locationItem.speed else null,
                        bearing  = if (locationItem.hasBearing()) locationItem.bearing else null,
                        timestamp = now
                    ))
                }
            }
        } catch (e: Exception) { }

        // 2. Sensor GNSS
        try {
            val sensorLoc: Location? = withTimeoutOrNull(SENSOR_TIMEOUT_MS) { getRawGpsLocation() }
            gpsRepo.saveSensorsPoint(GpsSensorsEntity(
                latitude  = sensorLoc?.latitude,
                longitude = sensorLoc?.longitude,
                provider  = LocationManager.GPS_PROVIDER,
                altitude  = if (sensorLoc?.hasAltitude() == true) sensorLoc.altitude else null,
                timestamp = now
            ))
        } catch (e: Exception) { }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getRawGpsLocation(): Location? = suspendCancellableCoroutine { continuation ->
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                locationManager.removeUpdates(this)
                if (continuation.isActive) continuation.resume(location)
            }
            override fun onProviderDisabled(provider: String) {
                locationManager.removeUpdates(this)
                if (continuation.isActive) continuation.resume(null)
            }
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        }

        if (hasLocationPermission()) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, listener)
        } else {
            continuation.resume(null)
        }

        continuation.invokeOnCancellation {
            locationManager.removeUpdates(listener)
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun createNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("Captura GNSS Activa")
        .setContentText("Registrando trayectorias en segundo plano...")
        .setSmallIcon(android.R.drawable.ic_menu_mylocation)
        .setOngoing(true)
        .build()

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "GPS Capture", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        captureJob?.cancel()
    }
}