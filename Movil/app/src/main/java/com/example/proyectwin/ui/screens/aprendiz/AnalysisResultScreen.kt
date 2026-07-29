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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisResultScreen(onBack: () -> Unit, onViewDetail: (String) -> Unit) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            SenaTopBar(
                title = "Resultado del An�lisis",
                onBack = onBack,
                showProfile = true,
                showNotifications = true
            )
        },
        containerColor = senaColors().background,
        bottomBar = {
            SenaBottomBar {
                SenaButton(text = "Finalizar", onClick = onBack, isPrimary = false, modifier = Modifier.weight(1f))
                SenaButton(
                    text = "Guardar", 
                    onClick = onBack, 
                    icon = Icons.Default.Save,
                    modifier = Modifier.weight(1f)
                )
            }
        }
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
                title = "An�lisis Completado",
                subtitle = "Revisa el informe de originalidad generado por nuestro sistema.",
                icon = Icons.Default.Verified
            )

            // High Level Summary Card (React style)
            SenaCard(containerColor = senaColors().success.copy(alpha = 0.05f), elevation = 1.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(modifier = Modifier.size(56.dp), shape = CircleShape, color = senaColors().success) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                    }
                    Spacer(Modifier.width(20.dp))
                    Column {
                        Text("An�lisis Exitoso", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = senaColors().success)
                        Text("El sistema no encontr� similitudes cr�ticas en tu propuesta.", style = MaterialTheme.typography.bodySmall, color = senaColors().textSecondary)
                    }
                }
            }

            // Metrics Grid (React cards)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ResultMetricCard("Similitud", "12%", senaColors().success, modifier = Modifier.weight(1f))
                ResultMetricCard("Coincidencias", "3", senaColors().warning, modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ResultMetricCard("Proyectos", "2", senaColors().info, modifier = Modifier.weight(1f))
                ResultMetricCard("Estado", "Aprobado", senaColors().green, modifier = Modifier.weight(1f))
            }

            // Detailed Matches (The "Table" in React)
            SenaSectionHeader(title = "Detalle de Coincidencias")
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                MatchItem("Plataforma Educativa SENA", "65%", "Resumen", onClick = { onViewDetail("1") })
                MatchItem("Sistema de Notas Web", "8%", "Tecnolog�as", onClick = { onViewDetail("2") })
            }

            // Recommendations
            SenaSectionHeader(title = "Recomendaciones IA")
            SenaCard(elevation = 1.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    RecommendationItem("El t�tulo de tu proyecto es original y descriptivo.")
                    RecommendationItem("Las tecnolog�as propuestas son adecuadas.")
                    RecommendationItem("Considera ampliar la secci�n de metodolog�a.")
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun ResultMetricCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = senaColors().backgroundElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = color)
        }
    }
}

@Composable
fun MatchItem(project: String, percentage: String, section: String, onClick: () -> Unit) {
    SenaCard(onClick = onClick, elevation = 0.5.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(project, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = senaColors().text)
                Text("Secci�n: $section", style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
            }
            Surface(
                color = if (percentage.toInt().let { it > 50 }) senaColors().warning.copy(alpha = 0.1f) else senaColors().success.copy(alpha = 0.1f),
                shape = CircleShape
            ) {
                Text(
                    percentage, 
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = if (percentage.toInt().let { it > 50 }) senaColors().warning else senaColors().success
                )
            }
        }
    }
}

@Composable
fun RecommendationItem(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = senaColors().warning, modifier = Modifier.size(16.dp).offset(y = 2.dp))
        Spacer(Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = senaColors().textSecondary)
    }
}

@Preview(showBackground = true)
@Composable
fun AnalysisResultScreenPreview() {
    ProyecTwinTheme {
        AnalysisResultScreen(onBack = {}, onViewDetail = {})
    }
}
