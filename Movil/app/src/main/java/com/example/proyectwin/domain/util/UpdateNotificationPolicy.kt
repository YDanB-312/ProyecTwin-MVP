package com.example.proyectwin.domain.util

/**
 * Política pura de la notificación local de actualizaciones. Se separa de
 * Android para poder probarla sin dispositivo y para no duplicar reglas entre
 * el ViewModel y el notificador.
 */
object UpdateNotificationPolicy {

    fun debeNotificar(
        versionNueva: String?,
        versionYaNotificada: String?,
        permisoConcedido: Boolean,
        notificacionesHabilitadas: Boolean,
    ): Boolean {
        if (versionNueva.isNullOrBlank()) return false
        if (!permisoConcedido || !notificacionesHabilitadas) return false
        return versionNueva != versionYaNotificada
    }
}
