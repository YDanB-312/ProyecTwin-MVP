package com.example.proyectwin.ui.screens.aprendiz

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

data class ProjectSummary(
    val id: String,
    val name: String,
    val status: String,
    val date: String,
    val members: Int,
    val instructor: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    onNavigate: (String) -> Unit,
    onNewProject: () -> Unit,
    onProjectDetail: (String) -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Todos") }
    val filters = listOf("Todos", "Aprobado", "En revisión", "Borrador", "Rechazado")

    val projects = remember {
        listOf(
            ProjectSummary("1", "Sistema de Gestión Académica", "En revisión", "15 mar 2023", 3, "Carlos Ruiz"),
            ProjectSummary("2", "Aplicación Web de Inventarios", "Aprobado", "22 abr 2023", 2, "Ana Gomez"),
            ProjectSummary("3", "App Móvil de Seguridad", "Borrador", "01 jun 2023", 4, "Pedro Lopez"),
            ProjectSummary("4", "Plataforma IoT para Agricultura", "Rechazado", "10 feb 2023", 5, "Laura Diaz"),
            ProjectSummary("5", "Chatbot de Atención al Cliente", "En revisión", "05 ene 2023", 2, "Carlos Ruiz"),
        )
    }

    val filteredProjects = projects.filter { project ->
        val matchesFilter = if (selectedFilter == "Todos") true else project.status == selectedFilter
        val matchesSearch = project.name.contains(searchQuery, ignoreCase = true)
        matchesFilter && matchesSearch
    }

    Scaffold(
        topBar = {
            SenaTopBar(
                title = "ProyecTwin",
                onBack = { onNavigate(AppNavigation.APRENDIZ_DASHBOARD) },
                onNavigateToProfile = { onNavigate(AppNavigation.APRENDIZ_PROFILE) },
                onNavigateToAlerts = { onNavigate(AppNavigation.APRENDIZ_ALERTS) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewProject,
                containerColor = SenaGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Proyecto")
            }
        },
        containerColor = SenaBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                SenaPageHeader(
                    title = "Mis Proyectos",
                    subtitle = "Gestiona y haz seguimiento a tus propuestas de formación.",
                    icon = Icons.Default.FolderOpen
                )
            }

            // Filter Bar
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
                            placeholder = "Buscar por nombre...",
                            leadingIcon = Icons.Default.Search
                        )
                        
                        Text(
                            "Estado del proyecto",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SenaTextLight,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            filters.forEach { filter ->
                                SenaChip(
                                    text = filter,
                                    color = if (filter == "Todos") SenaGreen else when(filter) {
                                        "Aprobado" -> SenaSuccess
                                        "En revisión" -> SenaAccent
                                        "Borrador" -> SenaWarning
                                        else -> SenaDanger
                                    },
                                    isSelected = selectedFilter == filter,
                                    onClick = { selectedFilter = filter }
                                )
                            }
                        }
                    }
                }
            }

            if (filteredProjects.isEmpty()) {
                item {
                    SenaEmptyState(
                        message = "No se encontraron proyectos que coincidan con tu búsqueda.", 
                        icon = Icons.Default.SearchOff
                    )
                }
            } else {
                items(filteredProjects) { project ->
                    SenaCard(onClick = { onProjectDetail(project.id) }) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SenaStatusBadge(status = project.status)
                                Text(project.date, style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
                            }
                            Text(
                                project.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SenaText
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Group, contentDescription = null, tint = SenaTextLight, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("${project.members} integrantes", style = MaterialTheme.typography.bodySmall, color = SenaTextSecondary)
                                Spacer(Modifier.weight(1f))
                                Surface(
                                    modifier = Modifier.size(32.dp),
                                    shape = CircleShape,
                                    color = SenaGreen.copy(alpha = 0.1f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                        project.instructor.split(" ").joinToString("") { it.take(1) },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SenaGreen
                                    )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProjectsScreenPreview() {
    ProyecTwinTheme {
        ProjectsScreen(onNavigate = {}, onNewProject = {}, onProjectDetail = {})
    }
}
