package com.example.proyectwin.ui.screens.aprendiz

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.proyectwin.data.model.GeneralUser
import com.example.proyectwin.data.model.ProjectDraft
import com.example.proyectwin.data.model.ProjectStatus
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*
import com.example.proyectwin.ui.viewmodel.ProjectsViewModel
import com.example.proyectwin.ui.viewmodel.TeamSelectionUiState
import com.example.proyectwin.ui.viewmodel.TeamSelectionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewProjectScreen(
    onBack: () -> Unit,
    onSubmit: (ProjectDraft) -> Unit = { },
    onSuccess: (Int) -> Unit = { },
    projectId: String = "",
    projectsViewModel: ProjectsViewModel = hiltViewModel(),
    teamSelectionViewModel: TeamSelectionViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf("") }
    var summary by remember { mutableStateOf("") }
    var keywords by remember { mutableStateOf("") }
    var objectives by remember { mutableStateOf("") }
    var deliverables by remember { mutableStateOf("") }
    var observations by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("6") }
    var projectType by remember { mutableStateOf("aplicacion") }

    val scrollState = rememberScrollState()
    val createState by projectsViewModel.createState.collectAsState()
    val teamUiState by teamSelectionViewModel.uiState.collectAsState()

    LaunchedEffect(createState) {
        if (createState is ProjectsViewModel.CreateState.Success) {
            onSuccess((createState as ProjectsViewModel.CreateState.Success).projectId)
            projectsViewModel.resetCreateState()
        }
    }

    LaunchedEffect(Unit) {
        teamSelectionViewModel.loadTeam()
    }

    val draft = remember(title, summary, keywords, projectType, objectives, deliverables) {
        ProjectDraft(
            titulo = title,
            resumen = summary,
            palabrasClave = if (keywords.isNotBlank()) keywords.split(",").map { it.trim() }.filter { it.isNotBlank() }.joinToString(",") else null,
            areaAplicacion = projectType,
            objetivoGeneral = objectives,
            objetivosEspecificos = if (deliverables.isNotBlank()) deliverables.split(",").map { it.trim() }.filter { it.isNotBlank() } else emptyList(),
            estado = ProjectStatus.PENDIENTE.value,
            idCreador = 0,
            idInstructorAsignado = null,
            idClassGroup = null
        )
    }

    val selectedIds = when (val state = teamUiState) {
        is TeamSelectionUiState.Success -> state.selectedIds
        else -> emptySet()
    }

    Scaffold(
        topBar = {
            SenaTopBar(
                title = "ProyecTwin",
                onBack = onBack,
                showProfile = false,
                showNotifications = false
            )
        },
        containerColor = senaColors().background,
        bottomBar = {
            SenaBottomBar {
                SenaButton(
                    text = "Cancelar",
                    onClick = onBack,
                    isPrimary = false,
                    modifier = Modifier.weight(1f)
                )
                SenaButton(
                    text = "Guardar Proyecto",
                    onClick = {
                        if (title.isBlank() || summary.isBlank() || objectives.isBlank() || deliverables.isBlank()) {
                            return@SenaButton
                        }
                        val team = selectedIds.toList()
                        projectsViewModel.crear(draft, team)
                    },
                    isLoading = createState is ProjectsViewModel.CreateState.Loading,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            SenaPageHeader(
                title = "Nuevo Proyecto",
                subtitle = "Inicia una idea desde cero y compártela con tus instructores.",
                icon = Icons.Default.AddCircle
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Paso 1 de 4: Información Básica",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = senaColors().green
                )
                LinearProgressIndicator(
                    progress = { 0.25f },
                    modifier = Modifier.width(100.dp).height(6.dp),
                    color = senaColors().green,
                    trackColor = senaColors().borderSoft,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }

            if (createState is ProjectsViewModel.CreateState.Error) {
                SenaAlertBanner(
                    title = "Error al crear el proyecto",
                    message = (createState as ProjectsViewModel.CreateState.Error).message,
                    icon = Icons.Default.Error,
                    color = senaColors().danger
                )
                val fieldErrors = (createState as ProjectsViewModel.CreateState.Error).fieldErrors
                fieldErrors.forEach { (campo, error) ->
                    Text(
                        text = "$campo: $error",
                        style = MaterialTheme.typography.bodySmall,
                        color = senaColors().danger,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }
            }

            SenaSectionHeader(title = "Información del Proyecto")
            SenaCard {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    SenaTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = "Título del proyecto *",
                        placeholder = "Ej: Sistema de Gestión IoT"
                    )

                    SenaTextField(
                        value = summary,
                        onValueChange = { summary = it },
                        label = "Resumen ejecutivo *",
                        placeholder = "Describe brevemente el alcance...",
                        modifier = Modifier.heightIn(min = 120.dp)
                    )

                    SenaTextField(
                        value = keywords,
                        onValueChange = { keywords = it },
                        label = "Palabras clave (separadas por coma) *",
                        placeholder = "Desarrollo, IoT, Sena"
                    )
                }
            }

            SenaSectionHeader(title = "Objetivos y Entregables")
            SenaCard {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    SenaTextField(
                        value = objectives,
                        onValueChange = { objectives = it },
                        label = "Objetivos generales *",
                        placeholder = "Define lo que esperas lograr...",
                        modifier = Modifier.heightIn(min = 100.dp)
                    )

                    SenaTextField(
                        value = deliverables,
                        onValueChange = { deliverables = it },
                        label = "Entregables esperados *",
                        placeholder = "Software, Documentación, etc.",
                        modifier = Modifier.heightIn(min = 100.dp)
                    )
                }
            }

            SenaSectionHeader(title = "Equipo")
            SenaCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    when (val state = teamUiState) {
                        is TeamSelectionUiState.Loading -> {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }
                        is TeamSelectionUiState.Error -> {
                            Text(state.message, color = senaColors().danger)
                        }
                        is TeamSelectionUiState.Success -> {
                            if (state.members.isEmpty()) {
                                Text("No hay compañeros disponibles en tu ficha.", color = senaColors().textSecondary)
                            } else {
                                TeamMemberList(
                                    members = state.members,
                                    selectedIds = state.selectedIds,
                                    onToggle = { teamSelectionViewModel.toggleMember(it) }
                                )
                            }
                            val selectedList = state.members.filter { it.id in state.selectedIds }
                            if (selectedList.isNotEmpty()) {
                                HorizontalDivider(color = senaColors().borderSoft, modifier = Modifier.padding(vertical = 8.dp))
                                Text("Seleccionados:", style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    selectedList.forEach { member ->
                                        Chip(
                                            label = member.name ?: member.email,
                                            onClick = { teamSelectionViewModel.removeSelected(member.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            SenaSectionHeader(title = "Información Técnica")
            SenaCard {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Text("Tipo de proyecto", style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
                    Row(horizontalArrangement = Arrangement.SpaceBetween) {
                        SenaChip(
                            text = "Software",
                            color = senaColors().green,
                            isSelected = projectType == "aplicacion",
                            onClick = { projectType = "aplicacion" }
                        )
                        SenaChip(
                            text = "Investigación",
                            color = senaColors().accent,
                            isSelected = projectType == "investigacion",
                            onClick = { projectType = "investigacion" }
                        )
                    }

                    SenaTextField(
                        value = duration,
                        onValueChange = { duration = it },
                        label = "Duración estimada (meses)",
                        placeholder = "6"
                    )
                }
            }

            Spacer(Modifier.height(100.dp))
        }
    }
}

@Composable
fun TeamMemberList(
    members: List<GeneralUser>,
    selectedIds: Set<Int>,
    onToggle: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        members.forEach { member ->
            val selected = selectedIds.contains(member.id)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle(member.id) }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(
                            if (selected) senaColors().green else Color.Transparent,
                            shape = RoundedCornerShape(4.dp)
                        )
                )
                Spacer(Modifier.width(12.dp))
                Text(member.name ?: member.email, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun Chip(label: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = senaColors().success.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, senaColors().success)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            color = senaColors().success
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NewProjectScreenPreview() {
    ProyecTwinTheme {
        NewProjectScreen(onBack = {})
    }
}
