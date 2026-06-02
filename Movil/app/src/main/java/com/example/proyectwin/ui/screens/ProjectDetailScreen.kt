package com.example.proyectwin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.proyectwin.ui.components.SenaCard
import com.example.proyectwin.ui.components.SenaChip
import com.example.proyectwin.ui.components.SenaTopBar
import com.example.proyectwin.ui.theme.*

@Composable
fun ProjectDetailScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            SenaTopBar(title = "Detalle del Proyecto", showProfile = false, showNotifications = false)
        },
        containerColor = SenaBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header with Back
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
                Text(
                    "Información General",
                    style = MaterialTheme.typography.titleLarge,
                    color = SenaText
                )
            }

            // Main Info Card
            SenaCard {
                DetailItem("Nombre del Proyecto", "Sistema IoT para Agricultura de Precisión")
                DetailItem("Programa de Formación", "ADSO - Análisis y Desarrollo de Sistemas")
                DetailItem("Fecha de Creación", "15/03/2026")
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                    Text("Estado: ", style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
                    SenaChip(text = "Aprobado", color = SenaSuccess)
                    Spacer(modifier = Modifier.width(8.dp))
                    SenaChip(text = "Analizado por IA", color = SenaGreen)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Descripción", style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
                Text(
                    "Desarrollo de un sistema basado en sensores IoT para monitorear variables ambientales en cultivos, permitiendo la toma de decisiones en tiempo real para optimizar el riego y la fertilización.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SenaText
                )
            }

            // Team Card
            Text(
                "Integrantes del Equipo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SenaText
            )
            SenaCard {
                TeamMemberDetailRow("MG", "Maria Gonzalez", "Creador / Líder")
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SenaBorder)
                TeamMemberDetailRow("JP", "Juan Perez", "Integrante")
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SenaBorder)
                TeamMemberDetailRow("LG", "Laura Gomez", "Integrante")
            }

            // Observations Section
            Text(
                "Observaciones del Instructor",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SenaText
            )
            ObservationCard(
                author = "Carlos Ruiz | Instructor",
                date = "10 may 2026",
                content = "El proyecto necesita mejorar la sección de análisis de requisitos. Se recomienda ampliar la documentación técnica antes de continuar con el desarrollo."
            )
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
        Text(value, style = MaterialTheme.typography.bodyLarge, color = SenaText, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun TeamMemberDetailRow(initials: String, name: String, role: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = if (role.contains("Líder")) SenaGreen else SenaTextLight.copy(alpha = 0.2f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(initials, color = if (role.contains("Líder")) Color.White else SenaText, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SenaText)
            Text(role, style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
        }
    }
}

@Composable
fun ObservationCard(author: String, date: String, content: String) {
    SenaCard(containerColor = SenaBorder.copy(alpha = 0.2f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = SenaTextLight)
            Spacer(modifier = Modifier.width(8.dp))
            Text(author, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SenaText)
            Spacer(modifier = Modifier.weight(1f))
            Text(date, style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(content, style = MaterialTheme.typography.bodyMedium, color = SenaText)
    }
}

@Preview(showBackground = true)
@Composable
fun ProjectDetailScreenPreview() {
    ProyecTwinTheme {
        ProjectDetailScreen(onBack = {})
    }
}
