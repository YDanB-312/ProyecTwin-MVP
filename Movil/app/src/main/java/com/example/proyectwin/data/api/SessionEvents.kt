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

    // 403 del middleware CambioContrasenaObligatorio: la sesión es válida, pero
    // la app debe llevar al usuario al cambio de la contraseña temporal.
    private val _mustChangePassword = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    val mustChangePassword: SharedFlow<Unit> = _mustChangePassword.asSharedFlow()

    fun notifyExpired() {
        _expired.tryEmit(Unit)
    }

    fun notifyMustChangePassword() {
        _mustChangePassword.tryEmit(Unit)
    }
}
