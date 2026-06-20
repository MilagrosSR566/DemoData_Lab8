package com.example.demodata.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.demodata.ui.screens.GpsScreen
import com.example.demodata.ui.screens.ProfileScreen
import com.example.demodata.ui.viewmodel.GpsViewModel
import com.example.demodata.ui.viewmodel.SessionViewModel

sealed class Ruta(val ruta: String, val etiqueta: String, val icono: ImageVector) {
    object Gps    : Ruta("gps",    "Captura GNSS", Icons.Default.Place)
    object Perfil : Ruta("perfil", "Mi Perfil",    Icons.Default.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(gpsViewModel: GpsViewModel, sessionViewModel: SessionViewModel) {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStack?.destination

    val pestañas = listOf(Ruta.Gps, Ruta.Perfil)

    val tituloActual = when (currentDestination?.route) {
        Ruta.Gps.ruta    -> "Laboratorio 4: GNSS Dual"
        Ruta.Perfil.ruta -> "Panel de Usuario"
        else             -> "DemoData"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tituloActual) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                pestañas.forEach { pestaña ->
                    val seleccionada = currentDestination?.hierarchy
                        ?.any { it.route == pestaña.ruta } == true
                    NavigationBarItem(
                        selected = seleccionada,
                        onClick  = {
                            navController.navigate(pestaña.ruta) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState    = true
                            }
                        },
                        icon  = { Icon(pestaña.icono, contentDescription = pestaña.etiqueta) },
                        label = { Text(pestaña.etiqueta) }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController    = navController,
            startDestination = Ruta.Gps.ruta,
            modifier         = Modifier.padding(paddingValues)
        ) {
            composable(Ruta.Gps.ruta) {
                GpsScreen(viewModel = gpsViewModel)
            }
            composable(Ruta.Perfil.ruta) {
                ProfileScreen(
                    sessionVm = sessionViewModel,
                    onLogout  = { sessionViewModel.logout() }
                )
            }
        }
    }
}
