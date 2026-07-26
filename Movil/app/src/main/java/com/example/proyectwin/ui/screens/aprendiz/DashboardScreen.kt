package com.example.proyectwin.ui.screens.aprendiz

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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
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

data class DashboardProject(
    val id: String,
    val name: String,
    val status: String,
    val date: String,
    val members: Int,
    val instructor: String,
)

data class DashboardData(
    val userName: String = "Maria",
    val date: String = "28 may. 2026",
    val projects: List<DashboardProject> = emptyList(),
    val notificationCount: Int = 5,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(onNavigate: (String) -> Unit) {
    val data = remember {
        DashboardData(
            projects = listOf(
                DashboardProject("1", "Sistema de Gestión Académica", "En revisión", "15 mar 2023", 3, "Carlos Ruiz"),
                DashboardProject("2", "Aplicación Web de Inventarios", "Aprobado", "22 abr 2023", 2, "Ana Gomez"),
                DashboardProject("3", "Plataforma de E-learning", "Borrador", "10 may 2023", 4, "Diego Munoz"),
            )
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "rocket")
    val rocketFloat by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 15f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "rocketFloat"
    )

    Scaffold(
        topBar = {
            SenaTopBar(
                title = "ProyecTwin",
                onNavigateToProfile = { onNavigate(AppNavigation.APRENDIZ_PROFILE) },
                onNavigateToAlerts = { onNavigate(AppNavigation.APRENDIZ_ALERTS) }
            )
        },
        containerColor = SenaBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // --- COMMAND CENTER HEADER ---
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(bottomStart = 48.dp, bottomEnd = 48.dp))
                        .background(Brush.verticalGradient(colors = listOf(SenaHeader, SenaGreen)))
                        .padding(24.dp)
                ) {
                    // Background Rocket Decoration
                    Icon(
                        Icons.Default.RocketLaunch,
                        contentDescription = null,
                        modifier = Modifier
                            .size(240.dp)
                            .align(Alignment.CenterEnd)
                            .offset(x = 60.dp, y = 40.dp)
                            .rotate(-15f + rocketFloat)
                            .scale(1.2f),
                        tint = Color.White.copy(alpha = 0.05f)
                    )

                    Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Center) {
                        Text(
                            "Centro de Comando",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.7f),
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "¡Hola de nuevo, ${data.userName}!",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Spacer(Modifier.height(16.dp))
                        Surface(
                            color = Color.White.copy(alpha = 0.15f),
                            shape = CircleShape,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(data.date, style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // --- QUICK COMMANDS (HORIZONTAL) ---
            item {
                Column {
                    PaddingRow {
                        Text("Comandos Rápidos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    }
                    Spacer(Modifier.height(16.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            PremiumActionCard(
                                title = "Nuevo Proyecto",
                                icon = Icons.Default.AddCircle,
                                color = SenaSuccess,
                                onClick = { onNavigate(AppNavigation.APRENDIZ_NEW_PROJECT) }
                            )
                        }
                        item {
                            PremiumActionCard(
                                title = "Alertas IA",
                                icon = Icons.Default.AutoAwesome,
                                color = SenaInfo,
                                onClick = { onNavigate(AppNavigation.APRENDIZ_ALERTS) }
                            )
                        }
                        item {
                            PremiumActionCard(
                                title = "Mi Ficha",
                                icon = Icons.Default.School,
                                color = SenaWarning,
                                onClick = { onNavigate(AppNavigation.APRENDIZ_FICHA_DETAIL) }
                            )
                        }
                    }
                }
            }

            // --- FEATURED PROJECTS (CAROUSEL-ISH) ---
            item {
                Column {
                    PaddingRow {
                        SenaSectionHeader(
                            title = "Proyectos Activos",
                            actionText = "Ver todos",
                            onActionClick = { onNavigate(AppNavigation.APRENDIZ_PROJECTS) }
                        )
                    }
                    
                    if (data.projects.isEmpty()) {
                        PaddingRow {
                            SenaEmptyState(
                                message = "No hay misiones activas. Inicia una nueva propuesta ahora.",
                                icon = Icons.Default.Explore
                            )
                        }
                    } else {
                        data.projects.forEach { project ->
                            PaddingRow(Modifier.padding(bottom = 16.dp)) {
                                DashboardProjectCard(project, onClick = { onNavigate("aprendiz_detail/${project.id}") })
                            }
                        }
                    }
                }
            }

            // --- ANALYTICS PREVIEW ---
            item {
                PaddingRow {
                    SenaCard(containerColor = Color(0xFF0F172A)) { // Slate 900
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Estado de Originalidad", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
                                Spacer(Modifier.height(4.dp))
                                Text("Nivel Óptimo", color = SenaSuccess, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                                Spacer(Modifier.height(8.dp))
                                Text("Tus proyectos mantienen un promedio del 92% de originalidad detectada por IA.", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
                            }
                            Spacer(Modifier.width(16.dp))
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = { 0.92f },
                                    modifier = Modifier.size(64.dp),
                                    color = SenaSuccess,
                                    strokeWidth = 6.dp,
                                    trackColor = Color.White.copy(alpha = 0.1f),
                                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                                )
                                Text("92%", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumActionCard(title: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(140.dp),
        shape = RoundedCornerShape(32.dp),
        color = Color.White,
        shadowElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, SenaBorderSoft)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(14.dp),
                color = color.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
                }
            }
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black, color = SenaText, lineHeight = 18.sp)
        }
    }
}

@Composable
fun DashboardProjectCard(project: DashboardProject, onClick: () -> Unit) {
    SenaCard(onClick = onClick, elevation = 2.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                SenaStatusBadge(status = project.status)
                Spacer(Modifier.height(12.dp))
                Text(project.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = SenaText)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, contentDescription = null, tint = SenaTextLight, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Actualizado: ${project.date}", style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SenaBorder)
        }
    }
}

@Composable
fun PaddingRow(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = modifier.padding(horizontal = 20.dp), content = content)
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    ProyecTwinTheme {
        DashboardScreen(onNavigate = {})
    }
}
