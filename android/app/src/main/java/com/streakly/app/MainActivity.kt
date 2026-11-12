package com.streakly.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.streakly.app.ui.auth.LoginScreen
import com.streakly.app.ui.navigation.StreaklyNavHost
import com.streakly.app.ui.theme.StreaklyColors
import com.streakly.app.ui.theme.StreaklyTheme
import kotlinx.coroutines.flow.map

private sealed interface Session {
    data object Loading : Session
    data object LoggedOut : Session
    data object LoggedIn : Session
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as StreaklyApp).container

        setContent {
            StreaklyTheme {
                val sessionFlow = remember {
                    container.authRepository.token.map { if (it.isNullOrBlank()) Session.LoggedOut else Session.LoggedIn }
                }
                val session by sessionFlow.collectAsStateWithLifecycle(initialValue = Session.Loading)

                when (session) {
                    Session.Loading -> Box(Modifier.fillMaxSize().background(StreaklyColors.Background))
                    Session.LoggedOut -> LoginScreen()
                    Session.LoggedIn -> StreaklyNavHost()
                }
            }
        }
    }
}
