package com.example.demodata.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.demodata.DemoDataApp
import com.example.demodata.ui.viewmodel.SyncViewModel

@Composable
fun SyncScreen() {
    val context = LocalContext.current
    val app     = context.applicationContext as DemoDataApp
    val vm: SyncViewModel = viewModel(
        factory = SyncViewModel.Factory(app.gpsRepository, app.mediaRepository, app.audioRepository)
    )

    val counts by vm.counts.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Text("Sync Center", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Inventario de registros locales pendientes",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick  = { Toast.makeText(context, "Por implementar", Toast.LENGTH_SHORT).show() },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Icon(Icons.Default.CloudUpload, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sincronizar ahora")
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "El servidor se integrará en una fase posterior.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth().padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total de registros locales", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("Suma de todas las categorías", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Text("${counts.total}", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Desglose por tipo", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(8.dp))

        CategoryRow(Icons.Default.LocationOn, "GNSS Google FLP",  counts.gpsGoogle)
        Spacer(modifier = Modifier.height(8.dp))
        CategoryRow(Icons.Default.Sensors,    "GNSS Sensores HW", counts.gpsSensors)
        Spacer(modifier = Modifier.height(8.dp))
        CategoryRow(Icons.Default.PhotoCamera,"Fotos",            counts.photos)
        Spacer(modifier = Modifier.height(8.dp))
        CategoryRow(Icons.Default.Videocam,   "Videos",           counts.videos)
        Spacer(modifier = Modifier.height(8.dp))
        CategoryRow(Icons.Default.AudioFile,  "Audios",           counts.audios)
    }
}

@Composable
private fun CategoryRow(icon: ImageVector, label: String, count: Int) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Text(
                "$count",
                style = MaterialTheme.typography.titleLarge,
                color = if (count > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
        }
    }
}