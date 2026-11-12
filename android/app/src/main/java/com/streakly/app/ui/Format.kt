package com.streakly.app.ui

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

val Spanish: Locale = Locale.forLanguageTag("es-CO")

/** 2.0 -> "2", 2.5 -> "2.5" */
fun Double.pretty(): String =
    if (this % 1.0 == 0.0) this.toLong().toString() else "%.1f".format(Locale.US, this)

/** "Miércoles 4 de noviembre" */
fun LocalDate.longSpanish(): String =
    format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Spanish)).replaceFirstChar { it.titlecase(Spanish) }

/** "5 dic" */
fun LocalDate.shortSpanish(): String =
    format(DateTimeFormatter.ofPattern("d MMM", Spanish)).replace(".", "")

/** "faltan 31 días" / "vence hoy" / "venció hace 2 días" */
fun LocalDate.remainingText(today: LocalDate = LocalDate.now()): String {
    val days = ChronoUnit.DAYS.between(today, this)
    return when {
        days > 1 -> "faltan $days días"
        days == 1L -> "falta 1 día"
        days == 0L -> "vence hoy"
        days == -1L -> "venció ayer"
        else -> "venció hace ${-days} días"
    }
}

fun minimumText(target: Double, unit: String): String =
    "Mínimo ${target.pretty()}${if (unit.isNotBlank()) " $unit" else ""}"
