package com.example.proyectwin.ui.screens.aprendiz

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
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
import com.example.proyectwin.data.model.Project
import com.example.proyectwin.navigation.AppNavigation
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.proyectwin.ui.viewmodel.ProjectDetailUiState
import com.example.proyectwin.ui.viewmodel.ProjectDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    projectId: String = "",
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    projectDetailViewModel: ProjectDetailViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val uiState by projectDetailViewModel.uiState.collectAsState()

    LaunchedEffect(projectId) {
        projectId.toIntOrNull()?.let { projectDetailViewModel.loadProject(it) }
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
            when (uiState) {
                is ProjectDetailUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.size(48.dp))
                }
                is ProjectDetailUiState.Error -> {
                    SenaAlertBanner(
                        title = "Error",
                        message = (uiState as ProjectDetailUiState.Error).message,
                        icon = Icons.Default.Error,
                        color = senaColors().danger
                    )
                }
                is ProjectDetailUiState.Success -> {
                    val project = (uiState as ProjectDetailUiState.Success).project
                    SenaPageHeader(
                        title = project.title,
                        subtitle = "Detalle del proyecto de formación",
                        icon = Icons.Default.FolderOpen
                    )

                    SenaSectionHeader(title = "Información General")
                    SenaCard(elevation = 1.dp) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            DetailRowItem(Icons.Default.Title, "Nombre del Proyecto", project.title)
                            HorizontalDivider(color = senaColors().borderSoft)
                            DetailRowItem(Icons.Default.Person, "Aprendiz", project.studentName ?: "Sin asignar")
                            HorizontalDivider(color = senaColors().borderSoft)
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    DetailRowItem(Icons.Default.CalendarToday, "Fecha", project.createdAt ?: "Sin fecha")
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Estado", style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
                                    Spacer(Modifier.height(4.dp))
                                    SenaStatusBadge(status = project.estado)
                                }
                            }
                            HorizontalDivider(color = senaColors().borderSoft)
                            Column {
                                Text("Descripción", style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    project.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = senaColors().textSecondary,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }

                    SenaSectionHeader(title = "Observaciones del Instructor")
                    SenaEmptyState(message = "No hay observaciones para este proyecto.", icon = Icons.AutoMirrored.Filled.Chat)
                }
            }

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
