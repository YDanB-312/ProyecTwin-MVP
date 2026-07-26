package com.example.proyectwin.ui.screens.admin

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSimilarityDetailScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()
    var selectedComparison by remember { mutableIntStateOf(0) }
    val comparisons = listOf("Plataforma Educativa SENA", "Plataforma de Cursos Online")
    
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
                    text = "Marcar Resuelto", 
                    onClick = { 
                        scope.launch {
                            snackbarHostState.showSnackbar("Caso marcado como resuelto")
                            kotlinx.coroutines.delay(1000)
                            onBack()
                        }
                    }, 
                    modifier = Modifier.weight(1f)
                )
                SenaButton(
                    text = "Desestimar", 
                    onClick = { 
                        scope.launch {
                            snackbarHostState.showSnackbar("Similitud desestimada")
                            kotlinx.coroutines.delay(1000)
                            onBack()
                        }
                    }, 
                    isPrimary = false, 
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
                title = "Análisis de Control",
                subtitle = "Comparativa técnica de alto nivel para la supervisión de integridad académica.",
                icon = Icons.Default.ExclamationTriangle
            )

            // Selector Banner
            SenaCard(containerColor = Color.White, elevation = 2.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Proyecto Bajo Supervisión: Sistema de Gestión Académica",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = SenaText
                    )
                    Text(
                        "Se detectaron ${comparisons.size} casos de similitud. Selecciona uno:",
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
                                focusedBorderColor = SenaGreen,
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

            SenaSectionHeader(title = "Comparativa Directa")
            
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                AdminCompCard("Proyecto Original", "Juan Pérez", SenaGreen, Modifier.fillMaxWidth())
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.CompareArrows, contentDescription = null, tint = SenaTextLight, modifier = Modifier.size(24.dp))
                }
                AdminCompCard("Caso Coincidente", "Ana Martínez", SenaWarning, Modifier.fillMaxWidth())
            }

            SenaSectionHeader(title = "Niveles de Coincidencia")
            SenaCard(elevation = 1.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    AdminMatchItem("Descripción Técnica", 72, SenaDanger)
                    AdminMatchItem("Alcance y Objetivos", 55, SenaWarning)
                    AdminMatchItem("Metodología", 45, SenaWarning)
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun AdminCompCard(label: String, author: String, color: Color, modifier: Modifier = Modifier) {
    SenaCard(elevation = 1.dp, modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(36.dp), shape = CircleShape, color = color.copy(alpha = 0.1f)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.FilePresent, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
                Text("Aprendiz: $author", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = SenaText)
            }
        }
    }
}

@Composable
fun AdminMatchItem(title: String, percentage: Int, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, style = MaterialTheme.typography.bodySmall, color = SenaText)
            Text("$percentage%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Black, color = color)
        }
        LinearProgressIndicator(
            progress = { percentage / 100f },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
            color = color,
            trackColor = SenaBorderSoft
        )
    }
}

// Missing icon
val Icons.Filled.ExclamationTriangle: ImageVector get() = Icons.Default.Warning

@Preview(showBackground = true)
@Composable
fun AdminSimilarityDetailPreview() {
    ProyecTwinTheme {
        AdminSimilarityDetailScreen(onBack = {})
    }
}
