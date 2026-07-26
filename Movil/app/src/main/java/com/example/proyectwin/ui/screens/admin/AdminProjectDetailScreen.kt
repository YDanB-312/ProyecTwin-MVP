package com.example.proyectwin.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
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
import com.example.proyectwin.navigation.AppNavigation
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProjectDetailScreen(projectId: String = "", onBack: () -> Unit, onNavigate: (String) -> Unit) {
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
                    text = "Ver Historial", 
                    onClick = { 
                        scope.launch {
                            snackbarHostState.showSnackbar("Historial de cambios no disponible")
                        }
                    }, 
                    isPrimary = false, 
                    modifier = Modifier.weight(1f)
                )
                SenaButton(text = "Ver Similitudes", onClick = { onNavigate(AppNavigation.ADMIN_SIMILARITY_DETAIL) }, modifier = Modifier.weight(1f))
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
                title = "Control de Proyecto",
                subtitle = "Supervisión detallada de la propuesta técnica y su estado en el sistema.",
                icon = Icons.Default.FolderSpecial
            )

            // Status Banner
            SenaCard(containerColor = Color.White, elevation = 2.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Estado Actual", style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
                        Spacer(Modifier.height(4.dp))
                        SenaStatusBadge(status = "Aprobado")
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Código Proyecto", style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
                        Text("#PRJ-2568", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Black, color = SenaText)
                    }
                }
            }

            SenaSectionHeader(title = "Información Técnica")
            SenaCard(elevation = 1.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Sistema IoT para Agricultura de Precisión", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SenaText)
                    HorizontalDivider(color = SenaBorderSoft)
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        AdminProjectDetailRow(Icons.Default.School, "Programa", "ADSO - Análisis y Desarrollo")
                        AdminProjectDetailRow(Icons.Default.Person, "Aprendiz Líder", "Maria Gonzalez")
                        AdminProjectDetailRow(Icons.Default.Badge, "Ficha", "ADSO-2568")
                        AdminProjectDetailRow(Icons.Default.CalendarToday, "Fecha Registro", "15/03/2026")
                        AdminProjectDetailRow(Icons.Default.SupervisorAccount, "Instructor", "Carlos Ruiz")
                    }
                }
            }

            SenaSectionHeader(title = "Contenido de la Propuesta")
            SenaCard {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Resumen Ejecutivo", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SenaTextLight)
                    Text(
                        "Desarrollo de un sistema basado en sensores IoT para monitorear variables ambientales en cultivos, permitiendo la toma de decisiones en tiempo real para optimizar el riego y la fertilización.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SenaTextSecondary,
                        lineHeight = 20.sp
                    )
                    
                    HorizontalDivider(color = SenaBorderSoft)
                    
                    Text("Palabras Clave", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SenaTextLight)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("IoT", "Agricultura", "Python").forEach { tag ->
                            SenaChip(text = tag, color = SenaGreen, isSelected = false)
                        }
                    }
                }
            }

            SenaSectionHeader(title = "Equipo de Trabajo")
            SenaCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf(
                        "Maria Gonzalez" to "Líder",
                        "Juan Pérez" to "Integrante",
                        "Laura Gómez" to "Integrante"
                    ).forEachIndexed { index, pair ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(36.dp),
                                shape = CircleShape,
                                color = if (index == 0) SenaGreen.copy(alpha = 0.1f) else SenaBorderSoft
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(pair.first.take(1), fontWeight = FontWeight.Bold, color = if (index == 0) SenaGreen else SenaTextMuted)
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(pair.first, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = SenaText)
                                Text(pair.second, style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun AdminProjectDetailRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = SenaTextLight, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = SenaTextLight, modifier = Modifier.width(120.dp))
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = SenaText)
    }
}

@Preview(showBackground = true)
@Composable
fun AdminProjectDetailScreenPreview() {
    ProyecTwinTheme {
        AdminProjectDetailScreen(onBack = {}, onNavigate = {})
    }
}
