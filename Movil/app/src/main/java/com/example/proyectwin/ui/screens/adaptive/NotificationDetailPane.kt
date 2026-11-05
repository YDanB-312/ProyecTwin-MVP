package com.example.proyectwin.ui.screens.adaptive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.proyectwin.data.model.Notification
import com.example.proyectwin.data.model.NotificationType
import com.example.proyectwin.ui.components.SenaButton
import com.example.proyectwin.ui.components.SenaCard
import com.example.proyectwin.ui.components.SenaEmptyState
import com.example.proyectwin.ui.components.SenaTopBar
import com.example.proyectwin.ui.theme.senaColors
import com.example.proyectwin.ui.util.formatearFechaLocal
import com.example.proyectwin.ui.viewmodel.NotificationsUiState
import com.example.proyectwin.ui.viewmodel.NotificationsViewModel

/** Panel derecho de notificaciones en tabletas. */
@Composable
fun NotificationDetailPane(
    notificationId: Int,
    resolverDestino: (Notification) -> String?,
    onClose: () -> Unit,
    onNavigate: (String) -> Unit,
    notificationsViewModel: NotificationsViewModel = hiltViewModel(),
) {
    val uiState by notificationsViewModel.uiState.collectAsState()
    val alert = (uiState as? NotificationsUiState.Success)
        ?.notifications
        ?.firstOrNull { it.id == notificationId }

    Scaffold(
        topBar = {
            SenaTopBar(
                title = "Notificación",
                onBack = onClose,
                showNotifications = false,
            )
        },
        containerColor = senaColors().background,
    ) { paddingValues ->
        if (alert == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                SenaEmptyState(
                    title = "Notificación no disponible",
                    message = "Puede que se haya eliminado o que la lista se esté actualizando.",
                    icon = Icons.Default.NotificationsNone,
                )
            }
        } else {
            val color = when (alert.notifType) {
                NotificationType.SIMILITUD, NotificationType.OBSERVACION -> senaColors().warning
                NotificationType.SISTEMA -> senaColors().info
                NotificationType.REVISION, NotificationType.MENSAJE -> senaColors().green
            }
            val categoria = when (alert.notifType) {
                NotificationType.SISTEMA -> "Sistema"
                NotificationType.SIMILITUD -> "Similitud"
                NotificationType.OBSERVACION, NotificationType.REVISION, NotificationType.MENSAJE -> "Instructor"
            }
            val destino = resolverDestino(alert)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    categoria.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = color,
                )
                Text(
                    alert.titulo ?: categoria,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = senaColors().text,
                )
                Text(
                    formatearFechaLocal(alert.createdAt ?: alert.fecha),
                    style = MaterialTheme.typography.labelSmall,
                    color = senaColors().textLight,
                )

                SenaCard(elevation = 1.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            alert.descripcion ?: alert.mensaje,
                            style = MaterialTheme.typography.bodyMedium,
                            color = senaColors().text,
                        )
                        if (!alert.descripcion.isNullOrBlank() &&
                            alert.descripcion != alert.mensaje
                        ) {
                            Text(
                                alert.mensaje,
                                style = MaterialTheme.typography.bodySmall,
                                color = senaColors().textSecondary,
                            )
                        }
                    }
                }

                SenaButton(
                    text = "Abrir destino",
                    onClick = { destino?.let(onNavigate) },
                    enabled = destino != null,
                    icon = Icons.AutoMirrored.Filled.OpenInNew,
                )
                Text(
                    if (destino == null) {
                        "Esta notificación no tiene un destino disponible; solo conserva el mensaje."
                    } else {
                        "Abre el proyecto, ficha o reporte relacionado."
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = senaColors().textLight,
                )
            }
        }
    }
}
