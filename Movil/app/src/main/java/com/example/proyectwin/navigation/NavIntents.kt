package com.example.proyectwin.navigation

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** Destinos globales que cualquier pantalla puede pedir desde el top bar. */
enum class NavIntent { NOTIFICACIONES, PERFIL }

/**
 * Canal único para acciones de navegación que viven en componentes compartidos
 * (campana y avatar de [com.example.proyectwin.ui.components.SenaTopBar]) y que
 * no tienen acceso al NavController. MainActivity escucha y traduce la
 * intención a la ruta del rol autenticado, de modo que la campana funcione
 * igual desde cualquier pantalla sin duplicar callbacks.
 */
object NavIntents {
    private val _destinos = MutableSharedFlow<NavIntent>(extraBufferCapacity = 4)
    val destinos = _destinos.asSharedFlow()

    fun pedirNotificaciones() {
        _destinos.tryEmit(NavIntent.NOTIFICACIONES)
    }

    fun pedirPerfil() {
        _destinos.tryEmit(NavIntent.PERFIL)
    }
}
