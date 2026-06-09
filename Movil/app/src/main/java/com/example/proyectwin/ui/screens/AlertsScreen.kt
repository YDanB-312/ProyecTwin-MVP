package com.example.proyectwin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*

@Composable
fun AlertsScreen(onNavigate: (String) -> Unit) {
    var selectedFilter by remember { mutableStateOf("Todos") }
    val filters = listOf("Todos", "Similitud", "Instructor", "Sistema")

    val allAlerts = listOf(
        AlertItem(
            title = "Alta Similitud Detectada",
            description = "Tu proyecto \"Sistema de gestion Academica\" tiene un 65% de similitud con otro proyecto.",
            time = "Hace 2 horas",
            type = AlertType.URGENT,
            project = "Sistema de gestion Academica",
            category = "Similitud",
            isNew = true
        ),
        AlertItem(
            title = "Revision Pendiente",
            description = "Tu instructor Carlos Ruiz tiene pendiente la Revision de tu proyecto \"App Movil para Inventarios\".",
            time = "Ayer",
            type = AlertType.WARNING,
            project = "App Movil para Inventarios",
            category = "Instructor",
            isNew = false
        ),
        AlertItem(
            title = "Comentario del Instructor",
            description = "Carlos Ruiz ha agregado comentarios a tu proyecto \"Sistema de gestion Academica\".",
            time = "Hace 3 dias",
            type = AlertType.INFO,
            project = "Sistema de gestion Academica",
            category = "Instructor",
            isNew = false
        ),
        AlertItem(
            title = "proyecto Aprobado",
            description = "¡Felicidades! Tu proyecto \"App Movil para Inventarios\" ha sido aprobado por el instructor.",
            time = "Hace 1 semana",
            type = AlertType.SUCCESS,
            project = "App Movil para Inventarios",
            category = "Sistema",
            isNew = false
        )
    )

    val filteredAlerts = if (selectedFilter == "Todos") {
        allAlerts
    } else {
        allAlerts.filter { it.category == selectedFilter }
    }

    Scaffold(
        topBar = {
            SenaTopBar(
                title = "Notificaciones",
                showNotifications = false,
                onNavigateToProfile = { onNavigate("main/profile") }
            )
        },
        containerColor = SenaBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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

            if (filteredAlerts.isEmpty()) {
                EmptyStateCard(
                    message = "No hay notificaciones en esta categoría",
                    icon = Icons.Default.NotificationsNone
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredAlerts) { alert ->
                        AlertCardV3(alert, onClick = {
                            if (alert.category == "Similitud") onNavigate("project/similarity/1")
                            else onNavigate("project/detail/1")
                        })
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
fun AlertCardV3(alert: AlertItem, onClick: () -> Unit) {
    val (icon, color) = when (alert.type) {
        AlertType.URGENT -> Icons.Default.Warning to SenaDanger
        AlertType.WARNING -> Icons.Default.History to SenaWarning
        AlertType.INFO -> Icons.AutoMirrored.Filled.Comment to SenaGreen
        AlertType.SUCCESS -> Icons.Default.CheckCircle to SenaSuccess
    }

    SenaCard(
        elevation = if (alert.isNew) 2.dp else 0.5.dp,
        containerColor = if (alert.isNew) Color.White else Color.White.copy(alpha = 0.8f),
        onClick = onClick
    ) {
        Row(verticalAlignment = Alignment.Top) {
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
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                    Text(
                        alert.time,
                        style = MaterialTheme.typography.labelSmall,
                        color = SenaTextMuted
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    alert.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = SenaText
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    alert.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = SenaTextSecondary
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Folder,
                        contentDescription = null,
                        tint = SenaTextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        alert.project,
                        style = MaterialTheme.typography.labelSmall,
                        color = SenaTextMuted,
                        modifier = Modifier.weight(1f)
                    )
                    if (alert.isNew) {
                        Surface(modifier = Modifier.size(8.dp), shape = CircleShape, color = SenaGreen) {}
                    }
                }
            }
        }
    }
}

enum class AlertType(val label: String) {
    URGENT("Urgente"),
    WARNING("Pendiente"),
    INFO("Comentario"),
    SUCCESS("Éxito")
}

data class AlertItem(
    val title: String,
    val description: String,
    val time: String,
    val type: AlertType,
    val project: String,
    val category: String,
    val isNew: Boolean
)

@Preview(showBackground = true)
@Composable
fun AlertsScreenPreview() {
    ProyecTwinTheme {
        AlertsScreen(onNavigate = {})
    }
}
