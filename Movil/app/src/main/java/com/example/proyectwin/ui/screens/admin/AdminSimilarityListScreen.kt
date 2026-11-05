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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.proyectwin.data.model.Similarity
import com.example.proyectwin.navigation.AppNavigation
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*
import com.example.proyectwin.ui.viewmodel.AdminViewModel
import com.example.proyectwin.ui.viewmodel.AdminUiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSimilarityListScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    /** Filtro por propuesta (viene del detalle o de una notificación). */
    proyectoId: Int? = null,
    bottomBar: @Composable () -> Unit = {},
    adminViewModel: AdminViewModel = hiltViewModel()
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedStatus by rememberSaveable { mutableStateOf("Todos") }
    var similitudParaEliminar by remember { mutableStateOf<Similarity?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var estadoPropuesta by rememberSaveable { mutableStateOf("Todos") }
    var fichaFiltro by rememberSaveable { mutableStateOf("") }
    var programaFiltro by rememberSaveable { mutableStateOf("") }
    var minSimilitud by rememberSaveable { mutableIntStateOf(0) }
    var pagina by rememberSaveable { mutableIntStateOf(1) }
    val porPagina = 8
    val statusFilterItems = listOf(
        "Todos" to "Todos",
        "Vigentes" to "Vigentes",
        "Históricas" to "Históricas",
    )

    val adminState by adminViewModel.uiState.collectAsState()
    val completas by adminViewModel.similitudesCompletas.collectAsState()

    // El listado global del servidor solo trae pares con ambos proyectos
    // aprobados; al abrir la pantalla se completan con los pares por propuesta
    // (vigentes e históricas) usando los endpoints existentes. Se espera al
    // estado Success para no lanzarlo antes de tener los datos ni duplicarlo.
    LaunchedEffect(adminState) {
        if (adminState is AdminUiState.Success) adminViewModel.cargarSimilitudesCompletas()
    }

    val similarityGroups = when (val state = adminState) {
        is AdminUiState.Success -> completas ?: state.similarities
        else -> emptyList()
    }

    val filteredGroups = similarityGroups.filter { group ->
        val matchesProyecto = proyectoId == null ||
            group.projectId1 == proyectoId || group.projectId2 == proyectoId
        val matchesEstado = when (selectedStatus) {
            "Vigentes" -> group.vigente
            "Históricas" -> !group.vigente
            else -> true
        }
        val consulta = searchQuery.trim()
        val matchesSearch = consulta.isEmpty() ||
            group.project1Title.orEmpty().contains(consulta, ignoreCase = true) ||
            group.project2Title.orEmpty().contains(consulta, ignoreCase = true) ||
            group.project1Student.orEmpty().contains(consulta, ignoreCase = true) ||
            group.project2Student.orEmpty().contains(consulta, ignoreCase = true)
        val matchesPropuesta = estadoPropuesta == "Todos" ||
            group.project1?.estado == estadoPropuesta.lowercase() ||
            group.project2?.estado == estadoPropuesta.lowercase()
        val matchesFicha = fichaFiltro.isBlank() ||
            group.project1?.fichaId?.toString() == fichaFiltro.trim() ||
            group.project2?.fichaId?.toString() == fichaFiltro.trim()
        val matchesPrograma = programaFiltro.isBlank() ||
            group.project1?.programa.orEmpty().contains(programaFiltro, true) ||
            group.project2?.programa.orEmpty().contains(programaFiltro, true)
        val matchesUmbral = (group.similitud * 100).toInt() >= minSimilitud
        matchesProyecto && matchesEstado && matchesSearch && matchesPropuesta &&
            matchesFicha && matchesPrograma && matchesUmbral
    }
    val totalPaginas = ((filteredGroups.size + porPagina - 1) / porPagina).coerceAtLeast(1)
    val paginaActual = pagina.coerceIn(1, totalPaginas)
    val visibles = filteredGroups.drop((paginaActual - 1) * porPagina).take(porPagina)

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
        bottomBar = bottomBar,
        snackbarHost = { SnackbarHost(snackbarHostState) },
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

            when (val state = adminState) {
                is AdminUiState.Loading -> item {
                    Box(Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                        SenaLoadingState()
                    }
                }
                is AdminUiState.Error -> item {
                    SenaErrorState(message = state.message, onRetry = { adminViewModel.loadAll() })
                }
                is AdminUiState.Success -> {
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
                                        "Vigentes" -> senaColors().success
                                        "Históricas" -> senaColors().warning
                                        else -> senaColors().green
                                    },
                                    isSelected = selectedStatus == statusKey,
                                    onClick = { selectedStatus = statusKey; pagina = 1 }
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            SenaTextField(
                                value = programaFiltro,
                                onValueChange = { programaFiltro = it; pagina = 1 },
                                label = "",
                                placeholder = "Programa",
                                leadingIcon = Icons.Default.School,
                                modifier = Modifier.weight(1f),
                            )
                            SenaTextField(
                                value = fichaFiltro,
                                onValueChange = { fichaFiltro = it; pagina = 1 },
                                label = "",
                                placeholder = "ID de ficha",
                                leadingIcon = Icons.Default.Groups,
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            listOf("Todos", "Pendiente", "Aprobado", "Rechazado").forEach { estado ->
                                SenaChip(
                                    text = estado,
                                    color = senaColors().green,
                                    isSelected = estadoPropuesta == estado,
                                    onClick = { estadoPropuesta = estado; pagina = 1 },
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            listOf(0, 30, 50, 75).forEach { umbral ->
                                SenaChip(
                                    text = if (umbral == 0) "Similitud: todas" else "≥ $umbral%",
                                    color = senaColors().green,
                                    isSelected = minSimilitud == umbral,
                                    onClick = { minSimilitud = umbral; pagina = 1 },
                                )
                            }
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (completas == null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = senaColors().green,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Cargando también los pares de cada propuesta…",
                                style = MaterialTheme.typography.labelSmall,
                                color = senaColors().textSecondary,
                            )
                        }
                    }
                    Text(
                        if (proyectoId != null) {
                            "Mostrando solo los pares de la propuesta #$proyectoId."
                        } else {
                            "${filteredGroups.size} de ${similarityGroups.size} pares · incluye los pares por propuesta " +
                                "(vigentes con contraparte aprobada e históricas), no solo los de ambos lados aprobados."
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = senaColors().textLight,
                    )
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
                items(visibles, key = { it.id }) { similarity ->
                    SenaCard(
                        modifier = Modifier.fillMaxWidth(),
                        selected = LocalSelectedListItemId.current == similarity.id,
                        onClick = {
                            onNavigate(
                                AppNavigation.ADMIN_SIMILARITY_DETAIL.replace("{similarityId}", similarity.id.toString())
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
                                Text(
                                    similarity.similitudPercent,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = senaColors().green,
                                )
                            }
                            Text(
                                "A: ${similarity.project1Title ?: "N/A"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = senaColors().textSecondary
                            )
                            similarity.project1Student?.let {
                                Text(
                                    "Aprendiz A: $it",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = senaColors().textLight,
                                )
                            }
                            Text(
                                "B: ${similarity.project2Title ?: "N/A"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = senaColors().textSecondary
                            )
                            similarity.project2Student?.let {
                                Text(
                                    "Aprendiz B: $it",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = senaColors().textLight,
                                )
                            }
                            Text(
                                similarity.estadoDisplay,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (similarity.vigente) senaColors().success else senaColors().warning,
                            )
                            TextButton(
                                onClick = { similitudParaEliminar = similarity },
                                modifier = Modifier.align(Alignment.End),
                            ) {
                                Text("Eliminar par", color = senaColors().danger)
                            }
                        }
                    }
                }
            }
                }
            }

            if (totalPaginas > 1) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(enabled = paginaActual > 1, onClick = { pagina = paginaActual - 1 }) {
                            Text("Anterior")
                        }
                        Text(
                            "Página $paginaActual de $totalPaginas",
                            style = MaterialTheme.typography.labelMedium,
                            color = senaColors().textSecondary,
                        )
                        TextButton(enabled = paginaActual < totalPaginas, onClick = { pagina = paginaActual + 1 }) {
                            Text("Siguiente")
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(40.dp)) }
        }
    }

    similitudParaEliminar?.let { similarity ->
        AlertDialog(
            onDismissRequest = { similitudParaEliminar = null },
            title = { Text("Eliminar similitud") },
            text = {
                Text(
                    "Se eliminará el par #${similarity.id}. El motor podría volver a detectarlo al recalcular.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        similitudParaEliminar = null
                        adminViewModel.eliminarSimilitud(similarity.id) { resultado ->
                            resultado.onFailure { error ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        error.message ?: "No se pudo eliminar la similitud.",
                                    )
                                }
                            }
                        }
                    },
                ) { Text("Eliminar", color = senaColors().danger) }
            },
            dismissButton = {
                TextButton(onClick = { similitudParaEliminar = null }) { Text("Cancelar") }
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AdminSimilarityListPreview() {
    ProyecTwinTheme {
        AdminSimilarityListScreen(onBack = {}, onNavigate = {})
    }
}
