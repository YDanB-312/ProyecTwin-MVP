package com.example.proyectwin.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.navigation.AppNavigation
import com.example.proyectwin.ui.theme.*
import kotlinx.coroutines.launch

data class AdminDashboardData(
    val userName: String = "Administrador",
    val date: String = "16 jun. 2026",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(onNavigate: (String) -> Unit) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SenaTopBar(
                title = "ProyecTwin Master",
                onNavigateToProfile = { onNavigate(AppNavigation.ADMIN_PROFILE) },
                onNavigateToAlerts = { onNavigate(AppNavigation.ADMIN_ALERTS) }
            )
        },
        containerColor = SenaBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // --- ADMIN SYSTEM STATUS HEADER ---
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(bottomStart = 48.dp, bottomEnd = 48.dp))
                        .background(Brush.verticalGradient(colors = listOf(Color(0xFF022C22), Color(0xFF064E3B)))) // Deepest Greens
                        .padding(24.dp)
                ) {
                    Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Center) {
                        Text(
                            "Panel de Control Global",
                            style = MaterialTheme.typography.labelMedium,
                            color = SenaSuccess,
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Sistema Operativo",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Spacer(Modifier.height(20.dp))
                        
                        // Analytics Row
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            SystemStatusPill("Online", SenaSuccess, Icons.Default.CloudDone, modifier = Modifier.weight(1f))
                            SystemStatusPill("Safe", SenaInfo, Icons.Default.Shield, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // --- REAL-TIME METRICS ---
            item {
                Column {
                    PaddingRow {
                        Text("Métricas en Tiempo Real", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    }
                    Spacer(Modifier.height(16.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item { AdminMetricSquare("Usuarios", "1,240", Icons.Default.Groups, SenaInfo) }
                        item { AdminMetricSquare("Proyectos", "382", Icons.Default.FolderSpecial, SenaSuccess) }
                        item { AdminMetricSquare("Alertas", "12", Icons.Default.CrisisAlert, SenaWarning) }
                    }
                }
            }

            // --- CORE MANAGEMENT ACTIONS ---
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SenaSectionHeader(title = "Gestión Principal")
                    
                    ManagementActionCard(
                        title = "Usuarios & Roles",
                        desc = "Administrar permisos de acceso",
                        icon = Icons.Default.ManageAccounts,
                        color = SenaInfo,
                        onClick = { onNavigate(AppNavigation.ADMIN_USERS) }
                    )
                    ManagementActionCard(
                        title = "Similitudes Globales",
                        desc = "Auditoría de originalidad",
                        icon = Icons.Default.Plagiarism,
                        color = SenaWarning,
                        onClick = { onNavigate(AppNavigation.ADMIN_SIMILARITY_LIST) }
                    )
                    ManagementActionCard(
                        title = "Reportes Técnicos",
                        desc = "Mantenimiento de plataforma",
                        icon = Icons.Default.BugReport,
                        color = SenaDanger,
                        onClick = { onNavigate(AppNavigation.ADMIN_BUGS) }
                    )
                }
            }

            // --- SYSTEM CONFIG ---
            item {
                PaddingRow {
                    SenaCard(containerColor = Color.White, elevation = 2.dp) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = SenaText.copy(alpha = 0.05f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Settings, contentDescription = null, tint = SenaText)
                                }
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Ajustes del Sistema", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                Text("Configuración global de ProyecTwin", style = MaterialTheme.typography.bodySmall, color = SenaTextLight)
                            }
                            SenaButton(text = "Abrir", onClick = { 
                                scope.launch { snackbarHostState.showSnackbar("Acceso restringido") }
                            }, modifier = Modifier.width(80.dp).height(36.dp), isPrimary = false)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SystemStatusPill(label: String, color: Color, icon: ImageVector, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = color)
        }
    }
}

@Composable
fun AdminMetricSquare(label: String, value: String, icon: ImageVector, color: Color) {
    SenaCard(modifier = Modifier.width(160.dp), elevation = 4.dp) {
        Column {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(16.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = SenaText)
            Text(label, style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
        }
    }
}

@Composable
fun ManagementActionCard(title: String, desc: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, SenaBorderSoft)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(14.dp),
                color = color.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black, color = SenaText)
                Text(desc, style = MaterialTheme.typography.bodySmall, color = SenaTextLight)
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
fun AdminDashboardPreview() {
    ProyecTwinTheme {
        AdminDashboardScreen(onNavigate = {})
    }
}
