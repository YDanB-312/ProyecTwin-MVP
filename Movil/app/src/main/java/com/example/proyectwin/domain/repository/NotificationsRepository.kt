package com.example.proyectwin.domain.repository

import com.example.proyectwin.data.model.Notification

/** Bandeja de notificaciones (`notifications`). */
interface NotificationsRepository {

    suspend fun listar(idUsuario: Int? = null): Result<List<Notification>>

    /** PUT completo con `leida` en true (como hace el frontend). */
    suspend fun marcarComoLeida(notificacion: Notification): Result<Notification>

    suspend fun eliminar(id: Int): Result<Unit>
}
