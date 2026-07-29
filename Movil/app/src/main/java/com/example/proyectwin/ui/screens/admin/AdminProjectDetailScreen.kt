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
import com.example.proyectwin.navigation.AppNavigation
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProjectDetailScreen(projectId: String = "", onBack: () -> Unit, onNavigate: (String) -> Unit) {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SenaTopBar(
                title = "Auditoría de Proyecto",
                onBack = onBack,
                showProfile = true,
                showNotifications = true
            )
        },
        containerColor = senaColors().background,
        bottomBar = {
            SenaBottomBar {
                SenaButton(
                    text = "HISTORIAL", 
                    onClick = { 
                        scope.launch {
                            snackbarHostState.showSnackbar("Bitácora técnica encriptada")
                        }
                    }, 
                    isPrimary = false, 
                    modifier = Modifier.weight(1f)
                )
                SenaButton(
                    text = "SIMILITUDES", 
                    onClick = { onNavigate(AppNavigation.ADMIN_SIMILARITY_DETAIL) }, 
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Troubleshoot
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
            // --- HEADER PREMIUM ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF022C22), Color(0xFF064E3B))
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .offset(y = (-60).dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Info Principal Flotante
                SenaCard(elevation = 8.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SenaStatusBadge(status = "Aprobado")
                            Text("#PRJ-2568", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = senaColors().textLight)
                        }

                        Text(
                            "Sistema IoT para Agricultura de Precisión", 
                            style = MaterialTheme.typography.titleLarge, 
                            fontWeight = FontWeight.Black, 
                            color = senaColors().text,
                            lineHeight = 28.sp
                        )

                        HorizontalDivider(color = senaColors().borderSoft)

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            AdminProjectDetailRow(Icons.Default.School, "Programa", "ADSO")
                            AdminProjectDetailRow(Icons.Default.Person, "Aprendiz Líder", "Maria Gonzalez")
                            AdminProjectDetailRow(Icons.Default.SupervisorAccount, "Instructor", "Carlos Ruiz")
                        }
                    }
                }

                SenaSectionHeader(title = "Resumen Ejecutivo")
                SenaCard {
                    Text(
                        "Desarrollo de un sistema basado en sensores IoT para monitorear variables ambientales en cultivos, permitiendo la toma de decisiones en tiempo real.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = senaColors().textSecondary,
                        lineHeight = 22.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("IoT", "SENA", "AI").forEach { tag ->
                            SenaChip(text = tag, color = senaColors().green)
                        }
                    }
                }

                SenaSectionHeader(title = "Equipo Técnico")
                SenaCard {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        TeamMemberRow("MG", "Maria Gonzalez", "Líder de Proyecto", true)
                        TeamMemberRow("JP", "Juan Pérez", "Desarrollador Backend")
                        TeamMemberRow("LG", "Laura Gómez", "Diseñadora UI/UX")
                    }
                }

                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

@Composable
fun AdminProjectDetailRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(modifier = Modifier.size(28.dp), shape = CircleShape, color = senaColors().background) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = senaColors().green, modifier = Modifier.size(14.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = senaColors().textLight, modifier = Modifier.width(100.dp))
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = senaColors().text)
    }
}

@Preview(showBackground = true)
@Composable
fun AdminProjectDetailScreenPreview() {
    ProyecTwinTheme {
        AdminProjectDetailScreen(onBack = {}, onNavigate = {})
    }
}
