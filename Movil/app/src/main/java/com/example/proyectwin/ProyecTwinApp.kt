package com.example.proyectwin

import android.app.Application
import com.example.proyectwin.data.local.SessionManager
import com.example.proyectwin.notifications.UpdateNotificationManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Aplicación raíz. Inicializa la gráfica de dependencias de Hilt y migra el
 * token de sesión legado (texto plano) al almacén cifrado antes de usarlo.
 */
@HiltAndroidApp
class ProyecTwinApp : Application() {

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var updateNotificationManager: UpdateNotificationManager

    override fun onCreate() {
        super.onCreate()
        updateNotificationManager.crearCanal()
        CoroutineScope(Dispatchers.IO).launch {
            sessionManager.migrarTokenLegacy()
        }
    }
}
