package com.example.proyectwin.ui.screens.admin

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectwin.data.mock.MockDataProvider
import com.example.proyectwin.data.model.BugReport
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*
import com.example.proyectwin.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BugReportDetailScreen(
    bugId: String = "",
    onBack: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val bug = remember(bugId) {
        MockDataProvider.getAllBugReports().find { it.id == (bugId.toIntOrNull() ?: 0) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SenaTopBar(
                title = "Falla T�cnica",
                onBack = onBack,
                showProfile = true,
                showNotifications = true
            )
        },
        containerColor = senaColors().background,
        bottomBar = {
            SenaBottomBar {
                SenaButton(
                    text = "ASIGNAR T�CNICO",
                    onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Reporte escalado a Nivel 2")
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                SenaButton(
                    text = "CERRAR CASO", 
                    onClick = { onBack() }, 
                    isPrimary = false, 
                    containerColor = senaColors().danger,
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
        ) {
            // --- HEADER ADMIN ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(senaColors().danger.copy(alpha = 0.8f), senaColors().danger)
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .offset(y = (-50).dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Info Card
                SenaCard(elevation = 8.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SenaStatusBadge(status = bug?.statusDisplay ?: "Desconocido")
                            Text("#${bug?.id ?: bugId}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = senaColors().textLight)
                        }

                        Text(
                            bug?.titulo ?: "Error en el Sistema", 
                            style = MaterialTheme.typography.titleLarge, 
                            fontWeight = FontWeight.Black, 
                            color = senaColors().text
                        )

                        HorizontalDivider(color = senaColors().borderSoft)

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            BugDetailRow(Icons.Default.Person, "Reportado por", bug?.reporterName ?: "Aprendiz")
                            BugDetailRow(Icons.Default.PriorityHigh, "Criticidad", bug?.typeDisplay ?: "Alta")
                            BugDetailRow(Icons.Default.AccessTime, "Antig�edad", "2 horas")
                        }
                    }
                }

                SenaSectionHeader(title = "Descripci�n del Error")
                SenaCard {
                    Text(
                        bug?.descripcion ?: "Sin descripci�n t�cnica.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = senaColors().textSecondary,
                        lineHeight = 22.sp
                    )
                }

                SenaSectionHeader(title = "Logs de Actividad")
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    BugActivityItem("Admin Sistema", "Recibi� el reporte y valid� la captura de pantalla.", "10:30 AM")
                    BugActivityItem("Servidor IA", "Reinicio de m�dulo de comparaci�n exitoso.", "11:15 AM")
                }

                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

@Composable
fun BugDetailRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = senaColors().textMuted, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = senaColors().textLight, modifier = Modifier.width(100.dp))
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = senaColors().text)
    }
}

@Composable
fun BugActivityItem(user: String, text: String, date: String) {
    SenaCard(elevation = 1.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(user, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = senaColors().green)
                Text(date, style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
            }
            Text(text, style = MaterialTheme.typography.bodySmall, color = senaColors().textSecondary)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BugReportDetailScreenPreview() {
    ProyecTwinTheme {
        BugReportDetailScreen(bugId = "001", onBack = {})
    }
}
