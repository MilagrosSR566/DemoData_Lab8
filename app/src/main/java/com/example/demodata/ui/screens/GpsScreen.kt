package com.example.demodata.ui.screens

import android.Manifest
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.demodata.data.local.entity.GpsGoogleEntity
import com.example.demodata.data.local.entity.GpsSensorsEntity
import com.example.demodata.ui.viewmodel.ComparativeGpsRecord
import com.example.demodata.ui.viewmodel.GpsViewModel
import com.example.demodata.services.GpsCaptureService
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GpsScreen(viewModel: GpsViewModel) {
    val context = LocalContext.current

    // Lista de permisos requeridos por el laboratorio
    val permisos = buildList {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    val estadoPermisos = rememberMultiplePermissionsState(permissions = permisos)

    // Estado local para saber si el servicio está corriendo
    var capturando by remember { mutableStateOf(false) }

    // Recolectamos los datos reactivos del ViewModel
    val googlePoints  by viewModel.googlePoints.collectAsStateWithLifecycle()
    val sensorsPoints by viewModel.sensorsPoints.collectAsStateWithLifecycle()
    val history       by viewModel.comparativeHistory.collectAsStateWithLifecycle()

    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Bloqueo temprano: si no hay permisos mostramos Card de error y retornamos
        if (!estadoPermisos.allPermissionsGranted) {
            Card(
                colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text  = "Se requieren permisos de ubicación para este laboratorio.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { estadoPermisos.launchMultiplePermissionRequest() }) {
                        Text("Conceder permisos")
                    }
                }
            }
            return@Column
        }

        // Botón de control reactivo con cambio de color semántico
        Button(
            onClick = {
                capturando = !capturando
                val intent = Intent(context, GpsCaptureService::class.java)
                if (capturando) context.startForegroundService(intent)
                else            context.stopService(intent)
            },
            colors   = ButtonDefaults.buttonColors(
                containerColor = if (capturando) MaterialTheme.colorScheme.error
                else            MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                if (capturando) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (capturando) "Detener captura" else "Capturar coordenada (cada 10 s)")
        }

        // Contadores en vivo utilizando tarjetas contenedoras
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Google FLP",   style = MaterialTheme.typography.titleSmall)
                    Text("${googlePoints.size}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("registros",   style = MaterialTheme.typography.labelSmall)
                }
            }
            Card(
                colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Sensores GNSS", style = MaterialTheme.typography.titleSmall)
                    Text("${sensorsPoints.size}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("registros",    style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Text("Historial Comparativo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        // Lista optimizada usando claves basadas en el timestamp
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier            = Modifier.fillMaxSize()
        ) {
            items(items = history, key = { it.timestamp }) { record ->
                ComparativeCaptureCard(record, dateFormat)
            }
        }
    }
}

@Composable
fun ComparativeCaptureCard(record: ComparativeGpsRecord, dateFormat: SimpleDateFormat) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text       = "Instante: ${dateFormat.format(Date(record.timestamp))}",
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.secondary
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {

                // Panel Izquierdo: Google FLP
                Column(modifier = Modifier.weight(1f)) {
                    Text("GOOGLE FLP", style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    if (record.google != null) {
                        Text("Lat: ${record.google.latitude}",    style = MaterialTheme.typography.bodySmall)
                        Text("Lon: ${record.google.longitude}",   style = MaterialTheme.typography.bodySmall)
                        Text("Prec: ±${record.google.accuracy}m", style = MaterialTheme.typography.bodySmall)
                    } else {
                        Text("Buscando...", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Panel Derecho: Chip de Hardware (Sensores)
                Column(modifier = Modifier.weight(1f)) {
                    Text("SENSOR GNSS", style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                    if (record.sensors != null) {
                        if (record.sensors.latitude != null) {
                            Text("Lat: ${record.sensors.latitude}",         style = MaterialTheme.typography.bodySmall)
                            Text("Lon: ${record.sensors.longitude}",        style = MaterialTheme.typography.bodySmall)
                            Text("Alt: ${record.sensors.altitude ?: 0.0}m", style = MaterialTheme.typography.bodySmall)
                        } else {
                            // Feedback visual inmediato en rojo si no hay fijación satelital
                            Text("SIN SEÑAL",        style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            Text("Indoors / Sin Fix", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error)
                        }
                    } else {
                        Text("Buscando...", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}