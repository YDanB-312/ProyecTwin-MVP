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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectwin.data.mock.MockDataProvider
import com.example.proyectwin.data.model.Similarity
import com.example.proyectwin.data.model.SimilarityStatus
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSimilarityDetailScreen(onBack: () -> Unit, similarityId: String = "") {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var similarity by remember(similarityId) {
        mutableStateOf(
            MockDataProvider.getAllSimilarities().find { it.id == (similarityId.toIntOrNull() ?: 0) }
        )
    }
    var selectedComparison by remember { mutableIntStateOf(0) }

    val comparisons = listOfNotNull(
        similarity?.project1Title,
        similarity?.project2Title
    )

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
        containerColor = senaColors().background,
        bottomBar = {
            if (similarity != null) {
                SenaBottomBar {
                    SenaButton(
                        text = "Marcar Resuelto",
                        onClick = {
                            similarity?.let { s ->
                                MockDataProvider.updateSimilarityEstado(s.id, SimilarityStatus.CONFIRMADO.value)
                                similarity = s.copy(estado = SimilarityStatus.CONFIRMADO.value)
                            }
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
                            similarity?.let { s ->
                                MockDataProvider.updateSimilarityEstado(s.id, SimilarityStatus.RECHAZADO.value)
                                similarity = s.copy(estado = SimilarityStatus.RECHAZADO.value)
                            }
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
        }
    ) { paddingValues ->
        if (similarity == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                SenaEmptyState(
                    message = "No se encontraron similitudes para revisar.",
                    icon = Icons.Default.SearchOff
                )
            }
            return@Scaffold
        }

        val sim = similarity!!

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SenaPageHeader(
                title = "An\u00e1lisis de Control",
                subtitle = "Comparativa t\u00e9cnica de alto nivel para la supervisi\u00f3n de integridad acad\u00e9mica.",
                icon = Icons.Default.ExclamationTriangle
            )

            // Selector Banner
            SenaCard(elevation = 2.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Proyecto Bajo Supervisi\u00f3n: ${sim.project1Title ?: "Proyecto ${sim.projectId1}"}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = senaColors().text
                    )
                    Text(
                        "Se detectaron ${comparisons.size} casos de similitud. Selecciona uno:",
                        style = MaterialTheme.typography.labelSmall,
                        color = senaColors().textLight
                    )

                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = comparisons.getOrNull(selectedComparison) ?: "",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true).fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = MaterialTheme.typography.bodySmall,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = senaColors().green,
                                unfocusedBorderColor = senaColors().border
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
                AdminCompCard("Proyecto Original", sim.project1Student ?: "Sin asignar", senaColors().green, Modifier.fillMaxWidth())
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.CompareArrows, contentDescription = null, tint = senaColors().textLight, modifier = Modifier.size(24.dp))
                }
                AdminCompCard("Caso Coincidente", sim.project2Student ?: "Sin asignar", senaColors().warning, Modifier.fillMaxWidth())
            }

            SenaSectionHeader(title = "Niveles de Coincidencia")
            SenaCard(elevation = 1.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    AdminMatchItem("Similitud Global", (sim.similitud * 100).toInt(), senaColors().danger)
                    AdminMatchItem("Alcance y Objetivos", minOf((sim.similitud * 100).toInt() - 15, 100).coerceAtLeast(0), senaColors().warning)
                    AdminMatchItem("Metodolog\u00eda", maxOf((sim.similitud * 100).toInt() - 30, 0), senaColors().warning)
                }
            }

            SenaCard(elevation = 1.dp) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Estado del caso", style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
                    SenaStatusBadge(status = sim.statusDisplay)
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
                Text(label, style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
                Text("Aprendiz: $author", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = senaColors().text)
            }
        }
    }
}

@Composable
fun AdminMatchItem(title: String, percentage: Int, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, style = MaterialTheme.typography.bodySmall, color = senaColors().text)
            Text("$percentage%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Black, color = color)
        }
        LinearProgressIndicator(
            progress = { percentage / 100f },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
            color = color,
            trackColor = senaColors().borderSoft
        )
    }
}

// Missing icon
val Icons.Filled.ExclamationTriangle: ImageVector get() = Icons.Default.Warning

@Preview(showBackground = true)
@Composable
fun AdminSimilarityDetailPreview() {
    ProyecTwinTheme {
        AdminSimilarityDetailScreen(similarityId = "1", onBack = {})
    }
}
