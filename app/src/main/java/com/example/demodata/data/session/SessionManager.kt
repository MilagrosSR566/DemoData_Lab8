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

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "fleet_session"
)

class SessionManager(private val context: Context) {

    private companion object {
        val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val KEY_USERNAME     = stringPreferencesKey("username")
        val KEY_DARK_MODE    = booleanPreferencesKey("dark_mode")
    }

    val isLoggedIn: Flow<Boolean> = context.sessionDataStore.data
        .map { prefs -> prefs[KEY_IS_LOGGED_IN] ?: false }

    val currentUsername: Flow<String?> = context.sessionDataStore.data
        .map { prefs -> prefs[KEY_USERNAME] }

    val isDarkMode: Flow<Boolean?> = context.sessionDataStore.data
        .map { prefs -> prefs[KEY_DARK_MODE] }

    suspend fun login(username: String) {
        context.sessionDataStore.edit { prefs ->
            prefs[KEY_IS_LOGGED_IN] = true
            prefs[KEY_USERNAME]     = username
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.sessionDataStore.edit { prefs ->
            prefs[KEY_DARK_MODE] = enabled
        }
    }

    suspend fun logout() {
        context.sessionDataStore.edit { prefs ->
            val currentTheme = prefs[KEY_DARK_MODE]
            prefs.clear()
            if (currentTheme != null) prefs[KEY_DARK_MODE] = currentTheme
        }
    }
}