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
import com.example.proyectwin.navigation.AppNavigation
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(userId: String = "", onBack: () -> Unit, onNavigate: (String) -> Unit) {
    val scrollState = rememberScrollState()
    var showDeactivateDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
        containerColor = SenaBackground,
        bottomBar = {
            SenaBottomBar {
                SenaButton(text = "Editar Usuario", onClick = { onNavigate(AppNavigation.ADMIN_NEW_USER) }, modifier = Modifier.weight(1f))
                SenaButton(text = "Desactivar", onClick = { showDeactivateDialog = true }, isPrimary = false, containerColor = SenaDanger, modifier = Modifier.weight(1f))
            }
        }
    ) { paddingValues ->
        // ... rest of content ...
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

            // User Info Card (Gradient Header like MiPerfil)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.linearGradient(colors = listOf(SenaHeader, SenaGreen)))
                    .padding(24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("MG", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 24.sp)
                        }
                    }
                    Spacer(Modifier.width(20.dp))
                    Column {
                        Text("Maria Gonzalez", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("maria.gonzalez@soy.sena.edu.co", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                    }
                }
            }

            SenaSectionHeader(title = "Información Personal")
            SenaCard(elevation = 1.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    UserDetailRow(Icons.Default.School, "Programa", "ADSO - Análisis y Desarrollo")
                    HorizontalDivider(color = SenaBorderSoft)
                    UserDetailRow(Icons.Default.Badge, "Documento", "1023456789")
                    HorizontalDivider(color = SenaBorderSoft)
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Rol", style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
                            Spacer(Modifier.height(4.dp))
                            SenaStatusBadge(status = "Aprendiz")
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Estado", style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
                            Spacer(Modifier.height(4.dp))
                            SenaStatusBadge(status = "Activo")
                        }
                    }
                }
            }

            SenaSectionHeader(title = "Actividad en el Sistema")
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SenaMetricPill(Icons.Default.FolderOpen, "2", "Proyectos")
                    SenaMetricPill(Icons.Default.Search, "1", "Similitudes")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SenaMetricPill(Icons.Default.CheckCircle, "2", "Revisiones")
                    SenaMetricPill(Icons.Default.BugReport, "0", "Reportes")
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
                    Text("Desactivar", color = SenaDanger)
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
        Icon(icon, contentDescription = null, tint = SenaTextLight, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
            Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = SenaText)
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
