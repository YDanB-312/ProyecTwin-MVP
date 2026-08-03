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
import com.example.proyectwin.data.mock.MockDataProvider
import com.example.proyectwin.data.model.Similarity
import com.example.proyectwin.data.model.SimilarityStatus
import com.example.proyectwin.navigation.AppNavigation
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructorSimilarityDetailScreen(onBack: () -> Unit, onNavigate: (String) -> Unit, similarityId: String = "") {
    val scrollState = rememberScrollState()
    var selectedComparison by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var similarity by remember(similarityId) {
        mutableStateOf(
            MockDataProvider.getAllSimilarities().find { it.id == (similarityId.toIntOrNull() ?: 0) }
        )
    }
    val comparisons = listOfNotNull(similarity?.project1Title, similarity?.project2Title)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SenaTopBar(
                title = "Análisis IA",
                onBack = onBack,
                showProfile = true,
                showNotifications = true
            )
        },
        containerColor = senaColors().background,
        bottomBar = {
            SenaBottomBar {
                SenaButton(
                    text = "REVISADO", 
                    onClick = { 
                        similarity?.let { s ->
                            MockDataProvider.updateSimilarityEstado(s.id, SimilarityStatus.REVISADO.value)
                            similarity = s.copy(estado = SimilarityStatus.REVISADO.value)
                        }
                        scope.launch {
                            snackbarHostState.showSnackbar("Caso marcado como revisado")
                            kotlinx.coroutines.delay(1000)
                            onBack()
                        }
                    }, 
                    modifier = Modifier.weight(1f)
                )
                SenaButton(
                    text = "REPORTAR", 
                    onClick = { onNavigate(AppNavigation.REPORT_ISSUE) }, 
                    isPrimary = false, 
                    containerColor = senaColors().danger, 
                    modifier = Modifier.weight(1f)
                )
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
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            SenaPageHeader(
                title = "Comparación de Propuestas",
                subtitle = "Herramienta asistida por IA para la validación de originalidad técnica.",
                icon = Icons.Default.Compare
            )

            // Selector Premium
            SenaCard(elevation = 2.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "PROPUESTA ANALIZADA",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = senaColors().green,
                        letterSpacing = 1.sp
                    )
                    Text(
                        "${sim.project1Title ?: "Propuesta"} — ${sim.project1Student ?: "Aprendiz"}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = senaColors().text
                    )
                    
                    HorizontalDivider(color = senaColors().borderSoft)
                    
                    Text(
                        "COMPARA CON COINCIDENCIA:",
                        style = MaterialTheme.typography.labelSmall,
                        color = senaColors().textLight,
                        fontWeight = FontWeight.Bold
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
                            shape = RoundedCornerShape(16.dp),
                            textStyle = MaterialTheme.typography.bodyMedium,
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
                                    text = { Text(name, style = MaterialTheme.typography.bodyMedium) },
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

            SenaSectionHeader(title = "Contraste Técnico")
            
            // Grid de Comparación
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InstructorCompCard("Tu Aprendiz", sim.project1Student ?: "Sin asignar", senaColors().green, Modifier.weight(1f))
                InstructorCompCard("Preexistente", sim.project2Student ?: "Sin asignar", senaColors().warning, Modifier.weight(1f))
            }

            SenaSectionHeader(title = "Métricas de Similitud")
            SenaCard {
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    MatchMetricItem("Similitud Global", (sim.similitud * 100).toInt())
                    MatchMetricItem("Objetivos Generales", maxOf((sim.similitud * 100).toInt() - 20, 0))
                    MatchMetricItem("Stack Tecnológico", maxOf((sim.similitud * 100).toInt() - 45, 0))
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
fun InstructorCompCard(label: String, name: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = senaColors().backgroundElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, senaColors().borderSoft),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = color.copy(alpha = 0.1f)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(name.take(1), fontWeight = FontWeight.Black, color = color)
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = senaColors().textLight, fontWeight = FontWeight.Bold)
            Text(name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Black, color = senaColors().text, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun MatchMetricItem(title: String, percentage: Int) {
    val color = if (percentage > 70) senaColors().danger else if (percentage > 40) senaColors().warning else senaColors().green
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = senaColors().text)
            Text("$percentage%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Black, color = color)
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
fun InstructorSimilarityDetailScreenPreview() {
    ProyecTwinTheme {
        InstructorSimilarityDetailScreen(onBack = {}, onNavigate = {})
    }
}
