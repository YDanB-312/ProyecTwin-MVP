package com.example.proyectwin.ui.screens.admin

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectwin.navigation.AppNavigation
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*

data class AdminProjectItem(
    val id: String,
    val title: String,
    val program: String,
    val instructor: String,
    val status: String,
    val date: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProjectsScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("Todos") }
    val statuses = listOf("Todos", "Aprobado", "En Revisión", "Borrador", "Rechazado")

    val projects = remember {
        listOf(
            AdminProjectItem("1", "Sistema IoT para Agricultura", "ADSO", "Carlos Ruiz", "Aprobado", "15/03/2026"),
            AdminProjectItem("2", "Plataforma Educativa SENA", "ADSO", "Maria Torres", "En Revisión", "02/02/2026"),
            AdminProjectItem("3", "App de Reciclaje Inteligente", "Multimedia", "Andres Lopez", "Borrador", "20/04/2026"),
            AdminProjectItem("4", "Red de Sensores Ambientales", "Redes", "Pedro Jimenez", "Aprobado", "10/01/2026"),
        )
    }

    val filteredProjects = projects.filter { project ->
        val matchesStatus = if (selectedStatus == "Todos") true else project.status == selectedStatus
        val matchesSearch = project.title.contains(searchQuery, ignoreCase = true) || project.instructor.contains(searchQuery, ignoreCase = true)
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
                    title = "Proyectos Globales",
                    subtitle = "Supervisión de todas las propuestas técnicas registradas en la plataforma.",
                    icon = Icons.Default.FolderSpecial
                )
            }

            // Filter Bar
            item {
                SenaCard(elevation = 1.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SenaTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = "Buscar proyectos",
                            placeholder = "Nombre o instructor...",
                            leadingIcon = Icons.Default.Search
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            statuses.forEach { status ->
                                SenaChip(
                                    text = status,
                                    color = when(status) {
                                        "Aprobado" -> SenaSuccess
                                        "En Revisión" -> SenaAccent
                                        "Borrador" -> SenaWarning
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

            if (filteredProjects.isEmpty()) {
                item {
                    SenaEmptyState(
                        message = "No se encontraron proyectos bajo estos criterios.",
                        icon = Icons.Default.SearchOff
                    )
                }
            } else {
                items(filteredProjects) { project ->
                    SenaCard(onClick = { onNavigate(AppNavigation.ADMIN_PROJECT_DETAIL.replace("{projectId}", project.id)) }) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SenaStatusBadge(status = project.status)
                                Text(project.date, style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
                            }

                            Text(project.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SenaText)
                            
                            HorizontalDivider(color = SenaBorderSoft)

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.School, contentDescription = null, tint = SenaGreen, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(project.program, style = MaterialTheme.typography.labelSmall, color = SenaTextSecondary)
                                Spacer(Modifier.weight(1f))
                                Text("Instructor: ${project.instructor}", style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
                            }
                        }
                    }
                }
            }
            
            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdminProjectsScreenPreview() {
    ProyecTwinTheme {
        AdminProjectsScreen(onBack = {}, onNavigate = {})
    }
}
