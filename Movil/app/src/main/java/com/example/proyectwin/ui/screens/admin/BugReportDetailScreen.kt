package com.example.proyectwin.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BugReportDetailScreen(bugId: String = "", onBack: () -> Unit) {
    val scrollState = rememberScrollState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SenaTopBar(
                title = "ProyecTwin",
                onBack = onBack,
                showProfile = true,
                showNotifications = true
            )
        },
        containerColor = SenaBackground,
        bottomBar = {
            SenaBottomBar {
                SenaButton(
                    text = "Asignar Técnico", 
                    onClick = { 
                        scope.launch {
                            snackbarHostState.showSnackbar("Reporte asignado al equipo técnico")
                        }
                    }, 
                    modifier = Modifier.weight(1f)
                )
                SenaButton(text = "Cerrar Caso", onClick = { onBack() }, isPrimary = false, modifier = Modifier.weight(1f))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            SenaPageHeader(
                title = "Detalle del Reporte",
                subtitle = "Gestión de fallas críticas y errores técnicos del sistema.",
                icon = Icons.Default.BugReport
            )

            // Status Card
            SenaCard(elevation = 1.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SenaStatusBadge(status = "En Revisión")
                        Text("#$bugId", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = SenaTextLight)
                    }

                    Text("Error al cargar el módulo de similitudes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SenaText)
                    
                    HorizontalDivider(color = SenaBorderSoft)
                    
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        BugDetailRow(Icons.Default.Person, "Reportado por", "Maria Gonzalez")
                        BugDetailRow(Icons.Default.CalendarToday, "Fecha", "08/04/2026")
                        BugDetailRow(Icons.Default.PriorityHigh, "Prioridad", "Alta")
                    }
                }
            }

            SenaSectionHeader(title = "Descripción Técnica")
            SenaCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Detalles de la falla", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SenaTextLight)
                    Text(
                        "Al intentar acceder al módulo de detección de similitudes, el sistema muestra un error 500 interno.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SenaTextSecondary,
                        lineHeight = 20.sp
                    )
                }
            }

            SenaSectionHeader(title = "Historial de Actividad")
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                BugActivityItem("Admin Principal", "Asignó el reporte al equipo de desarrollo.", "09/04/2026 10:30")
                BugActivityItem("Técnico Soporte", "Identificó problema de compatibilidad PHP.", "09/04/2026 14:15")
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun BugDetailRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = SenaTextLight, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = SenaTextLight, modifier = Modifier.width(100.dp))
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = SenaText)
    }
}

@Composable
fun BugActivityItem(user: String, text: String, date: String) {
    SenaCard(elevation = 0.5.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(user, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = SenaGreen)
                Text(date, style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
            }
            Text(text, style = MaterialTheme.typography.bodySmall, color = SenaTextSecondary)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BugReportDetailScreenPreview() {
    ProyecTwinTheme {
        BugReportDetailScreen(bugId = "001", onBack = {})
    }
}
