package com.example.proyectwin.ui.screens.instructor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectwin.data.mock.MockDataProvider
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*
import com.example.proyectwin.ui.viewmodel.AuthViewModel
import com.example.proyectwin.ui.viewmodel.FichasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearFichaScreen(
    onBack: () -> Unit,
    onFichaCreated: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    fichasViewModel: FichasViewModel = viewModel()
) {
    val authState by authViewModel.uiState.collectAsState()
    val user = authState.user

    var nombre by remember { mutableStateOf("") }
    var programa by remember { mutableStateOf("Análisis y Desarrollo de Software") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var successMsg by remember { mutableStateOf(false) }
    val programas = listOf(
        "Análisis y Desarrollo de Software",
        "Desarrollo Web",
        "Machine Learning",
        "Seguridad Informática",
        "Data Science",
        "Inteligencia Artificial",
        "DevOps",
        "Cloud Computing",
        "Ciberseguridad",
        "IoT",
        "Big Data",
        "Realidad Virtual"
    )

    LaunchedEffect(Unit) {
        fichasViewModel.generarCodigo()
    }

    val codigo = fichasViewModel.uiState.value.codigoGenerado

    Scaffold(
        topBar = {
            SenaTopBar(
                title = "ProyecTwin",
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SenaPageHeader(
                title = "Crear Nueva Ficha",
                subtitle = "Genera un nuevo grupo de formación con código único.",
                icon = Icons.Default.AddCircle
            )

            if (codigo != null) {
                SenaCard(elevation = 2.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Código de Ficha",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = senaColors().textSecondary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                color = senaColors().background
                            ) {
                                Text(
                                    codigo,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = senaColors().green
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            SenaCopyButton(textToCopy = codigo, label = "Copiar")
                        }
                    }
                }
            }

            SenaCard {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "Información de la Ficha",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = senaColors().text
                    )

                    SenaTextField(
                        value = nombre,
                        onValueChange = { nombre = it; errorMsg = null },
                        label = "Nombre de la ficha",
                        placeholder = "Ej: Ficha ADSO 2024"
                    )

                    Text(
                        "Programa de formación",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = senaColors().textSecondary
                    )
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = programa,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true).fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = senaColors().green,
                                unfocusedBorderColor = senaColors().border,
                                focusedContainerColor = senaColors().backgroundElevated,
                                unfocusedContainerColor = senaColors().backgroundElevated
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            programas.forEach { p ->
                                DropdownMenuItem(
                                    text = { Text(p) },
                                    onClick = { programa = p; expanded = false }
                                )
                            }
                        }
                    }
                }
            }

            if (errorMsg != null) {
                Surface(
                    color = senaColors().danger.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        errorMsg!!,
                        modifier = Modifier.padding(16.dp),
                        color = senaColors().danger,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (successMsg) {
                Surface(
                    color = senaColors().success.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Ficha creada exitosamente",
                        modifier = Modifier.padding(16.dp),
                        color = senaColors().success,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SenaButton(
                    text = "Cancelar",
                    onClick = onBack,
                    isPrimary = false,
                    modifier = Modifier.weight(1f)
                )
                SenaButton(
                    text = "Crear Ficha",
                    onClick = {
                        if (nombre.isBlank()) {
                            errorMsg = "El nombre de la ficha es obligatorio"
                        } else if (codigo == null) {
                            errorMsg = "Error al generar el código"
                        } else {
                            MockDataProvider.createFicha(
                                codigo = codigo,
                                programa = programa,
                                instructorName = user?.name ?: "Instructor"
                            )
                            successMsg = true
                            onFichaCreated()
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}
