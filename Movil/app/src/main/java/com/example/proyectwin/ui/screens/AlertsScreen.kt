package com.example.proyectwin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*

enum class AlertType(val label: String) {
    URGENT("Urgente"),
    WARNING("Advertencia"),
    INFO("Informativa"),
    SUCCESS("Éxito")
}

data class AlertItem(
    val title: String,
    val description: String,
    val time: String,
    val type: AlertType,
    val category: String,
    val isNew: Boolean,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    onNavigate: (String) -> Unit,
    onBack: () -> Unit = {},
    profileRoute: String = "aprendiz_profile",
    similarityRoute: String = "aprendiz_similarity",
    detailRoute: String = "aprendiz_detail/{id}",
) {
    var selectedFilter by remember { mutableStateOf("Todos") }
    val filters = listOf("Todos", "Similitud", "Instructor", "Sistema")

    val allAlerts = remember {
        listOf(
            AlertItem("Alta Similitud Detectada", "Tu proyecto \"Sistema de Gestión Académica\" tiene un 65% de similitud detectada por la IA.", "Hace 2 horas", AlertType.URGENT, "Similitud", isNew = true),
            AlertItem("Revisión Pendiente", "Tu instructor tiene pendiente la revisión de tu propuesta de proyecto.", "Ayer", AlertType.WARNING, "Instructor", isNew = false),
            AlertItem("Comentario del Instructor", "Se ha agregado una nueva observación a tu proyecto.", "Hace 3 días", AlertType.INFO, "Instructor", isNew = false),
            AlertItem("Proyecto Aprobado", "¡Felicidades! Tu proyecto ha sido aprobado exitosamente.", "Hace 1 semana", AlertType.SUCCESS, "Sistema", isNew = false),
        )
    }

    val filteredAlerts = if (selectedFilter == "Todos") {
        allAlerts
    } else {
        allAlerts.filter { it.category == selectedFilter }
    }

    Scaffold(
        topBar = {
            SenaTopBar(
                title = "Notificaciones",
                onBack = onBack,
                showNotifications = false,
                onNavigateToProfile = { onNavigate(profileRoute) },
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
                    title = "Centro de Alertas",
                    subtitle = "Mantente al día con el estado de tus proyectos y observaciones.",
                    icon = Icons.Default.Notifications
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filters) { filter ->
                        SenaChip(
                            text = filter,
                            color = SenaGreen,
                            isSelected = selectedFilter == filter,
                            onClick = { selectedFilter = filter }
                        )
                    }
                }
            }

            if (filteredAlerts.isEmpty()) {
                item {
                    SenaEmptyState(
                        message = "No tienes notificaciones en esta categoría.",
                        icon = Icons.Default.NotificationsNone
                    )
                }
            } else {
                items(filteredAlerts) { alert ->
                    NotificationCard(alert, onClick = {
                        if (alert.category == "Similitud") onNavigate(similarityRoute)
                        else onNavigate(detailRoute.replace("{id}", "1"))
                    })
                }
            }
            
            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}

@Composable
fun NotificationCard(alert: AlertItem, onClick: () -> Unit) {
    val (icon, color) = when (alert.type) {
        AlertType.URGENT -> Icons.Default.Warning to SenaDanger
        AlertType.WARNING -> Icons.Default.History to SenaWarning
        AlertType.INFO -> Icons.AutoMirrored.Filled.Comment to SenaGreen
        AlertType.SUCCESS -> Icons.Default.CheckCircle to SenaSuccess
    }

    SenaCard(
        elevation = if (alert.isNew) 2.dp else 0.5.dp,
        onClick = onClick,
        containerColor = Color.White
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Left Accent Border (Fidelity to .tarjeta-alerta in CSS)
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        alert.category.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = color,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        alert.time,
                        style = MaterialTheme.typography.labelSmall,
                        color = SenaTextLight
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    alert.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = SenaText
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    alert.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = SenaTextSecondary,
                    lineHeight = 18.sp
                )
                
                if (alert.isNew) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = SenaGreen.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "NUEVO",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SenaGreen,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AlertsScreenPreview() {
    ProyecTwinTheme {
        AlertsScreen(onNavigate = {})
    }
}
