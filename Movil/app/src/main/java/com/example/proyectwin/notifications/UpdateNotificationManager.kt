package com.example.proyectwin.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.proyectwin.MainActivity
import com.example.proyectwin.R
import com.example.proyectwin.data.local.UpdatePreferences
import com.example.proyectwin.domain.util.UpdateNotificationPolicy
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Notificación local de una nueva versión. No es un servicio push: se emite
 * cuando la app ya consultó el endpoint de versión y sigue en ejecución.
 */
@Singleton
class UpdateNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: UpdatePreferences,
) {

    companion object {
        const val CHANNEL_ID = "proyectwin_updates"
        const val EXTRA_ABRIR_ACTUALIZACIONES = "abrir_actualizaciones"
        private const val NOTIFICATION_ID = 1405
    }

    /** Debe ejecutarse al iniciar la aplicación, antes de notificar. */
    fun crearCanal() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val canal = NotificationChannel(
            CHANNEL_ID,
            "Actualizaciones de ProyecTwin",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Avisos de nuevas versiones de la aplicación Android."
        }
        manager.createNotificationChannel(canal)
    }

    /**
     * Publica la notificación una sola vez por versión y solo si el usuario
     * concedió el permiso y mantiene las notificaciones habilitadas.
     */
    suspend fun notificarSiCorresponde(version: String?, notas: String?): Boolean {
        if (version.isNullOrBlank()) return false
        val permiso = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        val habilitadas = NotificationManagerCompat.from(context).areNotificationsEnabled()
        val yaNotificada = prefs.versionNotificada()
        if (!UpdateNotificationPolicy.debeNotificar(version, yaNotificada, permiso, habilitadas)) {
            return false
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra(EXTRA_ABRIR_ACTUALIZACIONES, true)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val detalle = buildString {
            append("La versión ")
            append(version)
            append(" está disponible. Toca para revisar la actualización.")
            notas?.trim()?.takeIf { it.isNotEmpty() }?.let {
                append("\n")
                append(it.take(180))
            }
        }

        val notificacion = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_update)
            .setContentTitle("Nueva versión de ProyecTwin")
            .setContentText("La versión $version está disponible.")
            .setStyle(NotificationCompat.BigTextStyle().bigText(detalle))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(
                R.drawable.ic_notification_update,
                "Revisar actualización",
                pendingIntent,
            )
            .build()

        return try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notificacion)
            prefs.marcarNotificada(version)
            true
        } catch (_: SecurityException) {
            false
        }
    }
}
