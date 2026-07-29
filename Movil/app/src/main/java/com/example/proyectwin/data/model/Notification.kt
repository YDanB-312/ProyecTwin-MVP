package com.example.proyectwin.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Notification(
    val id: Int,
    val mensaje: String,
    val tipo: String = NotificationType.INFO.value,
    val userId: Int? = null,
    val projectId: Int? = null,
    val leido: Boolean = false,
    val createdAt: String? = null
) {
    val notifType: NotificationType get() = NotificationType.fromValue(tipo)
}
