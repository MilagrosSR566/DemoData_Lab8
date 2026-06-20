package com.example.demodata.ui.screens

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.demodata.DemoDataApp
import com.example.demodata.data.local.entity.AudioEntity
import com.example.demodata.data.local.entity.GpsGoogleEntity
import com.example.demodata.data.local.entity.GpsSensorsEntity
import com.example.demodata.data.local.entity.MediaEntity
import com.example.demodata.data.local.entity.MediaType
import com.example.demodata.ui.viewmodel.SessionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(onLogout: () -> Unit, username: String? = null) {
    val app = LocalContext.current.applicationContext as DemoDataApp
    val sessionVm: SessionViewModel = viewModel(
        factory = SessionViewModel.Factory(app.sessionManager)
    )

    var viewState by remember { mutableStateOf<ProfileViewState>(ProfileViewState.Menu) }

    when (viewState) {
        ProfileViewState.Menu -> ProfileMenu(
            username            = username,
            onLogout            = onLogout,
            onNavigateToProfile  = { viewState = ProfileViewState.MyProfile },
            onNavigateToActivity = { viewState = ProfileViewState.MyActivity }
        )
        ProfileViewState.MyProfile -> MyProfileScreen(
            username  = username,
            sessionVm = sessionVm,
            onBack    = { viewState = ProfileViewState.Menu }
        )
        ProfileViewState.MyActivity -> MyActivityScreen(
            onBack = { viewState = ProfileViewState.Menu }
        )
    }
}

private sealed class ProfileViewState {
    object Menu       : ProfileViewState()
    object MyProfile  : ProfileViewState()
    object MyActivity : ProfileViewState()
}

@Composable
private fun ProfileMenu(
    username: String?,
    onLogout: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToActivity: () -> Unit
) {
    var mostrarConfirmacion by remember { mutableStateOf(false) }

    Column(
        modifier            = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(72.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = username ?: "Usuario", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        MenuOption(icon = Icons.Default.Person,  title = "Mi Perfil",    subtitle = "Ver metadatos del usuario",                  onClick = onNavigateToProfile)
        Spacer(modifier = Modifier.height(12.dp))
        MenuOption(icon = Icons.Default.History, title = "Mi Actividad", subtitle = "Registros locales de GNSS y multimedia",     onClick = onNavigateToActivity)
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick  = { mostrarConfirmacion = true },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Spacer(modifier = Modifier.size(8.dp))
            Text("Cerrar sesión")
        }
    }

    if (mostrarConfirmacion) {
        LogoutDialog(onConfirm = onLogout, onDismiss = { mostrarConfirmacion = false })
    }
}

@Composable
private fun MenuOption(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title,    style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}

@Composable
private fun MyProfileScreen(username: String?, sessionVm: SessionViewModel, onBack: () -> Unit) {
    val isDarkModePref by sessionVm.isDarkMode.collectAsStateWithLifecycle()
    val isDark = isDarkModePref ?: isSystemInDarkTheme()

    Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
        Text("Mi Perfil", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(24.dp))

        ProfileMetadataItem("Username",       username ?: "N/A")
        ProfileMetadataItem("Rol",            "Administrador / Operador")
        ProfileMetadataItem("Directorio Local", LocalContext.current.filesDir.absolutePath)

        Row(
            modifier              = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Modo Noche", style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (isDarkModePref == null) "Siguiendo al sistema"
                        else if (isDark) "Activado" else "Desactivado",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Switch(checked = isDark, onCheckedChange = { sessionVm.setDarkMode(it) })
        }
        HorizontalDivider()

        Spacer(modifier = Modifier.height(16.dp))
        ProfileMetadataItem("Dispositivo",      "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
        ProfileMetadataItem("Android Version",  android.os.Build.VERSION.RELEASE)
        ProfileMetadataItem("API Level",        android.os.Build.VERSION.SDK_INT.toString())

        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Volver") }
    }
}

@Composable
private fun ProfileMetadataItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(value, style = MaterialTheme.typography.bodyLarge)
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun MyActivityScreen(onBack: () -> Unit) {
    val context  = LocalContext.current
    val app      = context.applicationContext as DemoDataApp

    val googlePoints  by app.gpsRepository.googlePoints.collectAsStateWithLifecycle(emptyList())
    val sensorsPoints by app.gpsRepository.sensorsPoints.collectAsStateWithLifecycle(emptyList())
    val allMedia      by app.mediaRepository.allMedia.collectAsStateWithLifecycle(emptyList())
    val allAudios     by app.audioRepository.allAudios.collectAsStateWithLifecycle(emptyList())

    var combinedItems by remember { mutableStateOf<List<ActivityItem>>(emptyList()) }

    LaunchedEffect(googlePoints, sensorsPoints, allMedia, allAudios) {
        withContext(Dispatchers.Default) {
            val items = mutableListOf<ActivityItem>()
            items.addAll(googlePoints.map  { ActivityItem.GpsGoogle(it) })
            items.addAll(sensorsPoints.map { ActivityItem.GpsSensors(it) })
            items.addAll(allMedia.map      { ActivityItem.Media(it) })
            items.addAll(allAudios.map     { ActivityItem.Audio(it) })
            items.sortByDescending { it.timestamp }
            combinedItems = items
        }
    }

    var detailItem by remember { mutableStateOf<ActivityItem?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Mi Actividad", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            TextButton(onClick = onBack) { Text("Cerrar") }
        }
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            items(combinedItems) { item ->
                ActivityRow(item, onClick = { detailItem = item })
            }
        }
    }

    if (detailItem != null) {
        ActivityDetailDialog(item = detailItem!!, onDismiss = { detailItem = null })
    }
}

sealed class ActivityItem {
    abstract val timestamp: Long
    abstract val label: String
    abstract val icon: ImageVector

    data class GpsGoogle(val data: GpsGoogleEntity) : ActivityItem() {
        override val timestamp = data.timestamp
        override val label     = "GNSS Google"
        override val icon      = Icons.Default.LocationOn
    }
    data class GpsSensors(val data: GpsSensorsEntity) : ActivityItem() {
        override val timestamp = data.timestamp
        override val label     = "GNSS Sensor"
        override val icon      = Icons.Default.LocationOn
    }
    data class Media(val data: MediaEntity) : ActivityItem() {
        override val timestamp = data.timestamp
        override val label     = data.type
        override val icon      = if (data.type == MediaType.PHOTO.name) Icons.Default.PhotoCamera else Icons.Default.Videocam
    }
    data class Audio(val data: AudioEntity) : ActivityItem() {
        override val timestamp = data.timestamp
        override val label     = "Audio"
        override val icon      = Icons.Default.AudioFile
    }
}

@Composable
private fun ActivityRow(item: ActivityItem, onClick: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("dd/MM HH:mm:ss", Locale.getDefault()) }
    val isNoSignal = item is ActivityItem.GpsSensors && item.data.latitude == null

    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                item.icon,
                contentDescription = null,
                tint = if (isNoSignal) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = if (isNoSignal) "${item.label} (Sin señal)" else item.label,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isNoSignal) MaterialTheme.colorScheme.error else Color.Unspecified
                )
                Text(dateFormat.format(Date(item.timestamp)), style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}

@Composable
private fun ActivityDetailDialog(item: ActivityItem, onDismiss: () -> Unit) {
    val context    = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(item.label) },
        text  = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Fecha: ${dateFormat.format(Date(item.timestamp))}")
                Spacer(modifier = Modifier.height(8.dp))

                when (item) {
                    is ActivityItem.GpsGoogle -> {
                        Text("Lat: ${item.data.latitude}")
                        Text("Lon: ${item.data.longitude}")
                        Text("Accuracy: ±${item.data.accuracy}m")
                        item.data.speed?.let { Text("Velocidad: $it m/s") }
                    }
                    is ActivityItem.GpsSensors -> {
                        if (item.data.latitude != null) {
                            Text("Lat: ${item.data.latitude}")
                            Text("Lon: ${item.data.longitude}")
                            item.data.altitude?.let { Text("Altitud: ${it}m") }
                        } else {
                            Text("Estado: SIN SEÑAL", color = MaterialTheme.colorScheme.error)
                            Text("Causa: Probable lugar cerrado (sin vista a satélites)")
                        }
                        Text("Provider: ${item.data.provider}")
                    }
                    is ActivityItem.Media -> {
                        Text("Tamaño: ${item.data.sizeBytes / 1024} KB")
                        Spacer(modifier = Modifier.height(8.dp))
                        AsyncImage(
                            model              = File(item.data.filePath),
                            contentDescription = null,
                            modifier           = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(8.dp)),
                            contentScale       = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            openFile(context, item.data.filePath,
                                if (item.data.type == MediaType.PHOTO.name) "image/*" else "video/*")
                        }) {
                            Text(if (item.data.type == MediaType.PHOTO.name) "Ver Foto" else "Reproducir Video")
                        }
                    }
                    is ActivityItem.Audio -> {
                        Text("Duración: ${item.data.durationMs / 1000}s")
                        Text("Tamaño: ${item.data.sizeBytes / 1024} KB")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { openFile(context, item.data.filePath, "audio/*") }) {
                            Text("Reproducir Audio")
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
    )
}

private fun openFile(context: android.content.Context, path: String, mimeType: String) {
    try {
        val uri    = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", File(path))
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    } catch (_: Exception) { }
}

@Composable
private fun LogoutDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title   = { Text("¿Confirmar cierre de sesión?") },
        text    = { Text("Volverás a la pantalla de login. Tus datos locales se conservan.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Sí, cerrar sesión", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}