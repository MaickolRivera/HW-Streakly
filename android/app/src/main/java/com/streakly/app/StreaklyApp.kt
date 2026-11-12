package com.streakly.app

import android.app.Application
import android.content.Context
import com.streakly.app.data.AuthRepository
import com.streakly.app.data.GoalRepository
import com.streakly.app.data.HabitRepository
import com.streakly.app.data.local.TokenStore
import com.streakly.app.data.remote.ApiClient

/** Contenedor de dependencias manual (sin Hilt para mantener el proyecto simple). */
class AppContainer(context: Context) {
    val tokenStore = TokenStore(context)
    private val api = ApiClient.create(tokenStore)

    val authRepository = AuthRepository(api, tokenStore)
    val habitRepository = HabitRepository(api)
    val goalRepository = GoalRepository(api)
}

class StreaklyApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
