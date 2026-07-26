package com.example.proyectwin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportIssueScreen(onBack: () -> Unit, onNavigate: (String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Scaffold(
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
                    text = "Enviar Reporte",
                    onClick = {
                        isLoading = true
                        scope.launch {
                            delay(1500)
                            isLoading = false
                            onBack()
                        }
                    },
                    isLoading = isLoading,
                    icon = Icons.AutoMirrored.Filled.Send,
                    modifier = Modifier.weight(1f)
                )
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
                title = "Reportar Falla",
                subtitle = "Ayúdanos a mejorar el sistema reportando cualquier error técnico que encuentres.",
                icon = Icons.Default.BugReport
            )

            SenaCard(elevation = 1.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    SenaTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = "Título de la falla *",
                        placeholder = "Ej: Error al cargar proyectos",
                        leadingIcon = Icons.Default.Title
                    )
                    
                    SenaTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = "Descripción detallada *",
                        placeholder = "Explica paso a paso qué sucedió...",
                        modifier = Modifier.heightIn(min = 120.dp)
                    )
                }
            }

            SenaSectionHeader(title = "Tus Reportes Recientes")
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(
                    Triple("#001", "Error en Dashboard", "Resuelto"),
                    Triple("#002", "Notificaciones duplicadas", "En Proceso"),
                    Triple("#003", "Botón no funciona", "Pendiente")
                ).forEach { (id, desc, status) ->
                    SenaCard(elevation = 0.5.dp) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(id, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = SenaGreen)
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(desc, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = SenaText)
                                Text("Hace 2 días", style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
                            }
                            SenaStatusBadge(status = status)
                        }
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReportIssueScreenPreview() {
    ProyecTwinTheme {
        ReportIssueScreen(onBack = {}, onNavigate = {})
    }
}
