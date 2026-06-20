package com.example.demodata.ui.screens

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.example.demodata.DemoDataApp
import com.example.demodata.data.local.entity.AudioEntity
import com.example.demodata.ui.viewmodel.AudioViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AudioScreen() {
    val context = LocalContext.current
    val app     = context.applicationContext as DemoDataApp
    val vm: AudioViewModel = viewModel(
        factory = AudioViewModel.Factory(
            context.applicationContext,
            app.audioRepository,
            app.fileStorage
        )
    )

    val audios         by vm.audios.collectAsStateWithLifecycle()
    val isRecording    by vm.isRecording.collectAsStateWithLifecycle()
    val elapsedSeconds by vm.elapsedSeconds.collectAsStateWithLifecycle()
    val micPermission  = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Audio", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Grabaciones AAC guardadas en filesDir",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (!micPermission.status.isGranted) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Esta pantalla necesita permiso de micrófono.", color = MaterialTheme.colorScheme.onErrorContainer)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { micPermission.launchPermissionRequest() }) { Text("Conceder permiso") }
                }
            }
            return@Column
        }

        // ── Card con el timer ──
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors   = CardDefaults.cardColors(
                containerColor = if (isRecording)
                    MaterialTheme.colorScheme.errorContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier            = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isRecording) {
                        Icon(Icons.Default.FiberManualRecord, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text  = if (isRecording) "Grabando…" else "Detenido",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text       = String.format(Locale.US, "%02d:%02d", elapsedSeconds / 60, elapsedSeconds % 60),
                    style      = MaterialTheme.typography.displayMedium,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick  = { if (isRecording) vm.stopRecording() else vm.startRecording() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor = if (isRecording)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(if (isRecording) Icons.Default.Stop else Icons.Default.Mic, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isRecording) "Detener grabación" else "Iniciar grabación")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("${audios.size} grabaciones guardadas", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(8.dp))

        if (audios.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Text(
                    "Aún no hay grabaciones. Tap en Iniciar grabación.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(items = audios, key = { it.id }) { audio ->
                    AudioItemRow(audio = audio, onDelete = { vm.delete(audio) })
                }
            }
        }
    }
}

@Composable
private fun AudioItemRow(audio: AudioEntity, onDelete: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("dd/MM HH:mm:ss", Locale.getDefault()) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.AudioFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("${audio.durationMs / 1000} seg · ${audio.format}", style = MaterialTheme.typography.titleSmall)
                Text("${audio.sizeBytes / 1024} KB", style = MaterialTheme.typography.bodyMedium)
                Text(dateFormat.format(Date(audio.timestamp)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}