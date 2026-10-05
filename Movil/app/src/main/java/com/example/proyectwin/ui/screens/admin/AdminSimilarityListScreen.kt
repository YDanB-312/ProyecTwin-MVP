package com.example.proyectwin.ui.screens.admin

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.proyectwin.data.model.Similarity
import com.example.proyectwin.navigation.AppNavigation
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*
import com.example.proyectwin.ui.viewmodel.AdminViewModel
import com.example.proyectwin.ui.viewmodel.AdminUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSimilarityListScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    adminViewModel: AdminViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("Todos") }
    val statusFilterItems = listOf(
        "Todos" to "Todos",
        "Pendiente" to "Pendiente",
        "Revisado" to "Revisado",
        "Confirmado" to "Confirmado",
        "Rechazado" to "Rechazado"
    )

    val adminState by adminViewModel.uiState.collectAsState()
    val similarityGroups = when (val state = adminState) {
        is AdminUiState.Success -> state.similarities
        else -> emptyList()
    }

    val filteredGroups = similarityGroups.filter { group ->
        val matchesSearch = when (selectedStatus) {
            "Todos" -> true
            else -> group.similitud.toString().contains(searchQuery, ignoreCase = true) || (
                (group.project1Title ?: "").contains(searchQuery, ignoreCase = true) ||
                (group.project2Title ?: "").contains(searchQuery, ignoreCase = true)
            )
        }
        matchesSearch
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
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                SenaPageHeader(
                    title = "Similitudes",
                    subtitle = "Listado de propuestas con similitudes detectadas por el sistema.",
                    icon = Icons.Default.Search
                )
            }

            item {
                SenaCard(elevation = 1.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            "Filtrar Similitudes",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = senaColors().textLight,
                            letterSpacing = 0.5.sp
                        )
                        SenaTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = "",
                            placeholder = "Nombre del proyecto...",
                            leadingIcon = Icons.Default.Search
                        )

                        Text(
                            "Estado",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = senaColors().textLight,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            statusFilterItems.forEach { (statusKey, statusLabel) ->
                                SenaChip(
                                    text = statusLabel,
                                    color = when (statusKey) {
                                        "Pendiente" -> senaColors().warning
                                        "Revisado" -> senaColors().info
                                        "Confirmado" -> senaColors().success
                                        "Rechazado" -> senaColors().danger
                                        else -> senaColors().green
                                    },
                                    isSelected = selectedStatus == statusKey,
                                    onClick = { selectedStatus = statusKey }
                                )
                            }
                        }
                    }
                }
            }

            if (filteredGroups.isEmpty()) {
                item {
                    SenaEmptyState(
                        title = "Sin similitudes",
                        message = "No se encontraron similitudes registradas.",
                        icon = Icons.Default.Search
                    )
                }
            } else {
                items(filteredGroups) { similarity ->
                    SenaCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onNavigate(
                                AppNavigation.ADMIN_SIMILARITY_DETAIL.replace("{projectId}", similarity.id.toString())
                            )
                        }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Similitud #${similarity.id}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                SenaStatusBadge(
                                    status = String.format(java.util.Locale.US, "%.0f%%", similarity.similitud * 100)
                                )
                            }
                            Text(
                                "Proyecto 1: ${similarity.project1Title ?: "N/A"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = senaColors().textSecondary
                            )
                            Text(
                                "Proyecto 2: ${similarity.project2Title ?: "N/A"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = senaColors().textSecondary
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}

@Composable
fun AdminSimilarityDetailScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit = {},
    similarityId: String = ""
) {
    Scaffold(
        topBar = {
            SenaTopBar(
                title = "Detalle de Similitud",
                onBack = onBack,
                showProfile = true,
                showNotifications = true
            )
        },
        containerColor = senaColors().background
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Detalle de similitud #$similarityId", style = MaterialTheme.typography.titleLarge)
            Text("Información detallada sobre la similitud detectada", style = MaterialTheme.typography.bodyMedium, color = senaColors().textSecondary)
            Spacer(Modifier.height(24.dp))
            Button(onClick = onBack) {
                Text("Regresar a la lista")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdminSimilarityListPreview() {
    ProyecTwinTheme {
        AdminSimilarityListScreen(onBack = {}, onNavigate = {})
    }
}
