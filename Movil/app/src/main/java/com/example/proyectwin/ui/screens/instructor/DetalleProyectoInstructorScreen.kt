package com.example.proyectwin.ui.screens.instructor

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleProyectoInstructorScreen(projectId: String = "", onBack: () -> Unit) {
    val scrollState = rememberScrollState()
    var observationText by remember { mutableStateOf("") }

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
                SenaButton(text = "Aprobar", onClick = { onBack() }, modifier = Modifier.weight(1f))
                SenaButton(text = "Rechazar", onClick = { onBack() }, isPrimary = false, modifier = Modifier.weight(1f), containerColor = SenaDanger)
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
                title = "Revisión de Proyecto",
                subtitle = "Evaluación detallada de la propuesta enviada por el aprendiz.",
                icon = Icons.Default.FolderOpen
            )

            // Info Card
            SenaCard(elevation = 1.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SenaStatusBadge(status = "Pendiente")
                        Surface(color = SenaDanger.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                            Text(
                                "45% Similitud", 
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SenaDanger
                            )
                        }
                    }

                    Text("Sistema IoT para Agricultura", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SenaText)
                    
                    HorizontalDivider(color = SenaBorderSoft)
                    
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        InstructorDetailRow(Icons.Default.Person, "Aprendiz", "Ana Martínez")
                        InstructorDetailRow(Icons.Default.School, "Programa", "ADSO - Trimestre 3")
                        InstructorDetailRow(Icons.Default.CalendarToday, "Fecha", "15/11/2026")
                        InstructorDetailRow(Icons.Default.Work, "Área", "Tecnología e Informática")
                    }
                }
            }

            SenaSectionHeader(title = "Resumen del Proyecto")
            SenaCard {
                Text(
                    "Sistema de monitoreo inteligente para cultivos utilizando sensores IoT que miden humedad, temperatura y nutrientes del suelo, permitiendo la toma de decisiones en tiempo real.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SenaTextSecondary,
                    lineHeight = 22.sp
                )
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("IoT", "Sensores", "Sena").forEach { tag ->
                        SenaChip(text = tag, color = SenaTextMuted, isSelected = false)
                    }
                }
            }

            SenaSectionHeader(title = "Objetivos y Entregables")
            SenaCard {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Objetivos Específicos", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SenaTextLight)
                    listOf(
                        "Diseñar una red de sensores IoT.",
                        "Desarrollar plataforma web de visualización.",
                        "Implementar algoritmos de alerta."
                    ).forEach { obj ->
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.Circle, contentDescription = null, tint = SenaGreen, modifier = Modifier.size(8.dp).padding(top = 4.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(obj, style = MaterialTheme.typography.bodySmall, color = SenaTextSecondary)
                        }
                    }
                }
            }

            SenaSectionHeader(title = "Observaciones")
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SenaCard(elevation = 1.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Carlos Ruiz | Instructor", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = SenaText)
                            Text("10 may 2026", style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
                        }
                        Text(
                            "El proyecto necesita mejorar la sección de análisis de requisitos. Se recomienda ampliar la documentación técnica.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SenaTextSecondary
                        )
                    }
                }

                // Add observation box
                SenaCard(containerColor = Color.White, elevation = 2.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Agregar Observación", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = SenaText)
                        SenaTextField(
                            value = observationText,
                            onValueChange = { observationText = it },
                            label = "",
                            placeholder = "Escribe tu comentario aquí...",
                            modifier = Modifier.heightIn(min = 100.dp)
                        )
                        SenaButton(
                            text = "Enviar Comentario",
                            onClick = { observationText = "" },
                            icon = Icons.AutoMirrored.Filled.Send,
                            modifier = Modifier.align(Alignment.End).width(180.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun InstructorDetailRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = SenaTextLight, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = SenaTextLight, modifier = Modifier.width(100.dp))
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = SenaText)
    }
}

@Preview(showBackground = true)
@Composable
fun DetalleProyectoInstructorScreenPreview() {
    ProyecTwinTheme {
        DetalleProyectoInstructorScreen(onBack = {})
    }
}
