package com.example.proyectwin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectwin.ui.components.SenaCard
import com.example.proyectwin.ui.components.SenaTopBar
import com.example.proyectwin.ui.theme.*

@Composable
fun SimilarityDetailScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            SenaTopBar(title = "Detalle de Similitud", showProfile = false, showNotifications = false)
        },
        containerColor = SenaBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header with Back
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
                Text(
                    "Comparación de Proyectos",
                    style = MaterialTheme.typography.titleLarge,
                    color = SenaText
                )
            }

            // Summary Card
            SenaCard(containerColor = SenaDanger.copy(alpha = 0.05f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Detectado: 25/04/2026", style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
                        Text(
                            "Similitud: 65%",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = SenaDanger
                        )
                    }
                    Surface(
                        color = SenaDanger,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Urgente",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Comparison Grid
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ComparisonProjectCard(
                    title = "Proyecto 1",
                    name = "Sistema de Gestión Académica",
                    author = "Juan Perez",
                    headerColor = SenaGreen,
                    modifier = Modifier.weight(1f)
                )
                ComparisonProjectCard(
                    title = "Proyecto 2",
                    name = "Plataforma Educativa SENA",
                    author = "Ana Martinez",
                    headerColor = SenaWarning,
                    modifier = Modifier.weight(1f)
                )
            }

            // Matches List
            Text(
                "Secciones Coincidentes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SenaText
            )
            SenaCard {
                MatchRow("Descripción del proyecto", "72%")
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SenaBorder)
                MatchRow("Tecnologías propuestas", "60%")
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SenaBorder)
                MatchRow("Objetivos generales", "55%")
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SenaBorder)
                MatchRow("Metodología", "45%")
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun ComparisonProjectCard(
    title: String,
    name: String,
    author: String,
    headerColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerColor)
                    .padding(8.dp)
            ) {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(name, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = SenaText, maxLines = 2)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Autor:", style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
                Text(author, style = MaterialTheme.typography.labelSmall, color = SenaText)
            }
        }
    }
}

@Composable
fun MatchRow(section: String, percentage: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(section, style = MaterialTheme.typography.bodyMedium, color = SenaText, modifier = Modifier.weight(1f))
        Text(percentage, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SenaDanger)
    }
}

@Preview(showBackground = true)
@Composable
fun SimilarityDetailScreenPreview() {
    ProyecTwinTheme {
        SimilarityDetailScreen(onBack = {})
    }
}
