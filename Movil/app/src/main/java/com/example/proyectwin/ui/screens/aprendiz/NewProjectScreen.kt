package com.example.proyectwin.ui.screens.aprendiz

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class NewProjectFormData(
    val title: String = "",
    val summary: String = "",
    val keywords: String = "",
    val technologies: String = "",
    val objectives: String = "",
    val deliverables: String = "",
    val observations: String = "",
    val duration: String = "",
    val startDate: String = "",
    val projectType: String = "aplicacion",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewProjectScreen(onBack: () -> Unit, onSubmit: () -> Unit = {}) {
    var formData by remember { mutableStateOf(NewProjectFormData()) }
    var isSubmitting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            SenaTopBar(
                title = "ProyecTwin",
                onBack = onBack,
                showProfile = false,
                showNotifications = false
            )
        },
        containerColor = senaColors().background,
        bottomBar = {
            SenaBottomBar {
                SenaButton(text = "Cancelar", onClick = onBack, isPrimary = false, modifier = Modifier.weight(1f))
                SenaButton(
                    text = "Guardar Proyecto",
                    onClick = {
                        isSubmitting = true
                        scope.launch {
                            delay(1500)
                            isSubmitting = false
                            onSubmit()
                        }
                    },
                    isLoading = isSubmitting,
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
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            SenaPageHeader(
                title = "Nuevo Proyecto",
                subtitle = "Inicia una idea desde cero y compártela con tus instructores.",
                icon = Icons.Default.AddCircle
            )

            // Step Indicator (Inspirado en la web que es un formulario guiado)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Paso 1 de 4: Información Básica",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = senaColors().green
                )
                LinearProgressIndicator(
                    progress = { 0.25f },
                    modifier = Modifier.width(100.dp).height(6.dp),
                    color = senaColors().green,
                    trackColor = senaColors().borderSoft,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }

            SenaSectionHeader(title = "Información del Proyecto")
            SenaCard {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    SenaTextField(
                        value = formData.title,
                        onValueChange = { formData = formData.copy(title = it) },
                        label = "Título del proyecto *",
                        placeholder = "Ej: Sistema de Gestión IoT"
                    )
                    
                    SenaTextField(
                        value = formData.summary,
                        onValueChange = { formData = formData.copy(summary = it) },
                        label = "Resumen ejecutivo *",
                        placeholder = "Describe brevemente el alcance...",
                        modifier = Modifier.heightIn(min = 120.dp)
                    )
                    
                    SenaTextField(
                        value = formData.keywords,
                        onValueChange = { formData = formData.copy(keywords = it) },
                        label = "Palabras clave (separadas por coma) *",
                        placeholder = "Desarrollo, IoT, Sena"
                    )
                }
            }

            SenaSectionHeader(title = "Objetivos y Entregables")
            SenaCard {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    SenaTextField(
                        value = formData.objectives,
                        onValueChange = { formData = formData.copy(objectives = it) },
                        label = "Objetivos generales *",
                        placeholder = "Define lo que esperas lograr...",
                        modifier = Modifier.heightIn(min = 100.dp)
                    )
                    
                    SenaTextField(
                        value = formData.deliverables,
                        onValueChange = { formData = formData.copy(deliverables = it) },
                        label = "Entregables esperados *",
                        placeholder = "Software, Documentación, etc.",
                        modifier = Modifier.heightIn(min = 100.dp)
                    )
                }
            }

            SenaSectionHeader(title = "Equipo de Trabajo")
            SenaCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Selecciona los integrantes de tu equipo (máx 5)",
                        style = MaterialTheme.typography.labelSmall,
                        color = senaColors().textLight
                    )
                    
                    repeat(3) { index ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = index == 0, 
                                onCheckedChange = {},
                                colors = CheckboxDefaults.colors(checkedColor = senaColors().green)
                            )
                            Text(
                                if (index == 0) "Maria Gonzalez (Tú)" else if (index == 1) "Juan Perez" else "Laura Gomez",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (index == 0) senaColors().text else senaColors().textSecondary
                            )
                        }
                    }
                }
            }

            SenaSectionHeader(title = "Información Técnica")
            SenaCard {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Text("Tipo de proyecto", style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SenaChip(
                            text = "Software", 
                            color = senaColors().green, 
                            isSelected = formData.projectType == "aplicacion",
                            onClick = { formData = formData.copy(projectType = "aplicacion") }
                        )
                        SenaChip(
                            text = "Investigación", 
                            color = senaColors().accent, 
                            isSelected = formData.projectType == "investigacion",
                            onClick = { formData = formData.copy(projectType = "investigacion") }
                        )
                    }
                    
                    SenaTextField(
                        value = formData.duration,
                        onValueChange = { formData = formData.copy(duration = it) },
                        label = "Duración estimada (meses)",
                        placeholder = "6"
                    )
                    
                    SenaTextField(
                        value = formData.observations,
                        onValueChange = { formData = formData.copy(observations = it) },
                        label = "Observaciones",
                        placeholder = "Información adicional relevante...",
                        modifier = Modifier.heightIn(min = 80.dp)
                    )
                }
            }

            Spacer(Modifier.height(100.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NewProjectScreenPreview() {
    ProyecTwinTheme {
        NewProjectScreen(onBack = {})
    }
}
