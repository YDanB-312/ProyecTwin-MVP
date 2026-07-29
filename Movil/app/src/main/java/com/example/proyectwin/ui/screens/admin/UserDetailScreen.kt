package com.example.proyectwin.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectwin.data.mock.MockDataProvider
import com.example.proyectwin.data.model.Project
import com.example.proyectwin.navigation.AppNavigation
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*
import com.example.proyectwin.ui.viewmodel.AdminViewModel
import com.example.proyectwin.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(
    userId: String = "",
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    adminViewModel: AdminViewModel = viewModel()
) {
    val scrollState = rememberScrollState()
    var showDeactivateDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val user = remember(userId) {
        MockDataProvider.findUserById(userId.toIntOrNull() ?: 0)
    }
    val userProjects = remember(userId) {
        val uid = userId.toIntOrNull() ?: 0
        MockDataProvider.getProjectsByStudent(uid)
    }

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
        bottomBar = {
            SenaBottomBar {
                SenaButton(text = "Editar Usuario", onClick = { onNavigate(AppNavigation.ADMIN_NEW_USER) }, modifier = Modifier.weight(1f))
                SenaButton(text = "Desactivar", onClick = { showDeactivateDialog = true }, isPrimary = false, containerColor = senaColors().danger, modifier = Modifier.weight(1f))
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
                title = "Detalle de Usuario",
                subtitle = "Información completa y actividad del usuario en el sistema.",
                icon = Icons.Default.Person
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.linearGradient(colors = listOf(senaColors().header, senaColors().green)))
                    .padding(24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(user?.initials ?: "??", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 24.sp)
                        }
                    }
                    Spacer(Modifier.width(20.dp))
                    Column {
                        Text(user?.name ?: "Usuario no encontrado", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(user?.email ?: "", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                    }
                }
            }

            SenaSectionHeader(title = "Información Personal")
            SenaCard(elevation = 1.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    UserDetailRow(Icons.Default.School, "Rol", user?.roleDisplayName ?: "N/A")
                    HorizontalDivider(color = senaColors().borderSoft)
                    UserDetailRow(Icons.Default.Badge, "Documento", user?.documentoIdentidad ?: "N/A")
                    HorizontalDivider(color = senaColors().borderSoft)
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Rol", style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
                            Spacer(Modifier.height(4.dp))
                            SenaStatusBadge(status = user?.roleDisplayName ?: "N/A")
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Estado", style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
                            Spacer(Modifier.height(4.dp))
                            SenaStatusBadge(status = "Activo")
                        }
                    }
                }
            }

            SenaSectionHeader(title = "Actividad en el Sistema")
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SenaMetricPill(Icons.Default.FolderOpen, "${userProjects.size}", "Proyectos")
                    val simCount = userProjects.sumOf { p ->
                        MockDataProvider.getSimilaritiesByProject(p.id).size
                    }
                    SenaMetricPill(Icons.Default.Search, "$simCount", "Similitudes")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    val bugCount = userProjects.sumOf { p ->
                        MockDataProvider.getBugReportsByProject(p.id).size
                    }
                    SenaMetricPill(Icons.Default.CheckCircle, "${userProjects.size}", "Revisiones")
                    SenaMetricPill(Icons.Default.BugReport, "$bugCount", "Reportes")
                }
            }

            if (userProjects.isNotEmpty()) {
                SenaSectionHeader(title = "Proyectos del Usuario")
                userProjects.forEach { project ->
                    SenaCard(
                        elevation = 0.5.dp,
                        onClick = { onNavigate(AppNavigation.ADMIN_PROJECT_DETAIL.replace("{projectId}", "${project.id}")) }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(project.title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = senaColors().text)
                                Text(project.statusDisplay, style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
                            }
                            SenaStatusBadge(status = project.statusDisplay)
                        }
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }

    if (showDeactivateDialog) {
        AlertDialog(
            onDismissRequest = { showDeactivateDialog = false },
            title = { Text("Desactivar Usuario") },
            text = { Text("¿Estás seguro de que deseas desactivar esta cuenta? El usuario ya no podrá iniciar sesión.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeactivateDialog = false
                    scope.launch {
                        snackbarHostState.showSnackbar("Cuenta desactivada")
                    }
                }) {
                    Text("Desactivar", color = senaColors().danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeactivateDialog = false }) {
                    Text("Cancelar")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun UserDetailRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = senaColors().textLight, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
            Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = senaColors().text)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserDetailScreenPreview() {
    ProyecTwinTheme {
        UserDetailScreen(onBack = {}, onNavigate = {})
    }
}
