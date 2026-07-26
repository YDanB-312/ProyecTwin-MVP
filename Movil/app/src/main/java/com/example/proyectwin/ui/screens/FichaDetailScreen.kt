package com.example.proyectwin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectwin.navigation.AppNavigation
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*

data class FichaMemberItem(
    val initials: String,
    val name: String,
    val status: String,
)

data class FichaDetailData(
    val nombre: String = "Análisis y Desarrollo de Software",
    val codigo: String = "2568421",
    val centro: String = "Centro de Biotecnología Industrial",
    val instructor: String = "Carlos Ruiz",
    val members: List<FichaMemberItem> = listOf(
        FichaMemberItem("AM", "Ana Martínez", "Activo"),
        FichaMemberItem("JG", "Juan García", "Activo"),
        FichaMemberItem("LG", "Laura Gómez", "Activo"),
        FichaMemberItem("CP", "Carlos Pérez", "Inactivo"),
        FichaMemberItem("MR", "Maria Rodriguez", "Activo"),
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FichaDetailScreen(onBack: () -> Unit, onNavigate: (String) -> Unit) {
    val ficha = remember { FichaDetailData() }

    Scaffold(
        topBar = {
            SenaTopBar(
                title = "ProyecTwin",
                onBack = onBack,
                showProfile = true,
                showNotifications = true
            )
        },
        containerColor = SenaBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                SenaPageHeader(
                    title = "Mi Ficha",
                    subtitle = "Detalles del programa de formación y compañeros de equipo.",
                    icon = Icons.Default.Groups
                )
            }

            // Ficha Info Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Brush.linearGradient(colors = listOf(SenaHeader, SenaGreen)))
                        .padding(24.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    ficha.nombre,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    "Código: ${ficha.codigo}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                            Surface(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = CircleShape
                            ) {
                                Text(
                                    "ACTIVA",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        
                        HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(ficha.centro, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.9f))
                        }
                    }
                }
            }

            item {
                SenaSectionHeader(title = "Instructor Encargado")
                SenaCard(elevation = 1.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            color = SenaGreen.copy(alpha = 0.1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.School, contentDescription = null, tint = SenaGreen, modifier = Modifier.size(24.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(ficha.instructor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SenaText)
                            Text("Líder de Ficha • ADSO", style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
                        }
                    }
                }
            }

            item {
                SenaSectionHeader(
                    title = "Compañeros de Ficha",
                    actionText = "Ver todos",
                    onActionClick = { onNavigate(AppNavigation.INSTRUCTOR_FICHA_DETAIL) }
                )
            }

            items(ficha.members) { member ->
                SenaCard(elevation = 0.5.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = if (member.status == "Activo") SenaGreen.copy(alpha = 0.1f) else SenaBorderSoft
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    member.initials, 
                                    fontWeight = FontWeight.Bold, 
                                    color = if (member.status == "Activo") SenaGreen else SenaTextMuted
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(member.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = SenaText)
                            Text("Aprendiz", style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
                        }
                        SenaStatusBadge(status = member.status)
                    }
                }
            }
            
            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FichaDetailScreenPreview() {
    ProyecTwinTheme {
        FichaDetailScreen(onBack = {}, onNavigate = {})
    }
}
