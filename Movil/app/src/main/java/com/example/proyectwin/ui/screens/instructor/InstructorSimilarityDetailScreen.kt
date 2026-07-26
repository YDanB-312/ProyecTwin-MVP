package com.example.proyectwin.ui.screens.instructor

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*
import kotlinx.coroutines.launch

import com.example.proyectwin.navigation.AppNavigation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructorSimilarityDetailScreen(onBack: () -> Unit, onNavigate: (String) -> Unit) {
    val scrollState = rememberScrollState()
    var selectedComparison by remember { mutableIntStateOf(0) }
    val comparisons = listOf("Plataforma Educativa SENA", "Sistema de Notas Web")
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SenaTopBar(
                title = "ProyecTwin",
                onBack = onBack,
                showProfile = true,
                showNotifications = true
            )
        },
        containerColor = SenaBackground,
        bottomBar = {
            SenaBottomBar {
                SenaButton(
                    text = "Revisado", 
                    onClick = { 
                        scope.launch {
                            snackbarHostState.showSnackbar("Caso marcado como revisado")
                            kotlinx.coroutines.delay(1000)
                            onBack()
                        }
                    }, 
                    modifier = Modifier.weight(1f)
                )
                SenaButton(text = "Reportar", onClick = { onNavigate(AppNavigation.REPORT_ISSUE) }, isPrimary = false, containerColor = SenaDanger, modifier = Modifier.weight(1f))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            SenaPageHeader(
                title = "Análisis del Instructor",
                subtitle = "Herramienta de comparación asistida por IA para la detección de similitudes.",
                icon = Icons.Default.Compare
            )

            // Selector Banner
            SenaCard(containerColor = SenaAccent.copy(alpha = 0.05f), elevation = 0.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Analizando: Sistema de Gestión Académica",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = SenaAccent
                    )
                    Text(
                        "Se detectaron ${comparisons.size} coincidencias. Selecciona una para comparar:",
                        style = MaterialTheme.typography.labelSmall,
                        color = SenaTextLight
                    )
                    
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = comparisons[selectedComparison],
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true).fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = MaterialTheme.typography.bodySmall,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SenaAccent,
                                unfocusedBorderColor = SenaBorder
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            comparisons.forEachIndexed { index, name ->
                                DropdownMenuItem(
                                    text = { Text(name, style = MaterialTheme.typography.bodySmall) },
                                    onClick = {
                                        selectedComparison = index
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            SenaSectionHeader(title = "Comparación Técnica")
            
            // Comparison Grid
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InstructorCompCard("Mi Aprendiz", "Juan Pérez", SenaGreen, Modifier.weight(1f))
                InstructorCompCard("Coincidencia", "Ana Martínez", SenaWarning, Modifier.weight(1f))
            }

            SenaSectionHeader(title = "Puntos de Coincidencia")
            SenaCard {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    MatchMetricItem("Resumen Ejecutivo", 72)
                    MatchMetricItem("Objetivos Generales", 55)
                    MatchMetricItem("Tecnologías", 40)
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun InstructorCompCard(label: String, name: String, color: Color, modifier: Modifier = Modifier) {
    SenaCard(elevation = 1.dp, modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Surface(modifier = Modifier.size(32.dp), shape = CircleShape, color = color.copy(alpha = 0.1f)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(name.take(1), fontWeight = FontWeight.Bold, color = color)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
            Text(name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = SenaText, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun MatchMetricItem(title: String, percentage: Int) {
    val color = if (percentage > 70) SenaDanger else if (percentage > 40) SenaWarning else SenaGreen
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, style = MaterialTheme.typography.bodySmall, color = SenaText)
            Text("$percentage%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = color)
        }
        LinearProgressIndicator(
            progress = { percentage / 100f },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
            color = color,
            trackColor = SenaBorderSoft
        )
    }
}

@Preview(showBackground = true)
@Composable
fun InstructorSimilarityDetailScreenPreview() {
    ProyecTwinTheme {
        InstructorSimilarityDetailScreen(onBack = {}, onNavigate = {})
    }
}
