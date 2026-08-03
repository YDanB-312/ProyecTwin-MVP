package com.example.proyectwin.ui.screens.instructor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectwin.data.mock.MockDataProvider
import com.example.proyectwin.data.model.BugReport
import com.example.proyectwin.data.model.Project
import com.example.proyectwin.data.model.ProjectStatus
import com.example.proyectwin.data.model.Similarity
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*
import com.example.proyectwin.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleProyectoInstructorScreen(
    projectId: String = "",
    onBack: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var observationText by remember { mutableStateOf("") }

    var project by remember(projectId) {
        mutableStateOf(
            MockDataProvider.findProjectById(projectId.toIntOrNull() ?: 0)
        )
    }
    val bugReports = remember(projectId) {
        val pid = projectId.toIntOrNull() ?: 0
        MockDataProvider.getBugReportsByProject(pid)
    }
    val similarities = remember(projectId) {
        val pid = projectId.toIntOrNull() ?: 0
        MockDataProvider.getSimilaritiesByProject(pid)
    }
    val maxSimilarity = similarities.maxOfOrNull { it.similitud } ?: 0.0

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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            SenaBottomBar {
                SenaButton(text = "Aprobar", onClick = {
                    project?.let { p ->
                        MockDataProvider.updateProjectEstado(p.id, ProjectStatus.APROBADO.value)
                        project = p.copy(estado = ProjectStatus.APROBADO.value)
                    }
                    scope.launch {
                        snackbarHostState.showSnackbar("Proyecto aprobado")
                        kotlinx.coroutines.delay(800)
                        onBack()
                    }
                }, modifier = Modifier.weight(1f))
                SenaButton(text = "Rechazar", onClick = {
                    project?.let { p ->
                        MockDataProvider.updateProjectEstado(p.id, ProjectStatus.RECHAZADO.value)
                        project = p.copy(estado = ProjectStatus.RECHAZADO.value)
                    }
                    scope.launch {
                        snackbarHostState.showSnackbar("Proyecto rechazado")
                        kotlinx.coroutines.delay(800)
                        onBack()
                    }
                }, isPrimary = false, modifier = Modifier.weight(1f), containerColor = senaColors().danger)
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
                title = "Revisi�n de Proyecto",
                subtitle = "Evaluaci�n detallada de la propuesta enviada por el aprendiz.",
                icon = Icons.Default.FolderOpen
            )

            SenaCard(elevation = 1.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SenaStatusBadge(status = project?.statusDisplay ?: "Desconocido")
                        if (maxSimilarity > 0) {
                            Surface(color = senaColors().danger.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                                Text(
                                    "%d%% Similitud".format((maxSimilarity * 100).toInt()),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = senaColors().danger
                                )
                            }
                        }
                    }

                    Text(project?.title ?: "Proyecto no encontrado", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = senaColors().text)

                    HorizontalDivider(color = senaColors().borderSoft)

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        InstructorDetailRow(Icons.Default.Person, "Aprendiz", project?.studentName ?: "N/A")
                        InstructorDetailRow(Icons.Default.School, "Programa", "ADSO")
                        InstructorDetailRow(Icons.Default.CalendarToday, "Fecha", project?.createdAt ?: "Sin fecha")
                        InstructorDetailRow(Icons.Default.Work, "�rea", "Tecnolog�a e Inform�tica")
                    }
                }
            }

            SenaSectionHeader(title = "Resumen del Proyecto")
            SenaCard {
                Text(
                    project?.description ?: "Sin descripci�n disponible.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = senaColors().textSecondary,
                    lineHeight = 22.sp
                )
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Proyecto", "Sena", "Tecnolog�a").forEach { tag ->
                        SenaChip(text = tag, color = senaColors().textMuted, isSelected = false)
                    }
                }
            }

            if (bugReports.isNotEmpty()) {
                SenaSectionHeader(title = "Reportes de Error")
                bugReports.forEach { bug ->
                    SenaCard(elevation = 1.dp) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(bug.titulo, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = senaColors().text)
                                SenaStatusBadge(status = bug.statusDisplay)
                            }
                            Text(bug.descripcion, style = MaterialTheme.typography.bodySmall, color = senaColors().textSecondary)
                            Text("Tipo: ${bug.typeDisplay}", style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
                        }
                    }
                }
            }

            if (similarities.isNotEmpty()) {
                SenaSectionHeader(title = "Similitudes Detectadas")
                similarities.forEach { sim ->
                    SenaCard(elevation = 1.dp) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "${sim.project1Title} vs ${sim.project2Title}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = senaColors().text
                                )
                                Surface(color = senaColors().danger.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                                    Text(
                                        sim.similitudPercent,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = senaColors().danger
                                    )
                                }
                            }
                            Text("Estado: ${sim.statusDisplay}", style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
                        }
                    }
                }
            }

            SenaSectionHeader(title = "Observaciones")
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SenaCard(elevation = 1.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${project?.instructorName ?: "Instructor"} | Instructor", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = senaColors().text)
                            Text(project?.createdAt ?: "Sin fecha", style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
                        }
                        Text(
                            "El proyecto necesita mejorar la secci�n de an�lisis de requisitos. Se recomienda ampliar la documentaci�n t�cnica.",
                            style = MaterialTheme.typography.bodySmall,
                            color = senaColors().textSecondary
                        )
                    }
                }

                SenaCard(elevation = 2.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Agregar Observaci�n", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = senaColors().text)
                        SenaTextField(
                            value = observationText,
                            onValueChange = { observationText = it },
                            label = "",
                            placeholder = "Escribe tu comentario aqu�...",
                            modifier = Modifier.heightIn(min = 100.dp)
                        )
                        SenaButton(
                            text = "Enviar Comentario",
                            onClick = { observationText = "" },
                            icon = Icons.AutoMirrored.Filled.Send,
                            modifier = Modifier.align(Alignment.End).width(180.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun InstructorDetailRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = senaColors().textLight, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = senaColors().textLight, modifier = Modifier.width(100.dp))
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = senaColors().text)
    }
}

@Preview(showBackground = true)
@Composable
fun DetalleProyectoInstructorScreenPreview() {
    ProyecTwinTheme {
        DetalleProyectoInstructorScreen(onBack = {})
    }
}
