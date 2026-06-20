package com.example.demodata

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.demodata.ui.navigation.AppNavigation
import com.example.demodata.ui.theme.AppTheme
import com.example.demodata.ui.viewmodel.GpsViewModel
import com.example.demodata.ui.viewmodel.SessionViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Los objetos de infraestructura ya viven en DemoDataApp
        val app = application as DemoDataApp

        // 2. Instanciación de los ViewModels inyectando sus dependencias desde DemoDataApp
        val gpsViewModel     = GpsViewModel(app.gpsRepository)
        val sessionViewModel = SessionViewModel(app.sessionManager)

        setContent {
            // 3. Recolección reactiva de la preferencia del Modo Oscuro de DataStore
            val isDarkModePref by sessionViewModel.isDarkMode.collectAsStateWithLifecycle()
            val usarModoOscuro = isDarkModePref ?: isSystemInDarkTheme()

            // 4. Inyección del estado dinámico al tema de Material Design 3
            AppTheme(darkTheme = usarModoOscuro, dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color    = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        gpsViewModel     = gpsViewModel,
                        sessionViewModel = sessionViewModel
                    )
                }
            }
        }
    }
}