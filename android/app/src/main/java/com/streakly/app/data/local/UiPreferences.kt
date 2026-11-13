package com.streakly.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Archivo distinto al de la sesion: cerrar sesion borra el token, pero conserva las preferencias.
private val Context.uiDataStore by preferencesDataStore(name = "ui_preferences")

data class UiPrefs(
    /** En "Hoy" se esconden los habitos que ya cumplieron su minimo. */
    val hideCompleted: Boolean = false,
    /** En "Hoy" los habitos se ordenan alfabeticamente en vez de por fecha de creacion. */
    val sortByName: Boolean = false,
)

/** Preferencias de interfaz guardadas en el telefono (RF-11). */
class UiPreferences(private val context: Context) {

    private val hideCompletedKey = booleanPreferencesKey("hide_completed")
    private val sortByNameKey = booleanPreferencesKey("sort_by_name")

    val prefs: Flow<UiPrefs> = context.uiDataStore.data.map {
        UiPrefs(
            hideCompleted = it[hideCompletedKey] ?: false,
            sortByName = it[sortByNameKey] ?: false,
        )
    }

    suspend fun setHideCompleted(value: Boolean) {
        context.uiDataStore.edit { it[hideCompletedKey] = value }
    }

    suspend fun setSortByName(value: Boolean) {
        context.uiDataStore.edit { it[sortByNameKey] = value }
    }
}
