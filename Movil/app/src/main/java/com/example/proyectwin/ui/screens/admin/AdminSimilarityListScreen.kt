package com.example.proyectwin.ui.screens.admin

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectwin.data.mock.MockDataProvider
import com.example.proyectwin.data.model.Similarity
import com.example.proyectwin.data.model.SimilarityStatus
import com.example.proyectwin.navigation.AppNavigation
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*
import com.example.proyectwin.ui.viewmodel.AdminViewModel
import com.example.proyectwin.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSimilarityListScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    bottomBar: @Composable () -> Unit = {},
    authViewModel: AuthViewModel = viewModel(),
    adminViewModel: AdminViewModel = viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf<SimilarityStatus?>(null) }

    val similarityGroups = remember { MockDataProvider.getAllSimilarities() }

    val statusFilters = remember { listOf(null) + SimilarityStatus.entries.toList() }

    val filteredGroups = similarityGroups.filter { group ->
        val matchesStatus = selectedStatus == null || group.simStatus == selectedStatus
        val matchesSearch = group.project1Title?.contains(searchQuery, ignoreCase = true) == true ||
                group.project2Title?.contains(searchQuery, ignoreCase = true) == true
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
        containerColor = senaColors().background,
        bottomBar = bottomBar
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                SenaPageHeader(
                    title = "Similitudes",
                    subtitle = "Listado de proyectos con coincidencias detectadas por el sistema.",
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
                            statusFilters.forEach { status ->
                                SenaChip(
                                    text = status?.let {
                                        when (it) {
                                            SimilarityStatus.PENDIENTE -> "Pendiente"
                                            SimilarityStatus.CONFIRMADO -> "Confirmado"
                                            SimilarityStatus.RECHAZADO -> "Rechazado"
                                        }
                                    } ?: "Todos",
                                    color = when (status) {
                                        SimilarityStatus.PENDIENTE -> senaColors().warning
                                        SimilarityStatus.CONFIRMADO -> senaColors().success
                                        SimilarityStatus.RECHAZADO -> senaColors().danger
                                        else -> senaColors().green
                                    },
                                    isSelected = selectedStatus == status,
                                    onClick = { selectedStatus = status }
                                )
                            }
                        }
                    }
                }
            }

            items(filteredGroups) { group ->
                SimilarityGroupCard(group, onClick = { onNavigate(AppNavigation.ADMIN_SIMILARITY_DETAIL) })
            }

            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}

@Composable
fun SimilarityGroupCard(group: Similarity, onClick: () -> Unit) {
    val percentage = (group.similitud * 100).toInt()
    val level = when {
        percentage >= 60 -> "Crítico"
        percentage >= 30 -> "Moderado"
        else -> "Bajo"
    }
    val levelColor = when(level) {
        "Crítico" -> senaColors().danger
        "Moderado" -> senaColors().warning
        else -> senaColors().success
    }

    SenaCard(onClick = onClick) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SenaStatusBadge(status = group.statusDisplay)
                Surface(
                    color = levelColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        level.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = levelColor
                    )
                }
            }

            Column {
                Text(
                    "${group.project1Title ?: "Proyecto A"} vs ${group.project2Title ?: "Proyecto B"}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = senaColors().text
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Compare, contentDescription = null, tint = senaColors().textLight, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("1 coincidencia encontrada", style = MaterialTheme.typography.labelSmall, color = senaColors().textSecondary)
                }
            }

            HorizontalDivider(color = senaColors().borderSoft)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Máxima Similitud", style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
                    Text(group.similitudPercent, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = levelColor)
                }

                TextButton(onClick = onClick) {
                    Text("Ver Detalles", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
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
