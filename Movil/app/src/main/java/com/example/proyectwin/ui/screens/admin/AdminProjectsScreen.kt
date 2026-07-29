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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectwin.data.mock.MockDataProvider
import com.example.proyectwin.data.model.Project
import com.example.proyectwin.data.model.ProjectStatus
import com.example.proyectwin.navigation.AppNavigation
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*
import com.example.proyectwin.ui.viewmodel.AdminViewModel
import com.example.proyectwin.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProjectsScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    adminViewModel: AdminViewModel = viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("Todos") }
    var refreshTrigger by remember { mutableIntStateOf(0) }
    var isRefreshing by remember { mutableStateOf(false) }
    val statuses = listOf("Todos", "Completado", "En Progreso", "Pendiente", "Cancelado")

    val allProjects = remember(refreshTrigger) {
        MockDataProvider.getAllProjects()
    }

    val filteredProjects = allProjects.filter { project ->
        val statusMatch = when (selectedStatus) {
            "Todos" -> true
            "Completado" -> project.estado == ProjectStatus.COMPLETADO.value
            "En Progreso" -> project.estado == ProjectStatus.EN_PROGRESO.value
            "Pendiente" -> project.estado == ProjectStatus.PENDIENTE.value
            "Cancelado" -> project.estado == ProjectStatus.CANCELADO.value
            else -> false
        }
        val searchMatch = project.title.contains(searchQuery, ignoreCase = true) ||
            (project.instructorName?.contains(searchQuery, ignoreCase = true) ?: false)
        statusMatch && searchMatch
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
        containerColor = senaColors().background
    ) { paddingValues ->
        SenaPullRefresh(
            isRefreshing = isRefreshing,
            onRefresh = { isRefreshing = true; refreshTrigger++ },
            modifier = Modifier.padding(paddingValues)
        ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
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

            item {
                SenaFilterBar(title = "Filtros de proyectos") {
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
                                    "Completado" -> senaColors().success
                                    "En Progreso" -> senaColors().accent
                                    "Pendiente" -> senaColors().warning
                                    "Cancelado" -> senaColors().danger
                                    else -> senaColors().green
                                },
                                isSelected = selectedStatus == status,
                                onClick = { selectedStatus = status }
                            )
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
                    SenaCard(onClick = { onNavigate(AppNavigation.ADMIN_PROJECT_DETAIL.replace("{projectId}", "${project.id}")) }) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SenaStatusBadge(status = project.statusDisplay)
                                project.createdAt?.let {
                                    Text(it, style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
                                }
                            }

                            Text(project.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = senaColors().text)

                            HorizontalDivider(color = senaColors().borderSoft)

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = senaColors().green, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(project.studentName ?: "Sin aprendiz", style = MaterialTheme.typography.labelSmall, color = senaColors().textSecondary)
                                Spacer(Modifier.weight(1f))
                                Text("Instructor: ${project.instructorName ?: "N/A"}", style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(40.dp)) }
        }
        }
    }

    LaunchedEffect(refreshTrigger) {
        kotlinx.coroutines.delay(500)
        isRefreshing = false
    }
}

@Preview(showBackground = true)
@Composable
fun AdminProjectsScreenPreview() {
    ProyecTwinTheme {
        AdminProjectsScreen(onBack = {}, onNavigate = {})
    }
}
