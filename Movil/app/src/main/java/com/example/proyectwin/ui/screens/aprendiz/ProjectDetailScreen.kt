package com.example.proyectwin.ui.screens.aprendiz

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
import com.example.proyectwin.data.model.Similarity
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.navigation.AppNavigation
import com.example.proyectwin.ui.theme.*
import com.example.proyectwin.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    projectId: String = "",
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val scrollState = rememberScrollState()
    val project = remember(projectId) {
        MockDataProvider.findProjectById(projectId.toIntOrNull() ?: 0)
    }
    val bugReports = remember(projectId) {
        val pid = projectId.toIntOrNull() ?: 0
        MockDataProvider.getBugReportsByProject(pid)
    }
    val similarities = remember(projectId) {
        val pid = projectId.toIntOrNull() ?: 0
        MockDataProvider.getSimilaritiesByProject(pid)
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
        bottomBar = {
            SenaBottomBar {
                SenaButton(
                    text = "Ver Similitudes",
                    onClick = { onNavigate(AppNavigation.APRENDIZ_SIMILARITY.replace("{projectId}", projectId)) },
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Warning
                )
                SenaButton(
                    text = "Editar",
                    onClick = { onNavigate(AppNavigation.APRENDIZ_NEW_PROJECT.replace("{projectId}", projectId)) },
                    isPrimary = false,
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Edit
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SenaPageHeader(
                title = project?.title ?: "Proyecto no encontrado",
                subtitle = "Detalle del proyecto de formaci�n",
                icon = Icons.Default.FolderOpen
            )

            SenaSectionHeader(title = "Informaci�n General")
            SenaCard(elevation = 1.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    DetailRowItem(Icons.Default.Title, "Nombre del Proyecto", project?.title ?: "N/A")
                    HorizontalDivider(color = senaColors().borderSoft)
                    DetailRowItem(Icons.Default.Person, "Aprendiz", project?.studentName ?: "N/A")
                    HorizontalDivider(color = senaColors().borderSoft)
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            DetailRowItem(Icons.Default.CalendarToday, "Fecha", project?.createdAt ?: "Sin fecha")
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Estado", style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
                            Spacer(Modifier.height(4.dp))
                            project?.let { SenaStatusBadge(status = it.statusDisplay) }
                        }
                    }
                    HorizontalDivider(color = senaColors().borderSoft)
                    Column {
                        Text("Descripci�n", style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            project?.description ?: "Sin descripci�n",
                            style = MaterialTheme.typography.bodyMedium,
                            color = senaColors().textSecondary,
                            lineHeight = 22.sp
                        )
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

            SenaSectionHeader(title = "Observaciones del Instructor")
            SenaEmptyState(message = "No hay observaciones para este proyecto.", icon = Icons.AutoMirrored.Filled.Chat)

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun DetailRowItem(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = RoundedCornerShape(8.dp),
            color = senaColors().green.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = senaColors().green, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = senaColors().text)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProjectDetailScreenPreview() {
    ProyecTwinTheme {
        ProjectDetailScreen(onBack = {}, onNavigate = {})
    }
}
