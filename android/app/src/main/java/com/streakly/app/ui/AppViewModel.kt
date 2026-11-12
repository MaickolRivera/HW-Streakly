package com.streakly.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.streakly.app.AppContainer
import com.streakly.app.StreaklyApp

/** Crea un ViewModel con acceso al contenedor de dependencias de la app. */
@Composable
inline fun <reified VM : ViewModel> appViewModel(crossinline create: (AppContainer) -> VM): VM {
    val app = LocalContext.current.applicationContext as StreaklyApp
    return viewModel(factory = viewModelFactory { initializer { create(app.container) } })
}
