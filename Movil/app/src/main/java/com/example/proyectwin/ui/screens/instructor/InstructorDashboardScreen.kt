package com.example.proyectwin.ui.screens.instructor

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

data class PendingProject(
    val id: String,
    val title: String,
    val student: String,
    val date: String,
    val program: String = "ADSO",
)

data class InstructorDashboardData(
    val userName: String = "Carlos",
    val date: String = "28 may. 2026",
    val pendingProjects: List<PendingProject> = emptyList(),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructorDashboardScreen(onNavigate: (String) -> Unit) {
    val data = remember {
        InstructorDashboardData(
            pendingProjects = listOf(
                PendingProject("1", "Sistema IoT para Agricultura", "Ana Martínez", "15 Nov 2026"),
                PendingProject("2", "App Movil para Turismo Local", "Juan Pérez", "14 Nov 2026"),
                PendingProject("3", "Plataforma de Reservas Médicas", "Diego Mora", "12 Nov 2026"),
            )
        )
    }

    Scaffold(
        topBar = {
            SenaTopBar(
                title = "ProyecTwin",
                onNavigateToProfile = { onNavigate(AppNavigation.INSTRUCTOR_PROFILE) },
                onNavigateToAlerts = { onNavigate(AppNavigation.INSTRUCTOR_ALERTS) },
                onLogout = { onNavigate(AppNavigation.HOME) }
            )
        },
        containerColor = SenaBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // --- INSTRUCTOR PRODUCTIVITY HEADER ---
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(bottomStart = 48.dp, bottomEnd = 48.dp))
                        .background(Brush.verticalGradient(colors = listOf(Color(0xFF0F172A), SenaHeader))) // Midnight to Emerald
                        .padding(24.dp)
                ) {
                    Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Center) {
                        Text(
                            "Gestión de Proyectos",
                            style = MaterialTheme.typography.labelMedium,
                            color = SenaAccent,
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Instructor ${data.userName}",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Spacer(Modifier.height(16.dp))
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            InstructorSummaryMetric("24", "Activos", SenaAccent)
                            InstructorSummaryMetric("3", "Pendientes", SenaWarning)
                        }
                    }
                }
            }

            // --- INBOX FILTERS / SEARCH ---
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    SenaTextField(
                        value = "",
                        onValueChange = {},
                        label = "",
                        placeholder = "Buscar propuesta o aprendiz...",
                        leadingIcon = Icons.Default.Search
                    )
                }
            }

            // --- PRODUCTIVITY INBOX (PENDING PROPOSALS) ---
            item {
                Column {
                    PaddingRow {
                        SenaSectionHeader(
                            title = "Inbox de Revisión",
                            actionText = "Historial",
                            onActionClick = { onNavigate(AppNavigation.INSTRUCTOR_REVISION) }
                        )
                    }
                    
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        data.pendingProjects.forEach { project ->
                            ProductivityInboxCard(project, onClick = { onNavigate("instructor_detail/${project.id}") })
                        }
                    }
                }
            }

            // --- QUICK TOOLS ---
            item {
                Column {
                    PaddingRow {
                        Text("Herramientas Rápidas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    }
                    Spacer(Modifier.height(16.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            ToolCard(
                                title = "Directorio",
                                subtitle = "Tus aprendices",
                                icon = Icons.Default.People,
                                color = SenaInfo,
                                onClick = { onNavigate(AppNavigation.INSTRUCTOR_FICHAS) }
                            )
                        }
                        item {
                            ToolCard(
                                title = "Gestión Fichas",
                                subtitle = "Administrar grupos",
                                icon = Icons.AutoMirrored.Filled.Assignment,
                                color = SenaGreen,
                                onClick = { onNavigate(AppNavigation.INSTRUCTOR_MANAGE_FICHAS) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InstructorSummaryMetric(value: String, label: String, color: Color) {
    Column {
        Text(value, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black, color = Color.White)
        Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ProductivityInboxCard(project: PendingProject, onClick: () -> Unit) {
    SenaCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(14.dp),
                color = SenaBorderSoft
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(project.student.take(1), fontWeight = FontWeight.Black, color = SenaText, fontSize = 20.sp)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(project.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = SenaText)
                Text("Aprendiz: ${project.student}", style = MaterialTheme.typography.bodySmall, color = SenaTextLight)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(project.date, style = MaterialTheme.typography.labelSmall, color = SenaTextMuted)
                Spacer(Modifier.height(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = SenaBorder, modifier = Modifier.size(12.dp))
            }
        }
    }
}

@Composable
fun ToolCard(title: String, subtitle: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.width(180.dp).height(100.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, SenaBorderSoft)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
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
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black, color = SenaText)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
            }
        }
    }
}

@Composable
fun PaddingRow(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = modifier.padding(horizontal = 20.dp), content = content)
}

@Preview(showBackground = true)
@Composable
fun InstructorDashboardPreview() {
    ProyecTwinTheme {
        InstructorDashboardScreen(onNavigate = {})
    }
}
