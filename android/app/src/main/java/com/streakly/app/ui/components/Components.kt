package com.streakly.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.streakly.app.ui.theme.StreaklyColors

@Composable
fun StreaklyCard(
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(if (highlighted) StreaklyColors.GreenTint else StreaklyColors.Surface)
            .border(1.dp, if (highlighted) StreaklyColors.GreenBorder else StreaklyColors.Border, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    ) { content() }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = StreaklyColors.Green,
            contentColor = StreaklyColors.OnGreen,
            disabledContainerColor = StreaklyColors.GreenDark.copy(alpha = 0.4f),
            disabledContentColor = StreaklyColors.OnGreen.copy(alpha = 0.6f),
        ),
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = StreaklyColors.OnGreen, strokeWidth = 2.dp)
        } else {
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

/** Boton circular verde (+) o gris (atras). */
@Composable
fun CircleIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    filled: Boolean = true,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(if (filled) StreaklyColors.Green else StreaklyColors.SurfaceHigh)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = if (filled) StreaklyColors.OnGreen else StreaklyColors.TextPrimary,
            modifier = Modifier.size(size * 0.5f),
        )
    }
}

@Composable
fun BackHeader(title: String, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
        CircleIconButton(Icons.AutoMirrored.Rounded.ArrowBack, "Volver", onBack, filled = false)
        Spacer(Modifier.width(14.dp))
        Text(title.uppercase(), style = MaterialTheme.typography.headlineSmall)
    }
}

@Composable
fun StreakBadge(days: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(StreaklyColors.AmberTint)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.LocalFireDepartment, null, tint = StreaklyColors.Amber, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(
            if (days == 1) "1 día" else "$days días",
            color = StreaklyColors.Amber,
            style = MaterialTheme.typography.titleSmall,
        )
    }
}

@Composable
fun SmallStreak(days: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Rounded.LocalFireDepartment, null, tint = StreaklyColors.Amber, modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(2.dp))
        Text("$days", color = StreaklyColors.Amber, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun ProgressBar(progress: Float, modifier: Modifier = Modifier, height: Dp = 6.dp) {
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height))
            .background(StreaklyColors.SurfaceHigh),
    ) {
        Box(
            Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(height)
                .clip(RoundedCornerShape(height))
                .background(StreaklyColors.Green),
        )
    }
}

@Composable
fun FieldLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = StreaklyColors.TextSecondary,
        modifier = modifier.padding(bottom = 8.dp),
    )
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = StreaklyColors.TextSecondary,
        modifier = modifier,
    )
}

@Composable
fun StreaklyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = StreaklyColors.TextMuted) },
        singleLine = singleLine,
        minLines = minLines,
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        textStyle = MaterialTheme.typography.bodyLarge,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = StreaklyColors.Surface,
            unfocusedContainerColor = StreaklyColors.Surface,
            focusedBorderColor = StreaklyColors.GreenBorder,
            unfocusedBorderColor = StreaklyColors.Border,
            cursorColor = StreaklyColors.Green,
        ),
    )
}

@Composable
fun LoadingBox(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = StreaklyColors.Green)
    }
}

@Composable
fun ErrorBox(message: String, modifier: Modifier = Modifier, onRetry: () -> Unit) {
    Column(
        modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(message, color = StreaklyColors.TextSecondary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Text(
            "Reintentar",
            color = StreaklyColors.Green,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.clickable(onClick = onRetry).padding(8.dp),
        )
    }
}

@Composable
fun EmptyState(title: String, subtitle: String, modifier: Modifier = Modifier) {
    StreaklyCard(modifier.fillMaxWidth()) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            Spacer(Modifier.height(6.dp))
            Text(subtitle, color = StreaklyColors.TextSecondary, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun Dot(color: Color, size: Dp = 12.dp, modifier: Modifier = Modifier) {
    Box(modifier.size(size).clip(RoundedCornerShape(3.dp)).background(color))
}
