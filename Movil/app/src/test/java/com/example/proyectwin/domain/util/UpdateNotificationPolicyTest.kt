package com.example.proyectwin.domain.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateNotificationPolicyTest {

    @Test
    fun `notifica una version nueva con permiso y canal habilitado`() {
        assertTrue(
            UpdateNotificationPolicy.debeNotificar(
                versionNueva = "1.5.0",
                versionYaNotificada = null,
                permisoConcedido = true,
                notificacionesHabilitadas = true,
            ),
        )
    }

    @Test
    fun `no repite la notificacion de la misma version`() {
        assertFalse(
            UpdateNotificationPolicy.debeNotificar(
                versionNueva = "1.5.0",
                versionYaNotificada = "1.5.0",
                permisoConcedido = true,
                notificacionesHabilitadas = true,
            ),
        )
    }

    @Test
    fun `una version distinta vuelve a notificar`() {
        assertTrue(
            UpdateNotificationPolicy.debeNotificar(
                versionNueva = "1.6.0",
                versionYaNotificada = "1.5.0",
                permisoConcedido = true,
                notificacionesHabilitadas = true,
            ),
        )
    }

    @Test
    fun `sin permiso o con notificaciones deshabilitadas no notifica`() {
        assertFalse(
            UpdateNotificationPolicy.debeNotificar("1.5.0", null, false, true),
        )
        assertFalse(
            UpdateNotificationPolicy.debeNotificar("1.5.0", null, true, false),
        )
        assertFalse(
            UpdateNotificationPolicy.debeNotificar(null, null, true, true),
        )
    }
}
