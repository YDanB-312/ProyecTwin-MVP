package com.example.proyectwin.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectwin.navigation.AppNavigation
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*

data class BugReport(
    val id: Int,
    val user: String,
    val description: String,
    val status: String,
    val date: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BugReportsScreen(onBack: () -> Unit, onNavigate: (String) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("Todos") }
    val statuses = listOf("Todos", "Pendiente", "En Revisión", "Resuelto", "Rechazado")

    val reports = remember {
        listOf(
            BugReport(1, "Carlos Rodríguez Díaz", "Error al cargar la página de Dashboard, muestra pantalla blanca.", "Pendiente", "12/04/2026"),
            BugReport(2, "Maria González Torres", "No se pueden subir archivos PDF en la sección de evidencias.", "Pendiente", "11/04/2026"),
            BugReport(3, "Andrés Martínez López", "El sistema no envía Notificaciones cuando un instructor revisa un proyecto.", "En Revisión", "10/04/2026"),
            BugReport(4, "Laura Sánchez Pérez", "El botón de Cerrar sesión no funciona correctamente en navegador Chrome.", "En Revisión", "09/04/2026"),
            BugReport(5, "Diego Ramírez Castro", "Error en la generación de reportes PDF, el archivo descargado está corrupto.", "Resuelto", "08/04/2026"),
        )
    }

    val filteredReports = reports.filter { report ->
        val matchesStatus = if (selectedStatus == "Todos") true else report.status == selectedStatus
        val matchesSearch = report.user.contains(searchQuery, ignoreCase = true) || report.description.contains(searchQuery, ignoreCase = true)
        matchesStatus && matchesSearch
    }

    Scaffold(
        topBar = {
            SenaTopBar(
                title = "ProyecTwin",
                onBack = onBack,
                showProfile = true,
                showNotifications = true
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
                    title = "Reportes de Fallas",
                    subtitle = "Supervisa y gestiona los errores técnicos reportados por los usuarios.",
                    icon = Icons.Default.BugReport
                )
            }

            // Filter Section
            item {
                SenaCard(elevation = 1.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            "Filtros de búsqueda",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SenaTextLight,
                            letterSpacing = 0.5.sp
                        )
                        SenaTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = "",
                            placeholder = "Buscar por usuario o descripción...",
                            leadingIcon = Icons.Default.Search
                        )
                        
                        Text(
                            "Estado del reporte",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SenaTextLight,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            statuses.forEach { status ->
                                SenaChip(
                                    text = status,
                                    color = when(status) {
                                        "Resuelto" -> SenaSuccess
                                        "Pendiente" -> SenaWarning
                                        "En Revisión" -> SenaInfo
                                        "Rechazado" -> SenaDanger
                                        else -> SenaGreen
                                    },
                                    isSelected = selectedStatus == status,
                                    onClick = { selectedStatus = status }
                                )
                            }
                        }
                    }
                }
            }

            if (filteredReports.isEmpty()) {
                item {
                    SenaEmptyState(
                        message = "No se encontraron reportes que coincidan con la búsqueda.",
                        icon = Icons.Default.SearchOff
                    )
                }
            } else {
                items(filteredReports) { report ->
                    BugReportCard(report) {
                        onNavigate(AppNavigation.ADMIN_BUG_DETAIL.replace("{bugId}", report.id.toString()))
                    }
                }
            }
            
            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}

@Composable
fun BugReportCard(report: BugReport, onClick: () -> Unit) {
    SenaCard(elevation = 1.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = SenaBorderSoft,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "#${report.id}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = SenaTextSecondary
                    )
                }
                SenaStatusBadge(status = report.status)
            }

            Column {
                Text(
                    report.description, 
                    style = MaterialTheme.typography.bodySmall, 
                    fontWeight = FontWeight.Bold, 
                    color = SenaText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = SenaGreen, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(report.user, style = MaterialTheme.typography.labelSmall, color = SenaTextSecondary)
                    Spacer(Modifier.weight(1f))
                    Text(report.date, style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
                }
            }

            HorizontalDivider(color = SenaBorderSoft)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onClick) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Ver Detalle", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onClick) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Gestionar", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BugReportsScreenPreview() {
    ProyecTwinTheme {
        BugReportsScreen(onBack = {}, onNavigate = {})
    }
}
