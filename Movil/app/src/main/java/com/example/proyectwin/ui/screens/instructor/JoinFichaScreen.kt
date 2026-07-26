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
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinFichaScreen(onBack: () -> Unit, onFichaCreated: () -> Unit) {
    var codigo by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var programa by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Scaffold(
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
                SenaButton(text = "Cancelar", onClick = onBack, isPrimary = false, modifier = Modifier.weight(1f))
                SenaButton(
                    text = "Crear Ficha", 
                    onClick = {
                        isSubmitting = true
                        scope.launch {
                            delay(1500)
                            isSubmitting = false
                            onFichaCreated()
                        }
                    }, 
                    isLoading = isSubmitting,
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Add
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
                    SenaTextField(
                        value = codigo, 
                        onValueChange = { codigo = it }, 
                        label = "Código de Ficha *",
                        placeholder = "Ej: ADSO-2568",
                        leadingIcon = Icons.Default.Numbers
                    )
                    
                    SenaTextField(
                        value = nombre, 
                        onValueChange = { nombre = it }, 
                        label = "Nombre descriptivo *",
                        placeholder = "Ej: Análisis y Desarrollo 2026",
                        leadingIcon = Icons.Default.Badge
                    )

                    Column {
                        Text("Programa de Formación *", style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
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
                                textStyle = MaterialTheme.typography.bodySmall.copy(color = if (programa.isEmpty()) SenaTextMuted else SenaText),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SenaGreen,
                                    unfocusedBorderColor = SenaBorder
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
                color = SenaInfo
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
