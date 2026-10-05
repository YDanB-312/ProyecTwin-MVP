package com.example.proyectwin.ui.screens.aprendiz

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectwin.data.model.Similarity
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimilarityDetailScreen(onBack: () -> Unit, projectId: String = "") {
    val scrollState = rememberScrollState()

    val sim = Similarity(
        id = projectId.toIntOrNull() ?: 1,
        projectId1 = 1,
        projectId2 = 2,
        project1Title = "Sistema de Gestión IoT",
        project2Title = "Monitoreo Agrícola Automatizado",
        project1Student = "Maria Gonzalez",
        project2Student = "Juan Perez",
        similitud = 0.45,
        estado = "PENDIENTE"
    )

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

            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .offset(y = (-80).dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                SimilarityScoreCard(sim)

                SenaSectionHeader(title = "Contraste de Proyectos")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProjectMiniCard("ORIGEN", sim.project1Title ?: "Proyecto 1", senaColors().green, Modifier.weight(1f))
                    ProjectMiniCard("COINCIDENCIA", sim.project2Title ?: "Proyecto 2", senaColors().warning, Modifier.weight(1f))
                }

                SenaSectionHeader(title = "Registro de Similitud")
                SenaCard(elevation = 1.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "${sim.project1Title ?: "Proyecto 1"} vs ${sim.project2Title ?: "Proyecto 2"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = senaColors().text
                                )
                                Text(
                                    "Estado: ${sim.statusDisplay}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = senaColors().textLight
                                )
                            }
                            Surface(
                                color = if (sim.similitud > 0.5) senaColors().warning.copy(alpha = 0.1f) else senaColors().success.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    sim.similitudPercent,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black,
                                    color = if (sim.similitud > 0.5) senaColors().warning else senaColors().success
                                )
                            }
                        }
                    }
                }

                SenaAlertBanner(
                    title = "Acción Recomendada",
                    message = "Considera reformular las secciones con alto nivel de coincidencia para asegurar la originalidad de tu propuesta.",
                    icon = Icons.Default.Lightbulb,
                    color = senaColors().warning
                )

                SenaButton(
                    text = "Regresar a Proyectos",
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
private fun SimilarityScoreCard(sim: Similarity) {
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
                color = if (sim.similitud > 0.5) senaColors().danger else senaColors().success,
                letterSpacing = 1.sp
            )

            Text(
                text = sim.similitudPercent,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = if (sim.similitud > 0.5) senaColors().danger else senaColors().success
            )

            Text(
                if (sim.similitud > 0.5) "Coincidencias moderadas detectadas en la base de datos global." else "Nivel de similitud bajo. Tu propuesta es original.",
                style = MaterialTheme.typography.bodySmall,
                color = senaColors().textSecondary,
                textAlign = TextAlign.Center
            )

            Surface(
                color = senaColors().info.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "Estado: ${sim.statusDisplay}",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = senaColors().info
                )
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

@Preview(showBackground = true)
@Composable
fun SimilarityDetailScreenPreview() {
    ProyecTwinTheme {
        SimilarityDetailScreen(onBack = {}, projectId = "1")
    }
}
