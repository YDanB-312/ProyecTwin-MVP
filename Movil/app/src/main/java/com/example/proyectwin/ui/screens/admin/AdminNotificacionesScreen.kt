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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*

data class AdminNotification(
    val title: String,
    val desc: String,
    val time: String,
    val module: String,
    val type: String,
    val isRead: Boolean,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNotificacionesScreen(
    onBack: () -> Unit,
    onNavigateToUser: (String) -> Unit,
    onNavigateToReport: (String) -> Unit,
    onNavigateToSimilarity: () -> Unit
) {
    val notifications = remember {
        listOf(
            AdminNotification("Nuevo Usuario Registrado", "Se ha registrado un nuevo aprendiz en el programa ADSO. Revisa los detalles.", "Hace 1h", "Usuarios", "sistema", false, Icons.Default.PersonAdd),
            AdminNotification("Reporte de Falla Recibido", "Carlos Rodriguez ha reportado una falla en el módulo de similitudes.", "Hace 2h", "Reportes", "revision", false, Icons.Default.BugReport),
            AdminNotification("Alerta de Seguridad", "Se ha detectado un intento de acceso no autorizado desde una IP desconocida.", "Ayer", "Sistema", "sistema", false, Icons.Default.Shield),
            AdminNotification("Similitud Crítica Detectada", "El proyecto 'Gestión Académica' presenta un 65% de similitud.", "Hace 3d", "Similitudes", "similitud", true, Icons.Default.Warning),
        )
    }

    Scaffold(
        topBar = {
            SenaTopBar(
                title = "Notificaciones Admin",
                onBack = onBack,
                showProfile = true,
                showNotifications = false
            )
        },
        containerColor = SenaBackground
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
                AdminNotificationCard(notification) {
                    when (notification.module) {
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

@Composable
fun AdminNotificationCard(notification: AdminNotification, onClick: () -> Unit) {
    val color = when (notification.type) {
        "similitud" -> SenaDanger
        "revision" -> SenaWarning
        "sistema" -> SenaGreen
        else -> SenaInfo
    }

    SenaCard(
        elevation = if (notification.isRead) 0.5.dp else 2.dp,
        onClick = onClick,
        containerColor = if (notification.isRead) Color.White.copy(alpha = 0.8f) else Color.White
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
                    Icon(notification.icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        notification.module.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = color,
                        letterSpacing = 0.5.sp
                    )
                    Text(notification.time, style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(notification.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SenaText)
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    notification.desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = SenaTextSecondary,
                    lineHeight = 18.sp
                )
                
                if (!notification.isRead) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(color = SenaGreen.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                        Text(
                            "PENDIENTE",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SenaGreen,
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
        AdminNotificacionesScreen(onBack = {}, onNavigateToUser = {}, onNavigateToReport = {}, onNavigateToSimilarity = {})
    }
}
