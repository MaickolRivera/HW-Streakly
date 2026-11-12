package com.streakly.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.streakly.app.ui.appViewModel
import com.streakly.app.ui.components.FieldLabel
import com.streakly.app.ui.components.PrimaryButton
import com.streakly.app.ui.components.StreaklyTextField
import com.streakly.app.ui.theme.StreaklyColors

@Composable
fun LoginScreen(viewModel: LoginViewModel = appViewModel { LoginViewModel(it.authRepository) }) {
    val state = viewModel.state
    var showPassword by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .background(StreaklyColors.Background)
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp),
    ) {
        Spacer(Modifier.height(80.dp))
        Box(
            Modifier.size(68.dp).clip(RoundedCornerShape(14.dp)).background(StreaklyColors.Green),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.LocalFireDepartment, null, tint = StreaklyColors.OnGreen, modifier = Modifier.size(34.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text("STREAKLY", style = MaterialTheme.typography.displayLarge)
        Spacer(Modifier.height(10.dp))
        Text(
            "Hábitos pequeños, todos los días. Cumple tu mínimo y mantén la racha.",
            style = MaterialTheme.typography.bodyLarge,
            color = StreaklyColors.TextSecondary,
        )

        Spacer(Modifier.height(36.dp))
        FieldLabel("Correo")
        StreaklyTextField(
            value = state.email,
            onValueChange = viewModel::onEmail,
            placeholder = "tucorreo@ejemplo.com",
            keyboardType = KeyboardType.Email,
        )
        Spacer(Modifier.height(18.dp))
        FieldLabel("Contraseña")
        StreaklyTextField(
            value = state.password,
            onValueChange = viewModel::onPassword,
            placeholder = if (state.isRegister) "Mínimo 8 caracteres" else "Tu contraseña",
            keyboardType = KeyboardType.Password,
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = if (showPassword) "Ocultar contraseña" else "Mostrar contraseña",
                        tint = StreaklyColors.TextSecondary,
                    )
                }
            },
        )

        state.error?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = StreaklyColors.Danger, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(24.dp))
        PrimaryButton(
            text = if (state.isRegister) "Crear cuenta" else "Iniciar sesión",
            onClick = viewModel::submit,
            loading = state.loading,
        )

        Spacer(Modifier.height(48.dp))
        Row(
            Modifier.fillMaxWidth().padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                if (state.isRegister) "¿Ya tienes cuenta? " else "¿Primera vez? ",
                color = StreaklyColors.TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                if (state.isRegister) "Iniciar sesión" else "Crear cuenta",
                color = StreaklyColors.Green,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.clickable(onClick = viewModel::toggleMode),
            )
        }
    }
}
