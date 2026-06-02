package com.example.proyectwin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.proyectwin.ui.components.SenaCard
import com.example.proyectwin.ui.components.SenaChip
import com.example.proyectwin.ui.components.SenaSegmentedFilter
import com.example.proyectwin.ui.components.SenaTopBar
import com.example.proyectwin.ui.theme.*

@Composable
fun AlertsScreen(onNavigate: (String) -> Unit) {
    var selectedFilter by remember { mutableStateOf("Todas") }
    val filters = listOf("Todas", "Urgentes", "Leídas", "Pendientes")

    val allAlerts = listOf(
        AlertItem(
            title = "Alta Similitud Detectada",
            description = "Tu proyecto \"Sistema de gestión Académica\" tiene un 65% de similitud con otro proyecto.",
            time = "Hace 2 horas",
            type = AlertType.URGENT,
            project = "Sistema de gestión Académica"
        ),
        AlertItem(
            title = "Revisión Pendiente",
            description = "Tu instructor Carlos Ruiz tiene pendiente la Revisión de tu proyecto \"App Móvil para Inventarios\".",
            time = "Ayer",
            type = AlertType.WARNING,
            project = "App Móvil para Inventarios"
        ),
        AlertItem(
            title = "Comentario del Instructor",
            description = "Carlos Ruiz ha agregado comentarios a tu proyecto \"Sistema de gestión Académica\".",
            time = "Hace 3 días",
            type = AlertType.INFO,
            project = "Sistema de gestión Académica"
        ),
        AlertItem(
            title = "Proyecto Aprobado",
            description = "¡Felicidades! Tu proyecto \"App Móvil para Inventarios\" ha sido aprobado.",
            time = "Hace 1 semana",
            type = AlertType.SUCCESS,
            project = "App Móvil para Inventarios"
        )
    )

    val filteredAlerts = when (selectedFilter) {
        "Urgentes" -> allAlerts.filter { it.type == AlertType.URGENT }
        "Pendientes" -> allAlerts.filter { it.type == AlertType.WARNING }
        else -> allAlerts
    }

    Scaffold(
        topBar = {
            SenaTopBar(title = "Notificaciones", showNotifications = false)
        },
        containerColor = SenaBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SenaSegmentedFilter(
                options = filters,
                selectedOption = selectedFilter,
                onOptionSelected = { selectedFilter = it },
                modifier = Modifier.padding(16.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredAlerts) { alert ->
                    AlertCard(alert, onClick = {
                        if (alert.type == AlertType.URGENT) {
                            onNavigate("project/similarity/1")
                        } else {
                            onNavigate("project/detail/1")
                        }
                    })
                }
            }
        }
    }
}

@Composable
fun AlertCard(alert: AlertItem, onClick: () -> Unit) {
    val (icon, color) = when (alert.type) {
        AlertType.URGENT -> Icons.Default.Warning to SenaDanger
        AlertType.WARNING -> Icons.Default.History to SenaWarning
        AlertType.INFO -> Icons.AutoMirrored.Filled.Comment to SenaGreen
        AlertType.SUCCESS -> Icons.Default.CheckCircle to SenaSuccess
    }

    SenaCard(elevation = 2.dp, onClick = onClick) {
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
            
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        alert.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = SenaText,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        alert.time,
                        style = MaterialTheme.typography.labelSmall,
                        color = SenaTextLight
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    alert.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SenaTextLight
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Folder,
                        contentDescription = null,
                        tint = SenaTextLight,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        alert.project,
                        style = MaterialTheme.typography.labelSmall,
                        color = SenaTextLight,
                        modifier = Modifier.weight(1f)
                    )
                    SenaChip(
                        text = alert.type.label,
                        color = color
                    )
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
    val project: String
)

@Preview(showBackground = true)
@Composable
fun AlertsScreenPreview() {
    ProyecTwinTheme {
        AlertsScreen(onNavigate = {})
    }
}
