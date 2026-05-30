package com.example.demodata.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.demodata.DemoDataApp
import com.example.demodata.data.local.entity.GpsGoogleEntity
import com.example.demodata.data.local.entity.GpsSensorsEntity
import com.example.demodata.ui.viewmodel.SessionViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private sealed class ProfileViewState {
    object Menu      : ProfileViewState()
    object MyProfile : ProfileViewState()
    object MyActivity: ProfileViewState()
}

sealed class ActivityItem {
    abstract val timestamp: Long
    abstract val id: String
    abstract val title: String
    abstract val subtitle: String
    abstract val icon: ImageVector

    data class GpsGoogle(val entity: GpsGoogleEntity) : ActivityItem() {
        override val timestamp = entity.timestamp
        override val id = "google_${entity.timestamp}"
        override val title = "Captura Google FLP"
        override val subtitle = "Lat: ${entity.latitude}, Lon: ${entity.longitude}"
        override val icon = Icons.Default.LocationOn
    }

    data class GpsSensors(val entity: GpsSensorsEntity) : ActivityItem() {
        override val timestamp = entity.timestamp
        override val id = "sensors_${entity.timestamp}"
        override val title = "Captura Chip GNSS"
        override val subtitle = "Lat: ${entity.latitude ?: "N/A"}, Lon: ${entity.longitude ?: "N/A"}"
        override val icon = Icons.Default.LocationOn
    }
}

@Composable
fun ProfileScreen(onLogout: () -> Unit, username: String?) {
    var viewState by remember { mutableStateOf<ProfileViewState>(ProfileViewState.Menu) }

    when (viewState) {
        ProfileViewState.Menu       -> ProfileMenu(
            username = username,
            onLogout = onLogout,
            onNavigateToProfile  = { viewState = ProfileViewState.MyProfile },
            onNavigateToActivity = { viewState = ProfileViewState.MyActivity }
        )
        ProfileViewState.MyProfile  -> MyProfileScreen(username = username, onBack = { viewState = ProfileViewState.Menu })
        ProfileViewState.MyActivity -> MyActivityScreen(onBack = { viewState = ProfileViewState.Menu })
    }
}

@Composable
fun ProfileMenu(
    username: String?,
    onLogout: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToActivity: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = username ?: "Usuario Invitado",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth().clickable { onNavigateToProfile() },
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null)
                Spacer(modifier = Modifier.width(16.dp))
                Text("Mi Perfil")
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.ChevronRight, contentDescription = null)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth().clickable { onNavigateToActivity() },
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, contentDescription = null)
                Spacer(modifier = Modifier.width(16.dp))
                Text("Mi Actividad")
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.ChevronRight, contentDescription = null)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { showDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cerrar sesión")
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("¿Confirmar cierre de sesión?") },
                text  = { Text("Tus datos locales se conservan.") },
                confirmButton = {
                    TextButton(onClick = {
                        showDialog = false
                        onLogout()
                    }) {
                        Text("Sí, cerrar sesión", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancelar") } }
            )
        }
    }
}

@Composable
fun MyProfileScreen(
    username: String?,
    onBack: () -> Unit,
    sessionVm: SessionViewModel = viewModel()
) {
    val isDarkModePref by sessionVm.isDarkMode.collectAsStateWithLifecycle()
    val isDark = isDarkModePref ?: isSystemInDarkTheme()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())
    ) {
        Text("Mi Perfil", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        ProfileMetadataItem("Username", username ?: "N/A")
        ProfileMetadataItem("Rol", "Administrador / Operador")
        ProfileMetadataItem("Directorio Local", LocalContext.current.filesDir.absolutePath)
        ProfileMetadataItem("Dispositivo", "${Build.MANUFACTURER} ${Build.MODEL}")
        ProfileMetadataItem("Android Version", Build.VERSION.RELEASE)
        ProfileMetadataItem("API Level", Build.VERSION.SDK_INT.toString())

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DarkMode, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Modo Oscuro")
            }
            Switch(
                checked = isDark,
                onCheckedChange = { sessionVm.setDarkMode(it) }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Volver")
        }
    }
}

@Composable
fun MyActivityScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as DemoDataApp

    val gpsRepo = app.gpsRepository

    val googlePoints by gpsRepo.googlePoints.collectAsStateWithLifecycle(emptyList())
    val sensorsPoints by gpsRepo.sensorsPoints.collectAsStateWithLifecycle(emptyList())

    val combinedItems = remember(googlePoints, sensorsPoints) {
        mutableListOf<ActivityItem>().apply {
            addAll(googlePoints.map  { ActivityItem.GpsGoogle(it) })
            addAll(sensorsPoints.map { ActivityItem.GpsSensors(it) })
        }.sortedByDescending { it.timestamp }
    }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Mi Actividad", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(combinedItems, key = { it.id }) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(item.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(item.title, style = MaterialTheme.typography.bodyLarge)
                            Text(item.subtitle, modifier = Modifier.padding(top = 2.dp), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Text(dateFormat.format(Date(item.timestamp)), style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Volver")
        }
    }
}

@Composable
fun ProfileMetadataItem(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider()
    }
}

@Composable
fun ActivityDetailDialog(
    path: String,
    mimeType: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Detalle de Actividad") },
        text = { Text(path.substringAfterLast("/")) },
        confirmButton = {
            Button(onClick = {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    File(path)
                )
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, mimeType)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(intent)
                onDismiss()
            }) {
                Text("Abrir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}