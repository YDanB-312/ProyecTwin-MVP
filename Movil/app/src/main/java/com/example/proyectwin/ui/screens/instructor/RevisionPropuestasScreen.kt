package com.example.proyectwin.ui.screens.instructor

import androidx.compose.foundation.background
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
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectwin.data.mock.MockDataProvider
import com.example.proyectwin.data.model.Project
import com.example.proyectwin.data.model.ProjectStatus
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*
import com.example.proyectwin.ui.viewmodel.AuthUiState
import com.example.proyectwin.ui.viewmodel.AuthViewModel
import com.example.proyectwin.ui.viewmodel.DashboardUiState
import com.example.proyectwin.ui.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevisionPropuestasScreen(
    onBack: () -> Unit,
    onProjectDetail: (Int) -> Unit,
    bottomBar: @Composable () -> Unit = {},
    authViewModel: AuthViewModel = viewModel(),
    dashboardViewModel: DashboardViewModel = viewModel()
) {
    val authState by authViewModel.uiState.collectAsState()
    val dashState by dashboardViewModel.uiState.collectAsState()
    val user = (authState as? AuthUiState.LoggedIn)?.user

    LaunchedEffect(user) {
        user?.let { dashboardViewModel.loadInstructorDashboard(it.id) }
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("Todos") }
    var obsModalId by remember { mutableStateOf<Int?>(null) }
    var obsTexto by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val statuses = listOf("Todos", "Pendiente", "En Progreso", "Completado", "Cancelado")

    val filteredPropuestas = (dashState as? DashboardUiState.Success)?.projects?.filter { proposal ->
        val statusMatch = when (selectedStatus) {
            "Todos" -> true
            "Pendiente" -> proposal.estado == ProjectStatus.PENDIENTE.value
            "En Progreso" -> proposal.estado == ProjectStatus.EN_PROGRESO.value
            "Completado" -> proposal.estado == ProjectStatus.COMPLETADO.value
            "Cancelado" -> proposal.estado == ProjectStatus.CANCELADO.value
            else -> false
        }
        val searchMatch = proposal.title.contains(searchQuery, ignoreCase = true) ||
            (proposal.studentName?.contains(searchQuery, ignoreCase = true) ?: false)
        statusMatch && searchMatch
    } ?: emptyList()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    title = "Revisi�n de Propuestas",
                    subtitle = "Eval�a las propuestas de proyectos enviadas por los aprendices.",
                    icon = Icons.AutoMirrored.Filled.List
                )
            }

            item {
                SenaFilterBar(title = "Filtros de revisi�n") {
                    SenaTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = "",
                        placeholder = "Buscar por proyecto o aprendiz...",
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
                                    "Pendiente" -> senaColors().warning
                                    "En Progreso" -> senaColors().accent
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

            if (filteredPropuestas.isEmpty()) {
                item {
                    SenaEmptyState(
                        message = "No hay propuestas que coincidan con los filtros seleccionados.",
                        icon = Icons.Default.SearchOff
                    )
                }
            } else {
                items(filteredPropuestas) { proposal ->
                    ProposalReviewCard(
                        proposal,
                        onDetailClick = { onProjectDetail(proposal.id) },
                        onObsClick = { obsModalId = proposal.id; obsTexto = "" }
                    )
                }
            }

            item { Spacer(Modifier.height(40.dp)) }
        }
    }

    if (obsModalId != null) {
        AlertDialog(
            onDismissRequest = { obsModalId = null },
            icon = { Icon(Icons.AutoMirrored.Filled.Comment, contentDescription = null, tint = senaColors().green) },
            title = { Text("Agregar Observaciones", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = obsTexto,
                    onValueChange = { obsTexto = it },
                    placeholder = { Text("Escribe tus observaciones...") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = senaColors().green,
                        unfocusedBorderColor = senaColors().border
                    )
                )
            },
            confirmButton = {
                SenaButton(
                    text = "Guardar",
                    onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Observaciones guardadas correctamente")
                        }
                        obsModalId = null
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { obsModalId = null }) {
                    Text("Cancelar", color = senaColors().textSecondary)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun ProposalReviewCard(proposal: Project, onDetailClick: () -> Unit, onObsClick: () -> Unit = {}) {
    val similarities = remember(proposal.id) {
        MockDataProvider.getSimilaritiesByProject(proposal.id)
    }
    val maxSimilarity = similarities.maxOfOrNull { it.similitud } ?: 0.0

    SenaCard {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SenaStatusBadge(status = proposal.statusDisplay)
                proposal.createdAt?.let {
                    Text(it, style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    proposal.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = senaColors().text
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = senaColors().green, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "${proposal.studentName ?: "Sin aprendiz"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = senaColors().textSecondary
                    )
                }
            }

            if (maxSimilarity > 0) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = senaColors().danger.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, senaColors().danger.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = senaColors().danger, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "%d%% de similitud detectada".format((maxSimilarity * 100).toInt()),
                            style = MaterialTheme.typography.labelSmall,
                            color = senaColors().danger,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Text(
                text = proposal.description,
                style = MaterialTheme.typography.bodySmall,
                color = senaColors().textSecondary,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SenaButton(
                    text = "Observaciones",
                    onClick = onObsClick,
                    icon = Icons.AutoMirrored.Filled.Comment,
                    isPrimary = false,
                    modifier = Modifier.weight(1f)
                )
                SenaButton(
                    text = "Revisar",
                    onClick = onDetailClick,
                    icon = Icons.Default.Visibility,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RevisionPropuestasPreview() {
    ProyecTwinTheme {
        RevisionPropuestasScreen(onBack = {}, onProjectDetail = {})
    }
}
