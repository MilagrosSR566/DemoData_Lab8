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
import androidx.compose.material.icons.automirrored.filled.List
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

enum class RecordsSource { LOCAL, REMOTE, ALL }

@Composable
fun ProfileScreen(onLogout: () -> Unit, username: String? = null) {
    val app = LocalContext.current.applicationContext as DemoDataApp
    val sessionVm: SessionViewModel = viewModel(
        factory = SessionViewModel.Factory(app.sessionManager)
    )
    var viewState by remember { mutableStateOf<ProfileViewState>(ProfileViewState.Menu) }

    when (viewState) {
        ProfileViewState.Menu          -> ProfileMenu(
            username                = username,
            onLogout                = onLogout,
            onNavigateToProfile      = { viewState = ProfileViewState.MyProfile },
            onNavigateToLocal        = { viewState = ProfileViewState.LocalRecords },
            onNavigateToAll          = { viewState = ProfileViewState.AllRecords },
            onNavigateToSync         = { viewState = ProfileViewState.Sync },
            onNavigateToNotifications = { viewState = ProfileViewState.Notifications }
        )
        ProfileViewState.MyProfile     -> MyProfileScreen(username = username, sessionVm = sessionVm, onBack = { viewState = ProfileViewState.Menu })
        ProfileViewState.LocalRecords  -> RecordsExplorerScreen(title = "Registros locales", allowedSource = RecordsSource.LOCAL, onBack = { viewState = ProfileViewState.Menu })
        ProfileViewState.AllRecords    -> RecordsExplorerScreen(title = "Todos los registros", allowedSource = RecordsSource.ALL, onBack = { viewState = ProfileViewState.Menu })
        ProfileViewState.Sync          -> NestedScreen(title = "Sincronización",  onBack = { viewState = ProfileViewState.Menu }) { SyncScreen() }
        ProfileViewState.Notifications -> NestedScreen(title = "Notificaciones",  onBack = { viewState = ProfileViewState.Menu }) { NotificationsScreen() }
    }
}

private sealed class ProfileViewState {
    object Menu : ProfileViewState()
    object MyProfile : ProfileViewState()
    object LocalRecords : ProfileViewState()
    object AllRecords : ProfileViewState()
    object Sync : ProfileViewState()
    object Notifications : ProfileViewState()
}

@Composable
private fun ProfileMenu(
    username: String?,
    onLogout: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToLocal: () -> Unit,
    onNavigateToAll: () -> Unit,
    onNavigateToSync: () -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    var mostrarConfirmacion by remember { mutableStateOf(false) }

    Column(
        modifier            = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Person, null, modifier = Modifier.size(72.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = username ?: "Usuario", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        MenuOption(Icons.Default.Person,              "Mi Perfil",          "Metadatos y configuración de tema",         onNavigateToProfile)
        Spacer(modifier = Modifier.height(12.dp))
        MenuOption(Icons.Default.History,             "Registros locales",  "Datos almacenados en este dispositivo",     onNavigateToLocal)
        Spacer(modifier = Modifier.height(12.dp))
        MenuOption(Icons.AutoMirrored.Filled.List,    "Todos los registros","Explorador local + nube (API)",             onNavigateToAll)
        Spacer(modifier = Modifier.height(12.dp))
        MenuOption(Icons.Default.CloudSync,           "Sincronización",     "Subir registros al servidor remoto",        onNavigateToSync)
        Spacer(modifier = Modifier.height(12.dp))
        MenuOption(Icons.Default.Notifications,       "Notificaciones",     "Programar y gestionar notificaciones",      onNavigateToNotifications)
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick  = { mostrarConfirmacion = true },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, null)
            Spacer(modifier = Modifier.size(8.dp))
            Text("Cerrar sesión")
        }
    }
    if (mostrarConfirmacion) LogoutDialog(onConfirm = onLogout, onDismiss = { mostrarConfirmacion = false })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordsExplorerScreen(title: String, allowedSource: RecordsSource, onBack: () -> Unit) {
    val context      = LocalContext.current
    val app          = context.applicationContext as DemoDataApp

    val googlePoints  by app.gpsRepository.googlePoints.collectAsStateWithLifecycle(emptyList())
    val sensorsPoints by app.gpsRepository.sensorsPoints.collectAsStateWithLifecycle(emptyList())
    val allMedia      by app.mediaRepository.allMedia.collectAsStateWithLifecycle(emptyList())
    val allAudios     by app.audioRepository.allAudios.collectAsStateWithLifecycle(emptyList())

    var selectedTab  by remember { mutableIntStateOf(0) }
    val tabs         = listOf("Todos", "GNSS", "Fotos", "Videos", "Audios")
    var sourceFilter by remember { mutableStateOf(if (allowedSource == RecordsSource.ALL) RecordsSource.ALL else RecordsSource.LOCAL) }
    var detailItem   by remember { mutableStateOf<ActivityItem?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            TextButton(onClick = onBack) { Text("Cerrar") }
        }

        // Filtro LOCAL / NUBE / TODO — solo visible en modo AllRecords
        if (allowedSource == RecordsSource.ALL) {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                SegmentedButton(
                    selected = sourceFilter == RecordsSource.ALL,
                    onClick  = { sourceFilter = RecordsSource.ALL },
                    shape    = SegmentedButtonDefaults.itemShape(0, 3),
                    icon     = { Icon(Icons.AutoMirrored.Filled.List, null, Modifier.size(16.dp)) }
                ) { Text("Todo",  style = MaterialTheme.typography.labelSmall) }
                SegmentedButton(
                    selected = sourceFilter == RecordsSource.LOCAL,
                    onClick  = { sourceFilter = RecordsSource.LOCAL },
                    shape    = SegmentedButtonDefaults.itemShape(1, 3),
                    icon     = { Icon(Icons.Default.Storage, null, Modifier.size(16.dp)) }
                ) { Text("Local", style = MaterialTheme.typography.labelSmall) }
                SegmentedButton(
                    selected = sourceFilter == RecordsSource.REMOTE,
                    onClick  = { sourceFilter = RecordsSource.REMOTE },
                    shape    = SegmentedButtonDefaults.itemShape(2, 3),
                    icon     = { Icon(Icons.Default.Cloud, null, Modifier.size(16.dp)) }
                ) { Text("Nube",  style = MaterialTheme.typography.labelSmall) }
            }
        }

        ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 16.dp) {
            tabs.forEachIndexed { index, t ->
                Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(t) })
            }
        }

        val filteredItems = remember(selectedTab, sourceFilter, googlePoints, sensorsPoints, allMedia, allAudios) {
            val localItems = mutableListOf<ActivityItem>().apply {
                addAll(googlePoints.map  { ActivityItem.GpsGoogle(it,  isRemote = false) })
                addAll(sensorsPoints.map { ActivityItem.GpsSensors(it, isRemote = false) })
                addAll(allMedia.map      { ActivityItem.Media(it,      isRemote = false) })
                addAll(allAudios.map     { ActivityItem.Audio(it,      isRemote = false) })
            }

            // Datos remotos simulados — placeholder hasta integrar API de consulta
            val remoteItems = if (sourceFilter != RecordsSource.LOCAL) listOf(
                ActivityItem.GpsGoogle(GpsGoogleEntity(id = 999, latitude = -12.0463, longitude = -77.0427, accuracy = 5f, timestamp = System.currentTimeMillis() - 86400000), isRemote = true),
                ActivityItem.Media(MediaEntity(id = 888, filePath = "", type = "PHOTO", sizeBytes = 1024, timestamp = System.currentTimeMillis() - 43200000), isRemote = true)
            ) else emptyList()

            val combined = when (sourceFilter) {
                RecordsSource.LOCAL  -> localItems
                RecordsSource.REMOTE -> remoteItems
                RecordsSource.ALL    -> localItems + remoteItems
            }

            val filtered = when (selectedTab) {
                0    -> combined
                1    -> combined.filter { it is ActivityItem.GpsGoogle || it is ActivityItem.GpsSensors }
                2    -> combined.filter { it is ActivityItem.Media && it.label == "PHOTO" }
                3    -> combined.filter { it is ActivityItem.Media && it.label == "VIDEO" }
                4    -> combined.filter { it is ActivityItem.Audio }
                else -> combined
            }
            filtered.sortedByDescending { it.timestamp }
        }

        LazyColumn(
            modifier            = Modifier.weight(1f).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredItems) { item ->
                ActivityRow(item, onClick = { detailItem = item })
            }
        }
    }

    if (detailItem != null) {
        ActivityDetailDialog(item = detailItem!!, onDismiss = { detailItem = null })
    }
}

@Composable
private fun NestedScreen(title: String, onBack: () -> Unit, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier          = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) { Text("← Volver") }
            Text(title, style = MaterialTheme.typography.titleMedium)
        }
        HorizontalDivider()
        content()
    }
}

@Composable
private fun MenuOption(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title,    style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, null)
        }
    }
}

@Composable
private fun MyProfileScreen(username: String?, sessionVm: SessionViewModel, onBack: () -> Unit) {
    val isDarkModePref by sessionVm.isDarkMode.collectAsStateWithLifecycle()
    val isDark         = isDarkModePref ?: isSystemInDarkTheme()
    val context        = LocalContext.current
    val androidId      = android.provider.Settings.Secure.getString(
        context.contentResolver, android.provider.Settings.Secure.ANDROID_ID
    )

    Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
        Text("Mi Perfil", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(24.dp))

        ProfileMetadataItem("Username",         username ?: "N/A")
        ProfileMetadataItem("Rol",              "Administrador / Operador")
        ProfileMetadataItem("Directorio Local", context.filesDir.absolutePath)

        Row(
            modifier              = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DarkMode, null, tint = MaterialTheme.colorScheme.primary)
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
        ProfileMetadataItem("Android ID",       androidId ?: "N/A")   // ← nuevo Lab 6

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

sealed class ActivityItem {
    abstract val timestamp: Long
    abstract val label: String
    abstract val icon: ImageVector
    abstract val isRemote: Boolean           // ← nuevo Lab 6

    data class GpsGoogle(val data: GpsGoogleEntity, override val isRemote: Boolean) : ActivityItem() {
        override val timestamp = data.timestamp
        override val label     = "GNSS Google"
        override val icon      = Icons.Default.LocationOn
    }
    data class GpsSensors(val data: GpsSensorsEntity, override val isRemote: Boolean) : ActivityItem() {
        override val timestamp = data.timestamp
        override val label     = "GNSS Sensor"
        override val icon      = Icons.Default.LocationOn
    }
    data class Media(val data: MediaEntity, override val isRemote: Boolean) : ActivityItem() {
        override val timestamp = data.timestamp
        override val label     = data.type
        override val icon      = if (data.type == MediaType.PHOTO.name) Icons.Default.PhotoCamera else Icons.Default.Videocam
    }
    data class Audio(val data: AudioEntity, override val isRemote: Boolean) : ActivityItem() {
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
                item.icon, null,
                tint = when {
                    isNoSignal    -> MaterialTheme.colorScheme.error
                    item.isRemote -> MaterialTheme.colorScheme.tertiary  // azul/verde para nube
                    else          -> MaterialTheme.colorScheme.primary
                }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text  = if (isNoSignal) "${item.label} (Sin señal)" else item.label,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (isNoSignal) MaterialTheme.colorScheme.error else Color.Unspecified
                    )
                    if (item.isRemote) {
                        Spacer(modifier = Modifier.width(8.dp))
                        SuggestionChip(
                            onClick  = {},
                            label    = { Text("Cloud", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.height(20.dp)
                        )
                    }
                }
                Text(dateFormat.format(Date(item.timestamp)), style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Default.ChevronRight, null)
        }
    }
}

@Composable
private fun ActivityDetailDialog(item: ActivityItem, onDismiss: () -> Unit) {
    val context    = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(item.label + if (item.isRemote) " (Nube)" else " (Local)") },
        text  = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Fecha:  ${dateFormat.format(Date(item.timestamp))}")
                Text("Origen: ${if (item.isRemote) "Servidor Externo" else "Memoria del Dispositivo"}")
                Spacer(modifier = Modifier.height(8.dp))

                when (item) {
                    is ActivityItem.GpsGoogle -> {
                        if (item.data.latitude != null) {
                            Text("Lat: ${item.data.latitude}")
                            Text("Lon: ${item.data.longitude}")
                            Text("Accuracy: ±${item.data.accuracy}m")
                        } else {
                            Text("Estado: SIN SEÑAL", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    is ActivityItem.GpsSensors -> {
                        if (item.data.latitude != null) {
                            Text("Lat: ${item.data.latitude}")
                            Text("Lon: ${item.data.longitude}")
                            item.data.altitude?.let { Text("Altitud: ${it}m") }
                        } else {
                            Text("Estado: SIN SEÑAL", color = MaterialTheme.colorScheme.error)
                        }
                        Text("Provider: ${item.data.provider}")
                    }
                    is ActivityItem.Media -> {
                        Text("Tamaño: ${item.data.sizeBytes / 1024} KB")
                        if (!item.isRemote) {
                            Spacer(modifier = Modifier.height(8.dp))
                            AsyncImage(
                                model        = File(item.data.filePath),
                                contentDescription = null,
                                modifier     = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = {
                                openFile(context, item.data.filePath,
                                    if (item.data.type == MediaType.PHOTO.name) "image/*" else "video/*")
                            }) {
                                Text(if (item.data.type == MediaType.PHOTO.name) "Ver Foto" else "Reproducir Video")
                            }
                        } else {
                            Text("Archivo alojado en la nube. Pendiente integración CDN.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                    is ActivityItem.Audio -> {
                        Text("Duración: ${item.data.durationMs / 1000}s")
                        if (!item.isRemote) {
                            Button(onClick = { openFile(context, item.data.filePath, "audio/*") }, modifier = Modifier.padding(top = 16.dp)) {
                                Text("Reproducir Audio")
                            }
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
        confirmButton = { TextButton(onClick = onConfirm) { Text("Sí, cerrar sesión", color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}