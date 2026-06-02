package com.example.proyectwin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(onNavigate: (String) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "ProyecTwin",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SenaHeader
                ),
                actions = {
                    IconButton(onClick = { onNavigate("main/alerts") }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notificaciones", tint = Color.White)
                    }
                    Surface(
                        modifier = Modifier.padding(end = 12.dp).size(36.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        IconButton(onClick = { onNavigate("main/profile") }) {
                            Icon(Icons.Default.Person, contentDescription = "Perfil", tint = Color.White)
                        }
                    }
                }
            )
        },
        containerColor = SenaBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                PremiumWelcomeCard(onJoinFicha = { onNavigate("ficha/join") })
            }
            
            item {
                Text(
                    "Resumen de Actividad",
                    style = MaterialTheme.typography.titleMedium,
                    color = SenaText
                )
            }
            
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ModernStatCard(
                            title = "Proyectos",
                            value = "12",
                            icon = Icons.Default.FolderCopy,
                            color = SenaGreen,
                            modifier = Modifier.weight(1f)
                        )
                        ModernStatCard(
                            title = "Similitudes",
                            value = "03",
                            icon = Icons.Default.Troubleshoot,
                            color = SenaDanger,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ModernStatCard(
                            title = "Alertas",
                            value = "05",
                            icon = Icons.Default.ReportProblem,
                            color = SenaWarning,
                            modifier = Modifier.weight(1f)
                        )
                        ModernStatCard(
                            title = "Fichas",
                            value = "02",
                            icon = Icons.Default.AssignmentInd,
                            color = SenaAccent,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Proyectos Recientes",
                        style = MaterialTheme.typography.titleMedium,
                        color = SenaText
                    )
                    TextButton(onClick = { onNavigate("main/projects") }) {
                        Text("Ver todos", color = SenaGreen, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            
            val dummyProjects = listOf(
                ProjectItem("Sistema de Riego Automatizado", "Pendiente", SenaWarning, "Hace 2 horas"),
                ProjectItem("App de Gestión de Inventario", "Aprobado", SenaSuccess, "Ayer"),
                ProjectItem("Plataforma de E-learning", "En Revisión", SenaGreen, "Hace 3 días")
            )
            
            items(dummyProjects) { project ->
                ProjectListItemCard(project, onClick = { onNavigate("project/detail/1") })
            }
        }
    }
}

@Composable
fun PremiumWelcomeCard(onJoinFicha: () -> Unit) {
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
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "¡Hola de nuevo!",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White
                        )
                        Text(
                            "Aprendiz SENA",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    Icon(
                        Icons.Default.WavingHand,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                // Botón para unirse a ficha si no tiene
                Button(
                    onClick = onJoinFicha,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.GroupAdd, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Unirse a una Ficha", color = Color.White, style = MaterialTheme.typography.labelMedium)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { 0.6f },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.2f),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "60% de tus tareas completadas",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun ModernStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    SenaCard(
        modifier = modifier,
        elevation = 2.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = color.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
                }
            }
            Column {
                Text(value, style = MaterialTheme.typography.titleLarge, color = SenaText)
                Text(title, style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
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
