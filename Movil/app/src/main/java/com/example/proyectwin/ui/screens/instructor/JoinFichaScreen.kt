package com.example.proyectwin.ui.screens.instructor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*
import com.example.proyectwin.ui.viewmodel.AuthViewModel
import com.example.proyectwin.ui.viewmodel.FichasActionState
import com.example.proyectwin.ui.viewmodel.FichasViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinFichaScreen(
    onBack: () -> Unit,
    onFichaCreated: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    fichasViewModel: FichasViewModel = viewModel()
) {
    var nombre by remember { mutableStateOf("") }
    var programa by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val actionState by fichasViewModel.actionState.collectAsState()

    Scaffold(
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
            SenaBottomBar {
                SenaButton(text = "Cancelar", onClick = onBack, isPrimary = false, modifier = Modifier.weight(1f))
                SenaButton(
                    text = "Crear Ficha", 
                    onClick = {
                        fichasViewModel.generarCodigo()
                    }, 
                    icon = Icons.Default.Add,
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
                title = "Crear Nueva Ficha",
                subtitle = "Registra un nuevo grupo de formación para comenzar a gestionar sus proyectos.",
                icon = Icons.Default.AddCircle
            )

            SenaSectionHeader(title = "Información del Grupo")
            SenaCard(elevation = 1.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    if (actionState.codigoGenerado != null) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Código de Ficha Generado",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = senaColors().green
                            )
                            Surface(
                                color = senaColors().green.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        actionState.codigoGenerado!!,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Black,
                                        color = senaColors().green,
                                        letterSpacing = 2.sp
                                    )
                                    SenaCopyButton(textToCopy = actionState.codigoGenerado!!)
                                }
                            }
                            Text(
                                "Formato: FT-XXXXXX (código único para compartir con aprendices)",
                                style = MaterialTheme.typography.labelSmall,
                                color = senaColors().textLight
                            )
                        }
                    } else {
                        SenaTextField(
                            value = "",
                            onValueChange = { },
                            label = "Código de Ficha *",
                            placeholder = "Se generará automáticamente",
                            leadingIcon = Icons.Default.Numbers,
                            enabled = false
                        )
                    }

                    SenaTextField(
                        value = nombre, 
                        onValueChange = { nombre = it }, 
                        label = "Nombre descriptivo *",
                        placeholder = "Ej: Análisis y Desarrollo 2026",
                        leadingIcon = Icons.Default.Badge
                    )

                    Column {
                        Text("Programa de Formación *", style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
                        Spacer(Modifier.height(8.dp))
                        var expanded by remember { mutableStateOf(false) }
                        val programs = listOf("ADSO - Análisis y Desarrollo de Sistemas", "Multimedia", "IoT", "Infraestructura")
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = it }
                        ) {
                            OutlinedTextField(
                                value = if (programa.isEmpty()) "Selecciona un programa" else programa,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true).fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = MaterialTheme.typography.bodySmall.copy(color = if (programa.isEmpty()) senaColors().textMuted else senaColors().text),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = senaColors().green,
                                    unfocusedBorderColor = senaColors().border
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                programs.forEach { p ->
                                    DropdownMenuItem(
                                        text = { Text(p, style = MaterialTheme.typography.bodySmall) },
                                        onClick = {
                                            programa = p
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            SenaAlertBanner(
                title = "Privacidad del Grupo",
                message = "Al crear la ficha, se generará un enlace de invitación único para tus aprendices.",
                icon = Icons.Default.Security,
                color = senaColors().info
            )

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun JoinFichaScreenPreview() {
    ProyecTwinTheme {
        JoinFichaScreen(onBack = {}, onFichaCreated = {})
    }
}
