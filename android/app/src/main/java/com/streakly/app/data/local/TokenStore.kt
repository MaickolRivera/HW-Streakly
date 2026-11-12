package com.streakly.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore(name = "session")

/**
 * Guarda el JWT y el correo del usuario en DataStore (almacenamiento privado de la app).
 * Mantiene una copia en memoria para que el interceptor de OkHttp no tenga que suspender.
 */
class TokenStore(private val context: Context) {

    private val tokenKey = stringPreferencesKey("token")
    private val emailKey = stringPreferencesKey("email")

    @Volatile
    var currentToken: String? = null
        private set

    val token: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[tokenKey].also { currentToken = it }
    }

    val email: Flow<String?> = context.dataStore.data.map { it[emailKey] }

    suspend fun load(): String? = token.first()

    suspend fun save(token: String, email: String) {
        currentToken = token
        context.dataStore.edit {
            it[tokenKey] = token
            it[emailKey] = email
        }
    }

    suspend fun clear() {
        currentToken = null
        context.dataStore.edit { it.clear() }
    }

    fun clearBlocking() = runBlocking { clear() }
}
