package com.example.proyectwin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*

data class ProjectItem(
    val name: String,
    val status: String,
    val statusColor: Color,
    val lastUpdate: String
)

@Composable
fun ProjectsScreen(
    onNewProject: () -> Unit,
    onProjectDetail: (String) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("Todos") }
    var searchQuery by remember { mutableStateOf("") }
    val filters = listOf("Todos", "Aprobados", "Pendientes", "Revisión")
    
    val allProjects = listOf(
        ProjectItem("Sistema de Riego Automatizado", "Pendiente", SenaWarning, "Hace 2 horas"),
        ProjectItem("App de Gestión de Inventario", "Aprobado", SenaSuccess, "Ayer"),
        ProjectItem("Plataforma de E-learning", "Revisión", SenaGreen, "Hace 3 días"),
        ProjectItem("Control de Acceso Biométrico", "Aprobado", SenaSuccess, "Hace 1 semana"),
        ProjectItem("Detección de Plagas con IA", "Pendiente", SenaWarning, "Hace 2 semanas")
    )
    
    val filteredProjects = allProjects.filter { project ->
        val matchesFilter = if (selectedFilter == "Todos") true else project.status == selectedFilter
        val matchesSearch = project.name.contains(searchQuery, ignoreCase = true)
        matchesFilter && matchesSearch
    }

    Scaffold(
        topBar = {
            SenaTopBar(title = "Mis Proyectos")
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewProject,
                containerColor = SenaGreen,
                contentColor = Color.White,
                shape = MaterialTheme.shapes.large
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Proyecto")
            }
        },
        containerColor = SenaBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Barra de Búsqueda Premium
            SenaTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = "",
                placeholder = "Buscar proyectos...",
                leadingIcon = Icons.Default.Search
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Filtro Segmentado Moderno
            Text(
                text = "Estado del Proyecto",
                style = MaterialTheme.typography.labelLarge,
                color = SenaText,
                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
            )
            SenaSegmentedFilter(
                options = filters,
                selectedOption = selectedFilter,
                onOptionSelected = { selectedFilter = it }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Lista de Proyectos
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                if (filteredProjects.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No se encontraron proyectos",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SenaTextLight
                            )
                        }
                    }
                } else {
                    items(filteredProjects) { project ->
                        ProjectListItemCard(project, onClick = { onProjectDetail("1") })
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectListItemCard(project: ProjectItem, onClick: () -> Unit) {
    SenaCard(
        onClick = onClick,
        elevation = 2.dp,
        modifier = Modifier.padding(bottom = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(50.dp),
                shape = RoundedCornerShape(12.dp),
                color = SenaBackground
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Folder, contentDescription = null, tint = SenaTextLight)
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    project.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = SenaText,
                    maxLines = 1
                )
                Text(
                    project.lastUpdate,
                    style = MaterialTheme.typography.bodySmall,
                    color = SenaTextLight
                )
            }
            
            SenaChip(text = project.status, color = project.statusColor)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProjectsScreenPreview() {
    ProyecTwinTheme {
        ProjectsScreen(onNewProject = {}, onProjectDetail = {})
    }
}
