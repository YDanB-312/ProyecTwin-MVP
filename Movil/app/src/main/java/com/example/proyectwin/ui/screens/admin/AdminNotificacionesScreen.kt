package com.example.proyectwin.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectwin.data.mock.MockDataProvider
import com.example.proyectwin.data.model.Notification
import com.example.proyectwin.data.model.NotificationType
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*
import com.example.proyectwin.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNotificacionesScreen(
    onBack: () -> Unit,
    onNavigateToUser: (String) -> Unit,
    onNavigateToReport: (String) -> Unit,
    onNavigateToSimilarity: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val authState by authViewModel.uiState.collectAsState()
    val userId = authState.user?.id ?: 1

    val notifications = remember(userId) { MockDataProvider.getNotificationsByUser(userId) }

    Scaffold(
        topBar = {
            SenaTopBar(
                title = "Notificaciones Admin",
                onBack = onBack,
                showProfile = true,
                showNotifications = false
            )
        },
        containerColor = senaColors().background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                SenaPageHeader(
                    title = "Centro de Control",
                    subtitle = "Alertas globales del sistema y reportes de actividad.",
                    icon = Icons.Default.Notifications
                )
            }

            items(notifications) { notification ->
                AdminNotificationCard(notification = notification) {
                    val module = detectNotificationModule(notification.mensaje)
                    when (module) {
                        "Usuarios" -> onNavigateToUser("1")
                        "Reportes" -> onNavigateToReport("1")
                        "Similitudes" -> onNavigateToSimilarity()
                        else -> {}
                    }
                }
            }

            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}

private fun detectNotificationModule(mensaje: String): String {
    val lower = mensaje.lowercase()
    return when {
        lower.contains("reporte") || lower.contains("bug") -> "Reportes"
        lower.contains("similitud") -> "Similitudes"
        lower.contains("usuario") || lower.contains("bienvenido") || lower.contains("registrado") -> "Usuarios"
        lower.contains("proyecto") -> "Proyectos"
        else -> "Sistema"
    }
}

private fun notifIcon(notifType: NotificationType): ImageVector = when (notifType) {
    NotificationType.INFO -> Icons.Default.Info
    NotificationType.WARNING -> Icons.Default.Warning
    NotificationType.SUCCESS -> Icons.Default.CheckCircle
    NotificationType.ERROR -> Icons.Default.Error
}

@Composable
private fun notifColor(notifType: NotificationType): Color = when (notifType) {
    NotificationType.INFO -> senaColors().info
    NotificationType.WARNING -> senaColors().warning
    NotificationType.SUCCESS -> senaColors().success
    NotificationType.ERROR -> senaColors().danger
}

@Composable
private fun notifModuleColor(module: String): Color = when (module) {
    "Similitudes" -> senaColors().danger
    "Reportes" -> senaColors().warning
    "Usuarios" -> senaColors().info
    else -> senaColors().green
}

@Composable
fun AdminNotificationCard(notification: Notification, onClick: () -> Unit) {
    val module = detectNotificationModule(notification.mensaje)
    val color = notifModuleColor(module)
    val icon = notifIcon(notification.notifType)
    val isRead = notification.leido

    SenaCard(
        elevation = if (isRead) 0.5.dp else 2.dp,
        onClick = onClick,
        containerColor = if (isRead) senaColors().backgroundElevated.copy(alpha = 0.8f) else senaColors().backgroundElevated
    ) {
        Row(verticalAlignment = Alignment.Top) {
            // Left Indicator
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(64.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = color.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        module.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = color,
                        letterSpacing = 0.5.sp
                    )
                    Text(notification.createdAt ?: "", style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(notification.mensaje, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = senaColors().text)

                Spacer(modifier = Modifier.height(4.dp))

                if (!isRead) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(color = senaColors().green.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                        Text(
                            "PENDIENTE",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = senaColors().green,
                            fontSize = 8.sp
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdminNotificacionesPreview() {
    ProyecTwinTheme {
        AdminNotificacionesScreen(
            onBack = {},
            onNavigateToUser = {},
            onNavigateToReport = {},
            onNavigateToSimilarity = {}
        )
    }
}
