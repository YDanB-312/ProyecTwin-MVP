package com.example.proyectwin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*

@Composable
fun ProjectsScreen(
    onNavigate: (String) -> Unit,
    onNewProject: () -> Unit,
    onProjectDetail: (String) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("Todos") }
    var searchQuery by remember { mutableStateOf("") }
    val filters = listOf("Todos", "Aprobado", "En revisión", "Borrador")
    
    val allProjects = listOf(
        ProjectItem("Sistema de Gestion Academica", "En revisión", SenaAccent, "15 mar 2023", 3, "Carlos Ruiz", "CR"),
        ProjectItem("Aplicacion Web de Inventarios", "Aprobado", SenaSuccess, "22 abr 2023", 2, "Ana Gomez", "AG"),
        ProjectItem("Plataforma E-Learning", "Borrador", SenaTextLight, "10 may 2023", 4, "Miguel Lopez", "ML")
    )
    
    val filteredProjects = allProjects.filter { project ->
        val matchesFilter = if (selectedFilter == "Todos") true else project.status == selectedFilter
        val matchesSearch = project.name.contains(searchQuery, ignoreCase = true)
        matchesFilter && matchesSearch
    }

    Scaffold(
        topBar = {
            SenaTopBar(
                title = "Mis Proyectos",
                onNavigateToProfile = { onNavigate("main/profile") },
                onNavigateToAlerts = { onNavigate("main/alerts") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewProject,
                containerColor = SenaGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
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
        ) {
            // Mobile Search & Filters
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SenaTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = "",
                    placeholder = "Buscar proyectos...",
                    leadingIcon = Icons.Default.Search
                )
                
                SenaSegmentedFilter(
                    options = filters,
                    selectedOption = selectedFilter,
                    onOptionSelected = { selectedFilter = it }
                )
            }
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (filteredProjects.isEmpty()) {
                    item {
                        EmptyStateCard(message = "No encontramos proyectos con estos filtros", icon = Icons.Default.SearchOff)
                    }
                } else {
                    items(filteredProjects) { project ->
                        ProjectListItemCard(project, onClick = { onProjectDetail("1") })
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
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
