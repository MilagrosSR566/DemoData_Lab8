package com.example.demodata.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.example.demodata.ui.viewmodel.GpsViewModel
import com.example.demodata.ui.viewmodel.ComparativeGpsRecord
import com.example.demodata.services.GpsCaptureService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GpsScreen(viewModel: GpsViewModel) {
    val context = LocalContext.current

    // Configuración de permisos reactivos
    val permisos = buildList {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    val estadoPermisos = rememberMultiplePermissionsState(permissions = permisos)

    // Estados observados desde el ViewModel de forma segura con el ciclo de vida
    val googlePoints by viewModel.googlePoints.collectAsStateWithLifecycle()
    val sensorsPoints by viewModel.sensorsPoints.collectAsStateWithLifecycle()
    val history by viewModel.comparativeHistory.collectAsStateWithLifecycle()

    // Estado local para saber si el servicio está corriendo (capturando)
    var capturando by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Monitoreo GNSS Comparativo",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 1. Bloqueo temprano si faltan permisos requeridos
        if (!estadoPermisos.allPermissionsGranted) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Se requieren permisos de ubicación y notificaciones para continuar.",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Button(onClick = { estadoPermisos.launchMultiplePermissionRequest() }) {
                        Text("Conceder permisos")
                    }
                }
            }
            return // Detiene el dibujado del resto de la pantalla
        }

        // 2. Botón interactivo de control de servicio en primer plano
        Button(
            onClick = {
                val intent = Intent(context, GpsCaptureService::class.java)
                if (capturando) {
                    context.stopService(intent)
                    capturando = false
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(intent)
                    } else {
                        context.startService(intent)
                    }
                    capturando = true
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (capturando) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Icon(if (capturando) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (capturando) "Detener captura" else "Capturar coordenada (cada 10 s)")
        }

        // 3. Tarjetas informativas superiores (Contadores)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Google FLP", fontWeight = FontWeight.Bold)
                    Text("${googlePoints.size}", fontSize = 24.sp, fontWeight = FontWeight.Black)
                    Text("registros", fontSize = 12.sp)
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Sensores GNSS", fontWeight = FontWeight.Bold)
                    Text("${sensorsPoints.size}", fontSize = 24.sp, fontWeight = FontWeight.Black)
                    Text("registros", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Historial Reactivo Comparativo
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items = history, key = { it.timestamp }) { record ->
                ComparativeCaptureCard(record, dateFormat)
            }
        }
    }
}

@Composable
fun ComparativeCaptureCard(record: ComparativeGpsRecord, dateFormat: SimpleDateFormat) {
    val horaStr = remember(record.timestamp) { dateFormat.format(Date(record.timestamp)) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Cabecera del instante
            Text(
                text = "Instante: $horaStr",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Divider(modifier = Modifier.padding(bottom = 8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                // Panel Izquierdo: Google FLP
                Column(modifier = Modifier.weight(1f).padding(end = 4.dp)) {
                    Text("GOOGLE FLP", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    if (record.google != null) {
                        Text("Lat: ${record.google.latitude}", fontSize = 13.sp)
                        Text("Lon: ${record.google.longitude}", fontSize = 13.sp)
                        Text("Acc: ±${record.google.accuracy ?: 0}m", fontSize = 11.sp, color = Color.Gray)
                    } else {
                        Text("SIN SEÑAL", fontSize = 13.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                }

                // Panel Derecho: Sensores puros de Hardware
                Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                    Text("SENSOR GNSS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    if (record.sensors?.latitude != null) {
                        Text("Lat: ${record.sensors.latitude}", fontSize = 13.sp)
                        Text("Lon: ${record.sensors.longitude}", fontSize = 13.sp)
                        Text("Alt: ${record.sensors.altitude ?: 0.0}m", fontSize = 11.sp, color = Color.Gray)
                    } else {
                        Text("SIN SEÑAL", fontSize = 13.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        Text("No satellite fix", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}