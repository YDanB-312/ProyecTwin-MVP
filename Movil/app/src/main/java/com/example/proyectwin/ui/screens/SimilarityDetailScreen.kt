package com.example.proyectwin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

@Composable
fun SimilarityDetailScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            SenaTopBar(title = "Análisis de Similitud", showProfile = false, showNotifications = false)
        },
        containerColor = SenaBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            IconButton(onClick = onBack) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = SenaGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Regresar a Notificaciones", color = SenaGreen, style = MaterialTheme.typography.labelLarge)
                }
            }

            // High Similitud Alert
            SenaCard(containerColor = SenaDanger.copy(alpha = 0.05f), elevation = 0.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(56.dp).background(SenaDanger, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("65%", color = Color.White, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Similitud Crítica Detectada", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = SenaDanger)
                        Text("Comparación automática del sistema", style = MaterialTheme.typography.bodySmall, color = SenaTextSecondary)
                    }
                }
            }

            SenaSectionHeader(title = "Comparación de Proyectos")
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ComparisonCard(
                    title = "Proyecto 1",
                    name = "Sistema de Gestión Académica",
                    author = "Juan Perez",
                    color = SenaGreen,
                    modifier = Modifier.weight(1f)
                )
                ComparisonCard(
                    title = "Proyecto 2",
                    name = "Plataforma Educativa SENA",
                    author = "Ana Martinez",
                    color = SenaWarning,
                    modifier = Modifier.weight(1f)
                )
            }

            Icon(
                Icons.AutoMirrored.Filled.CompareArrows,
                contentDescription = null,
                tint = SenaTextLight,
                modifier = Modifier.align(Alignment.CenterHorizontally).size(32.dp)
            )

            SenaSectionHeader(title = "Secciones Coincidentes")
            SenaCard {
                MatchRowV2("Descripción del proyecto", 0.72f)
                MatchRowV2("Tecnologías propuestas", 0.60f)
                MatchRowV2("Objetivos generales", 0.55f)
                MatchRowV2("Metodología", 0.45f)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun ComparisonCard(title: String, name: String, author: String, color: Color, modifier: Modifier = Modifier) {
    SenaCard(elevation = 2.dp, modifier = modifier) {
        Column {
            Text(title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(8.dp))
            Text(name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = SenaText, maxLines = 2)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Autor: $author", style = MaterialTheme.typography.labelSmall, color = SenaTextSecondary)
        }
    }
}

@Composable
fun MatchRowV2(label: String, progress: Float) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = SenaText)
            Text("${(progress * 100).toInt()}%", fontWeight = FontWeight.Bold, color = SenaDanger)
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
            color = SenaDanger,
            trackColor = SenaDanger.copy(alpha = 0.1f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SimilarityDetailScreenPreview() {
    ProyecTwinTheme {
        SimilarityDetailScreen {}
    }
}
