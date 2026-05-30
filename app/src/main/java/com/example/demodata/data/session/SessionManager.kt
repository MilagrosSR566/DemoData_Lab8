package com.example.demodata.data.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Declaramos la extensión necesaria para inicializar el DataStore
val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "session_prefs")

class SessionManager(private val context: Context) {

    private companion object {
        val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val KEY_USERNAME     = stringPreferencesKey("username")
        val KEY_DARK_MODE    = booleanPreferencesKey("dark_mode")   // ← nueva en este lab
    }

    val isLoggedIn: Flow<Boolean> = context.sessionDataStore.data
        .map { it[KEY_IS_LOGGED_IN] ?: false }

    val currentUsername: Flow<String?> = context.sessionDataStore.data
        .map { it[KEY_USERNAME] }

    // null = seguir al sistema operativo; true/false = forzar modo
    val isDarkMode: Flow<Boolean?> = context.sessionDataStore.data
        .map { it[KEY_DARK_MODE] }

    suspend fun setDarkMode(enabled: Boolean) {
        context.sessionDataStore.edit { it[KEY_DARK_MODE] = enabled }
    }

    suspend fun logout() {
        context.sessionDataStore.edit { prefs ->
            val currentTheme = prefs[KEY_DARK_MODE]   // preservar antes de clear
            prefs.clear()
            if (currentTheme != null) prefs[KEY_DARK_MODE] = currentTheme
        }
    }
}