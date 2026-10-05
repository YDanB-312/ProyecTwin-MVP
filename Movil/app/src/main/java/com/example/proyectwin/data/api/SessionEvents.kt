package com.example.proyectwin.data.api

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Evento global de "sesión expirada" (HTTP 401), equivalente al evento
 * `auth:expirada` del frontend web. La capa de red lo emite y la UI lo
 * recolecta para redirigir al login y limpiar la sesión local.
 */
object SessionEvents {

    private val _expired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    val expired: SharedFlow<Unit> = _expired.asSharedFlow()

    fun notifyExpired() {
        _expired.tryEmit(Unit)
    }
}
