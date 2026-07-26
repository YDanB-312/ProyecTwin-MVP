package com.example.proyectwin.ui.screens.aprendiz

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.navigation.AppNavigation
import com.example.proyectwin.ui.theme.*

data class ProjectDetailData(
    val title: String = "Sistema de Gestión Académica",
    val status: String = "En revisión",
    val program: String = "ADSO - Análisis y Desarrollo de Software",
    val date: String = "15/03/2026",
    val description: String = "Desarrollo de un sistema integral para la gestión académica que permita administrar notas, horarios, asistencia y reportes académicos en tiempo real.",
    val instructor: String = "Carlos Ruiz",
    val team: List<TeamMember> = listOf(
        TeamMember("Maria Gonzalez", "Creador / Lider", "MG"),
        TeamMember("Juan Pérez", "Integrante", "JP"),
        TeamMember("Laura Gómez", "Integrante", "LG")
    ),
    val observations: List<ProjectObservation> = listOf(
        ProjectObservation("Carlos Ruiz | Instructor", "El proyecto necesita mejorar la sección de análisis de requisitos. Se recomienda ampliar la documentación técnica.", "10 may 2026")
    )
)

data class TeamMember(val name: String, val role: String, val initials: String)
data class ProjectObservation(val author: String, val text: String, val date: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(projectId: String = "", onBack: () -> Unit, onNavigate: (String) -> Unit) {
    val data = remember { ProjectDetailData() }
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
                    text = "Ver Similitudes", 
                    onClick = { onNavigate(AppNavigation.APRENDIZ_SIMILARITY) },
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Warning
                )
                SenaButton(
                    text = "Editar", 
                    onClick = { onNavigate(AppNavigation.APRENDIZ_NEW_PROJECT) },
                    isPrimary = false,
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Edit
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SenaPageHeader(
                title = data.title,
                subtitle = "Detalle del proyecto de formación",
                icon = Icons.Default.FolderOpen
            )

            SenaSectionHeader(title = "Información General")
            SenaCard(elevation = 1.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    DetailRowItem(Icons.Default.Title, "Nombre del Proyecto", data.title)
                    HorizontalDivider(color = SenaBorderSoft)
                    DetailRowItem(Icons.Default.School, "Programa de Formación", data.program)
                    HorizontalDivider(color = SenaBorderSoft)
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            DetailRowItem(Icons.Default.CalendarToday, "Fecha", data.date)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Estado", style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
                            Spacer(Modifier.height(4.dp))
                            SenaStatusBadge(status = data.status)
                        }
                    }
                    HorizontalDivider(color = SenaBorderSoft)
                    Column {
                        Text("Descripción", style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            data.description, 
                            style = MaterialTheme.typography.bodyMedium, 
                            color = SenaTextSecondary,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            SenaSectionHeader(title = "Integrantes del Equipo")
            SenaCard(elevation = 1.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    data.team.forEachIndexed { index, member ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = CircleShape,
                                color = if (index == 0) SenaGreen else SenaBorderSoft
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        member.initials, 
                                        style = MaterialTheme.typography.labelMedium, 
                                        fontWeight = FontWeight.Bold,
                                        color = if (index == 0) Color.White else SenaText
                                    )
                                }
                            }
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(member.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SenaText)
                                Text(member.role, style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
                            }
                        }
                        if (index < data.team.size - 1) {
                            HorizontalDivider(color = SenaBorderSoft, modifier = Modifier.padding(start = 56.dp, top = 12.dp))
                        }
                    }
                }
            }

            SenaSectionHeader(title = "Observaciones del Instructor")
            if (data.observations.isEmpty()) {
                SenaEmptyState(message = "No hay observaciones para este proyecto.", icon = Icons.AutoMirrored.Filled.Chat)
            } else {
                data.observations.forEach { obs ->
                    SenaCard(containerColor = Color.White, elevation = 2.dp) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PersonSearch, contentDescription = null, tint = SenaGreen, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(obs.author, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SenaText)
                                }
                                Text(obs.date, style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
                            }
                            Text(
                                obs.text, 
                                style = MaterialTheme.typography.bodySmall, 
                                color = SenaTextSecondary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun DetailRowItem(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = RoundedCornerShape(8.dp),
            color = SenaGreen.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = SenaGreen, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SenaText)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProjectDetailScreenPreview() {
    ProyecTwinTheme {
        ProjectDetailScreen(onBack = {}, onNavigate = {})
    }
}
