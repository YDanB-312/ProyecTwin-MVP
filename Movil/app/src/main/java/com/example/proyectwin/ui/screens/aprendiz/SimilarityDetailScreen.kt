package com.example.proyectwin.ui.screens.aprendiz

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*

data class SimilarityMatch(val section: String, val percentage: Int)

data class SimilarityDetailData(
    val myProjectName: String = "Sistema de Gestión Académica",
    val otherProjectName: String = "Plataforma Educativa SENA",
    val otherAuthor: String = "Ana Martínez",
    val otherProgram: String = "ADSO",
    val otherDate: String = "02/02/2026",
    val totalPercentage: Int = 65,
    val matches: List<SimilarityMatch> = listOf(
        SimilarityMatch("Descripción del proyecto", 72),
        SimilarityMatch("Tecnologías propuestas", 60),
        SimilarityMatch("Objetivos generales", 55),
        SimilarityMatch("Metodología", 45)
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimilarityDetailScreen(onBack: () -> Unit) {
    val data = remember { SimilarityDetailData() }
    val scrollState = rememberScrollState()

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SenaPageHeader(
                title = "Detalle de Similitud",
                subtitle = "Comparativa automática entre proyectos registrados.",
                icon = Icons.Default.Compare
            )

            // Similarity Banner (Fidelity to frontend)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(colors = listOf(SenaHeader, SenaGreen)))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Tu proyecto ${data.myProjectName} tiene coincidencias detectadas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 18.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Similitud General: ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        Text(
                            "${data.totalPercentage}%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }

            SenaSectionHeader(title = "Comparación de Proyectos")
            
            // Side by Side Comparison
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // My Project
                SenaCard(elevation = 1.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FilePresent, contentDescription = null, tint = SenaGreen, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Mi Proyecto", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SenaGreen)
                        }
                        Text(data.myProjectName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SenaText)
                        Text("Aprendiz: Maria Gonzalez • ADSO", style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
                    }
                }

                // Center Icon
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = CircleShape,
                        color = SenaBorderSoft,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SenaBorder)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.AutoMirrored.Filled.CompareArrows, contentDescription = null, tint = SenaTextLight, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Other Project
                SenaCard(elevation = 1.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FilePresent, contentDescription = null, tint = SenaWarning, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Proyecto Coincidente", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SenaWarning)
                        }
                        Text(data.otherProjectName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SenaText)
                        Text("Aprendiz: ${data.otherAuthor} • ${data.otherProgram}", style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
                    }
                }
            }

            SenaSectionHeader(title = "Secciones Coincidentes")
            SenaCard(elevation = 1.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    data.matches.forEachIndexed { index, match ->
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(match.section, style = MaterialTheme.typography.bodySmall, color = SenaText)
                                Text("${match.percentage}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = if (match.percentage > 70) SenaDanger else if (match.percentage > 40) SenaWarning else SenaSuccess)
                            }
                            LinearProgressIndicator(
                                progress = { match.percentage / 100f },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                color = if (match.percentage > 70) SenaDanger else if (match.percentage > 40) SenaWarning else SenaGreen,
                                trackColor = SenaBorderSoft
                            )
                        }
                        if (index < data.matches.size - 1) {
                            HorizontalDivider(color = SenaBorderSoft, modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SimilarityDetailScreenPreview() {
    ProyecTwinTheme {
        SimilarityDetailScreen(onBack = {})
    }
}
