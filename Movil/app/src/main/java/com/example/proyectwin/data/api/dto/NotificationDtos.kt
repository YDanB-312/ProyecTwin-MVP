package com.example.proyectwin.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Bandeja de notificaciones (`notifications`). */
@Serializable
data class NotificationDto(
    val id: Int = 0,
    val titulo: String = "",
    val descripcion: String? = null,
    val tipo: String = "sistema",
    val enlace: String? = null,
    val leida: Boolean = false,
    val fecha: String? = null,
    @SerialName("id_usuario") val idUsuario: Int = 0,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    val user: GeneralUserDto? = null,
)

/**
 * POST/PUT /notifications: incluye siempre los campos obligatorios del backend;
 * `enlace`/`descripcion` viajan aún en null (como el objeto completo del
 * frontend al marcar como leída).
 */
@Serializable
data class NotificationRequest(
    val titulo: String,
    val descripcion: String?,
    val tipo: String,
    val enlace: String?,
    val leida: Boolean,
    val fecha: String,
    @SerialName("id_usuario") val idUsuario: Int,
)
