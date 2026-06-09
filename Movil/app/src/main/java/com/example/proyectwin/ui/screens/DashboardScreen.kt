package com.example.proyectwin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(onNavigate: (String) -> Unit) {
    Scaffold(
        topBar = {
            SenaTopBar(
                title = "ProyecTwin",
                onNavigateToProfile = { onNavigate("main/profile") },
                onNavigateToAlerts = { onNavigate("main/alerts") }
            )
        },
        containerColor = SenaBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Dashboard Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Hola, María",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = SenaText
                        )
                        Text(
                            "Gestiona tus proyectos de formación.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SenaTextSecondary
                        )
                    }
                    Text(
                        "28 may. 2026",
                        style = MaterialTheme.typography.labelSmall,
                        color = SenaTextLight
                    )
                }
            }

            // Welcome Card (Fiel al Frontend)
            item {
                PremiumWelcomeCard()
            }
            
            // Quick Actions
            item {
                SenaSectionHeader(title = "Acciones rápidas")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SenaActionCard(
                        title = "Nuevo proyecto",
                        subtitle = "Inicia una idea",
                        icon = Icons.Default.Add,
                        onClick = { onNavigate("project/new") },
                        modifier = Modifier.weight(1f)
                    )
                    SenaActionCard(
                        title = "Mis proyectos",
                        subtitle = "Continúa",
                        icon = Icons.Default.Folder,
                        onClick = { onNavigate("main/projects") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Recent Projects Header
            item {
                SenaSectionHeader(
                    title = "Mis proyectos",
                    actionText = "Ver todos",
                    onActionClick = { onNavigate("main/projects") }
                )
            }
            
            val dummyProjects = listOf(
                ProjectItem("Sistema de Gestion Academica", "En revisión", SenaAccent, "15 mar 2023", 3, "Carlos Ruiz", "CR"),
                ProjectItem("Aplicacion Web de Inventarios", "Aprobado", SenaSuccess, "22 abr 2023", 2, "Ana Gomez", "AG")
            )
            
            items(dummyProjects) { project ->
                ProjectListItemCard(project, onClick = { onNavigate("project/detail/1") })
            }
            
            // Notifications Placeholder
            item {
                SenaSectionHeader(title = "Notificaciones")
                EmptyStateCard(
                    message = "No hay notificaciones nuevas",
                    icon = Icons.Default.NotificationsNone
                )
            }
            
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun PremiumWelcomeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(SenaHeader, SenaGreen)
                    )
                )
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Hola, María",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        "¡Bienvenida al Sistema ProyecTwin!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "Evita similitudes con otras propuestas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                Icon(
                    Icons.Default.RocketLaunch,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(56.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    ProyecTwinTheme {
        DashboardScreen(onNavigate = {})
    }
}
