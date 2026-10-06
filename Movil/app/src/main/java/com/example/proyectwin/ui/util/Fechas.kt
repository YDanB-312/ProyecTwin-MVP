package com.example.proyectwin.ui.util

import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

/**
 * Fechas de la API: `created_at` viene como instante ISO-8601 en UTC
 * (`2026-10-06T03:38:56.000000Z`); `fecha` viene como `YYYY-MM-DD`.
 * Los instantes se muestran en la zona horaria del dispositivo; las fechas
 * de calendario se muestran tal cual (sin desplazarlas).
 */
fun formatearFechaLocal(valor: String?): String {
    if (valor.isNullOrBlank()) return "—"
    return runCatching {
        if (valor.length <= 10) {
            LocalDate.parse(valor).format(FORMATO_FECHA)
        } else {
            OffsetDateTime.parse(valor)
                .atZoneSameInstant(ZoneId.systemDefault())
                .toLocalDate()
                .format(FORMATO_FECHA)
        }
    }.getOrElse { valor.take(10) }
}

fun formatearFechaHoraLocal(valor: String?): String {
    if (valor.isNullOrBlank()) return "—"
    return runCatching {
        if (valor.length <= 10) {
            LocalDate.parse(valor).format(FORMATO_FECHA)
        } else {
            OffsetDateTime.parse(valor)
                .atZoneSameInstant(ZoneId.systemDefault())
                .format(FORMATO_FECHA_HORA)
        }
    }.getOrElse { valor }
}
