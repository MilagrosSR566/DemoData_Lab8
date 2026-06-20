package com.example.demodata.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.demodata.DemoDataApp
import com.example.demodata.data.local.entity.GpsGoogleEntity
import com.example.demodata.data.local.entity.GpsSensorsEntity
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class GpsCaptureService : Service() {

    companion object {
        private const val INTERVAL_MS        = 10_000L
        private const val SENSOR_TIMEOUT_MS  = 5_000L
        private const val NOTIFICATION_ID    = 1001
        private const val CHANNEL_ID         = "gps_capture_channel"
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var captureJob: Job? = null

    // El repositorio ya existe en DemoDataApp — solo lo referenciamos
    private val gpsRepo by lazy { (application as DemoDataApp).gpsRepository }

    private val fusedClient by lazy {
        LocationServices.getFusedLocationProviderClient(applicationContext)
    }

    private val locationManager by lazy {
        getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
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

    private suspend fun performCaptures() {
        val now = System.currentTimeMillis()     // mismo timestamp para ambas fuentes

        // 1. Google FLP — siempre devuelve ubicación cuando hay conectividad
        try {
            val loc = fusedClient
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .await()                         // suspende sin bloquear el hilo
            loc?.let {
                gpsRepo.saveGooglePoint(GpsGoogleEntity(
                    latitude  = it.latitude,
                    longitude = it.longitude,
                    accuracy  = it.accuracy,
                    speed     = if (it.hasSpeed()) it.speed else null,
                    bearing   = if (it.hasBearing()) it.bearing else null,
                    timestamp = now
                ))
            }
        } catch (e: Exception) {
            // Manejo de excepciones en caso de revocación de permisos o fallas del proveedor
        }

        // 2. Sensor de Hardware puro — puede no tener fix; timeout de 5 s
        try {
            val sensorLoc = withTimeoutOrNull(SENSOR_TIMEOUT_MS) { getRawGpsLocation() }
            gpsRepo.saveSensorsPoint(GpsSensorsEntity(
                latitude  = sensorLoc?.latitude,   // null si se cumple el timeout
                longitude = sensorLoc?.longitude,
                provider  = LocationManager.GPS_PROVIDER,
                altitude  = if (sensorLoc?.hasAltitude() == true) sensorLoc.altitude else null,
                timestamp = now
            ))
        } catch (e: Exception) {
            // Failsafe pasivo
        }
    }

    // Adaptador suspendible para emular el flujo lineal sobre la API legacy de LocationManager
    @SuppressLint("MissingPermission")
    private suspend fun getRawGpsLocation(): Location? = suspendCancellableCoroutine { continuation ->
        val listener = android.location.LocationListener { location ->
            if (continuation.isActive) continuation.resume(location, null)
        }

        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0L,
                0f,
                listener,
                mainLooper
            )
        } catch (e: Exception) {
            if (continuation.isActive) continuation.resumeWith(Result.failure(e))
        }

        continuation.invokeOnCancellation {
            locationManager.removeUpdates(listener)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        captureJob?.cancel()     // Cancela de inmediato el bucle de la corutina al apagar el servicio
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Captura GNSS Activa")
            .setContentText("Registrando coordenadas en paralelo cada 10s...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Servicio GNSS",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                ?.createNotificationChannel(channel)
        }
    }
}