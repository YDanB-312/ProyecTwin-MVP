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
import androidx.compose.ui.text.style.TextAlign
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
                title = "Comparativa Técnica",
                onBack = onBack,
                showProfile = true,
                showNotifications = true
            )
        },
        containerColor = senaColors().background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            // --- HEADER PREMIUM (SIMILARITY) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(senaColors().header, Color(0xFF0F172A))
                        )
                    )
            )

            // --- FLOATING CONTENT ---
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .offset(y = (-80).dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Main Score Card
                SenaCard(elevation = 8.dp) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "RIESGO DE SIMILITUD",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = senaColors().danger,
                            letterSpacing = 1.sp
                        )
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${data.totalPercentage}%",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Black,
                                color = senaColors().danger
                            )
                        }
                        
                        Text(
                            "Coincidencias moderadas detectadas en la base de datos global.",
                            style = MaterialTheme.typography.bodySmall,
                            color = senaColors().textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                SenaSectionHeader(title = "Contraste de Proyectos")
                
                // Side by Side
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProjectMiniCard("TU PROYECTO", data.myProjectName, senaColors().green, Modifier.weight(1f))
                    ProjectMiniCard("COINCIDENCIA", data.otherProjectName, senaColors().warning, Modifier.weight(1f))
                }

                SenaSectionHeader(title = "Desglose por Secciones")
                SenaCard(elevation = 1.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        data.matches.forEach { match ->
                            MatchProgressItem(match.section, match.percentage)
                        }
                    }
                }

                SenaAlertBanner(
                    title = "Acción Recomendada",
                    message = "Considera reformular las secciones con más del 60% de similitud para asegurar la originalidad de tu propuesta.",
                    icon = Icons.Default.Lightbulb,
                    color = senaColors().warning
                )

                SenaButton(
                    text = "REGRESAR A PROYECTOS",
                    onClick = onBack,
                    isPrimary = false,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

@Composable
fun ProjectMiniCard(label: String, title: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(24.dp),
        color = senaColors().backgroundElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, senaColors().borderSoft),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = color)
            Spacer(Modifier.height(8.dp))
            Text(
                title, 
                style = MaterialTheme.typography.bodySmall, 
                fontWeight = FontWeight.Bold, 
                color = senaColors().text,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun MatchProgressItem(title: String, percentage: Int) {
    val color = if (percentage > 70) senaColors().danger else if (percentage > 40) senaColors().warning else senaColors().green
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = senaColors().text)
            Text("$percentage%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Black, color = color)
        }
        LinearProgressIndicator(
            progress = { percentage / 100f },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = color,
            trackColor = senaColors().borderSoft,
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SimilarityDetailScreenPreview() {
    ProyecTwinTheme {
        SimilarityDetailScreen(onBack = {})
    }
}
