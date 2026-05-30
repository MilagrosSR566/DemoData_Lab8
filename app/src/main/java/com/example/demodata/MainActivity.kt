package com.example.demodata

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.demodata.ui.screens.GpsScreen
import com.example.demodata.ui.viewmodel.GpsViewModel
import com.example.demodata.ui.viewmodel.SessionViewModel
import com.example.demodata.ui.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicialización segura de dependencias globales
        val app = application as DemoDataApp
        val sessionVm = SessionViewModel(app.sessionManager)
        val gpsVm = GpsViewModel(app.gpsRepository)

        setContent {
            // 12. SessionViewModel expone isDarkMode: StateFlow<Boolean?>
            val isDarkModePref by sessionVm.isDarkMode.collectAsStateWithLifecycle()

            // 13. MainActivity aplica isDarkMode a AppTheme (DemoDataTheme)
            val darkTheme = isDarkModePref ?: isSystemInDarkTheme()

            AppTheme(darkTheme = darkTheme) {
                // Invocación segura de GpsScreen delegando el ciclo de vida de los permisos correctamente
                GpsScreen(viewModel = gpsVm)
            }
        }
    }
}