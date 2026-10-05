package com.example.proyectwin.domain.usecase

import com.example.proyectwin.domain.repository.NotificationsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

/**
 * Marca todas las notificaciones no leídas de un usuario (el frontend usa
 * `Promise.all` sobre PUT /notifications/{id}); devuelve cuántas se marcaron.
 */
class MarkAllNotificationsReadUseCase @Inject constructor(
    private val notifications: NotificationsRepository,
) {

    suspend operator fun invoke(idUsuario: Int): Result<Int> {
        val lista = notifications.listar(idUsuario).getOrElse { return Result.failure(it) }
        val pendientes = lista.filter { !it.leido }
        if (pendientes.isEmpty()) return Result.success(0)

        val resultados = coroutineScope {
            pendientes
                .map { notificacion -> async { notifications.marcarComoLeida(notificacion) } }
                .awaitAll()
        }
        return Result.success(resultados.count { it.isSuccess })
    }
}
