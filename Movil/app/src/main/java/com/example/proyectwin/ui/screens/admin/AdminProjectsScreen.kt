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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.proyectwin.data.model.ProjectStatus
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*
import com.example.proyectwin.ui.viewmodel.AdminUiState
import com.example.proyectwin.ui.viewmodel.AdminViewModel

/** Proyectos (admin): listado real con filtros y acceso al detalle/intervención. */
@Composable
fun AdminProjectsScreen(
    onBack: () -> Unit,
    onProjectDetail: (Int) -> Unit,
    adminViewModel: AdminViewModel = hiltViewModel(),
) {
    var busqueda by rememberSaveable { mutableStateOf("") }
    var estadoFiltro by rememberSaveable { mutableStateOf("Todos") }
    var programaFiltro by rememberSaveable { mutableStateOf("") }
    var fichaFiltro by rememberSaveable { mutableStateOf("") }
    var instructorFiltro by rememberSaveable { mutableStateOf("") }
    var areaFiltro by rememberSaveable { mutableStateOf("") }
    var minSimilitud by rememberSaveable { mutableIntStateOf(0) }
    var pagina by rememberSaveable { mutableIntStateOf(1) }
    val porPagina = 8
    val adminState by adminViewModel.uiState.collectAsState()
    val filtros = listOf("Todos", "Borrador", "En revisión", "Aprobado", "Rechazado")

    val todos = (adminState as? AdminUiState.Success)?.projects.orEmpty()
    val similitudes = (adminState as? AdminUiState.Success)?.similarities.orEmpty()
    fun maximaSimilitud(id: Int): Int = similitudes
        .filter { it.projectId1 == id || it.projectId2 == id }
        .maxOfOrNull { (it.similitud * 100).toInt() } ?: 0

    val proyectosFiltrados = todos.filter { proyecto ->
        val porEstado = when (estadoFiltro) {
            "Borrador" -> proyecto.estado == ProjectStatus.BORRADOR.value
            "En revisión" -> proyecto.estado == ProjectStatus.PENDIENTE.value
            "Aprobado" -> proyecto.estado == ProjectStatus.APROBADO.value
            "Rechazado" -> proyecto.estado == ProjectStatus.RECHAZADO.value
            else -> true
        }
        val porTexto = busqueda.isBlank() ||
            proyecto.title.contains(busqueda, true) ||
            proyecto.studentName.orEmpty().contains(busqueda, true)
        val porPrograma = programaFiltro.isBlank() ||
            proyecto.programa.orEmpty().contains(programaFiltro, true)
        val porFicha = fichaFiltro.isBlank() ||
            proyecto.fichaId?.toString() == fichaFiltro.trim()
        val porInstructor = instructorFiltro.isBlank() ||
            proyecto.instructorName.orEmpty().contains(instructorFiltro, true)
        val porArea = areaFiltro.isBlank() ||
            proyecto.areaAplicacion.contains(areaFiltro, true)
        val porSimilitud = maximaSimilitud(proyecto.id) >= minSimilitud
        porEstado && porTexto && porPrograma && porFicha && porInstructor && porArea && porSimilitud
    }
    val totalPaginas = ((proyectosFiltrados.size + porPagina - 1) / porPagina).coerceAtLeast(1)
    val paginaActual = pagina.coerceIn(1, totalPaginas)
    val proyectos = proyectosFiltrados.drop((paginaActual - 1) * porPagina).take(porPagina)

    Scaffold(
        topBar = {
            SenaTopBar(
                title = "Proyectos",
                onBack = onBack,
                showProfile = true,
                showNotifications = true,
            )
        },
        containerColor = senaColors().background,
    ) { paddingValues ->
        when (val estado = adminState) {
            is AdminUiState.Loading ->
                SenaLoadingState(modifier = Modifier.padding(paddingValues))
            is AdminUiState.Error -> Box(
                Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                SenaErrorState(message = estado.message, onRetry = { adminViewModel.refresh() })
            }
            is AdminUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item {
                        SenaPageHeader(
                            title = "Proyectos",
                            subtitle = "Supervisión e intervención de propuestas (${estado.projects.size}).",
                            icon = Icons.Default.FolderOpen,
                        )
                    }
                    item {
                        SenaCard(elevation = 1.dp) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                SenaTextField(
                                    value = busqueda,
                                    onValueChange = { busqueda = it; pagina = 1 },
                                    label = "",
                                    placeholder = "Buscar por título o aprendiz...",
                                    leadingIcon = Icons.Default.Search,
                                )
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
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    SenaTextField(
                                        value = instructorFiltro,
                                        onValueChange = { instructorFiltro = it; pagina = 1 },
                                        label = "",
                                        placeholder = "Instructor",
                                        leadingIcon = Icons.Default.Person,
                                        modifier = Modifier.weight(1f),
                                    )
                                    SenaTextField(
                                        value = areaFiltro,
                                        onValueChange = { areaFiltro = it; pagina = 1 },
                                        label = "",
                                        placeholder = "Área",
                                        leadingIcon = Icons.Default.Category,
                                        modifier = Modifier.weight(1f),
                                    )
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
                                Row(
                                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    filtros.forEach { filtro ->
                                        SenaChip(
                                            text = filtro,
                                            color = when (filtro) {
                                                "Aprobado" -> senaColors().success
                                                "En revisión" -> senaColors().warning
                                                "Rechazado" -> senaColors().danger
                                                "Borrador" -> senaColors().accent
                                                else -> senaColors().green
                                            },
                                            isSelected = estadoFiltro == filtro,
                                            onClick = { estadoFiltro = filtro; pagina = 1 },
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (proyectos.isEmpty()) {
                        item { SenaEmptyState(message = "No hay proyectos que coincidan.", icon = Icons.Default.SearchOff) }
                    } else {
                        items(proyectos, key = { it.id }) { proyecto ->
                            SenaCard(
                            elevation = 0.5.dp,
                            onClick = { onProjectDetail(proyecto.id) },
                            selected = LocalSelectedListItemId.current == proyecto.id,
                        ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            proyecto.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = senaColors().text,
                                            modifier = Modifier.weight(1f),
                                        )
                                        SenaStatusBadge(status = proyecto.estado)
                                    }
                                    Text(
                                        "${proyecto.studentName ?: "Sin aprendiz"} · ${proyecto.programa ?: "Sin programa"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = senaColors().textLight,
                                    )
                                    Text(
                                        "Ficha ${proyecto.fichaId ?: "—"} · ${proyecto.instructorName ?: "Sin instructor"} · ${proyecto.areaAplicacion.ifBlank { "Sin área" }} · Similitud ${maximaSimilitud(proyecto.id)}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = senaColors().textLight,
                                    )
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
        }
    }
}
