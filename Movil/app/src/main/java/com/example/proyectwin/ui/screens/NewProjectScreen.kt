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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*

@Composable
fun NewProjectScreen(onBack: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var summary by remember { mutableStateOf("") }
    var keywords by remember { mutableStateOf("") }
    var technologies by remember { mutableStateOf("") }
    var objectives by remember { mutableStateOf("") }
    var deliverables by remember { mutableStateOf("") }
    
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            SenaTopBar(title = "Nuevo Proyecto", showProfile = false, showNotifications = false)
        },
        containerColor = SenaBackground,
        bottomBar = {
            Surface(tonalElevation = 8.dp, shadowElevation = 16.dp, color = Color.White) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SenaButton(text = "Cancelar", onClick = onBack, isPrimary = false, modifier = Modifier.weight(1f))
                    SenaButton(text = "Guardar Proyecto", onClick = { /* Submit Logic */ }, modifier = Modifier.weight(1f))
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Page Header (Breadcrumb style)
            IconButton(onClick = onBack) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = SenaGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Volver a Mis proyectos", color = SenaGreen, style = MaterialTheme.typography.labelLarge)
                }
            }

            // Section 1: Información Básica
            SenaSectionHeader(title = "Información Básica")
            SenaCard {
                SenaTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = "Título del proyecto",
                    placeholder = "Ingresa un titulo descriptivo"
                )
                Spacer(modifier = Modifier.height(20.dp))
                SenaTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = "Resumen del proyecto",
                    placeholder = "Describe brevemente tu proyecto...",
                    modifier = Modifier.heightIn(min = 120.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                SenaTextField(
                    value = keywords,
                    onValueChange = { keywords = it },
                    label = "Palabras Clave",
                    placeholder = "Ej: desarrollo web, app movil"
                )
            }

            // Section 2: Detalles Técnicos
            SenaSectionHeader(title = "Detalles Técnicos")
            SenaCard {
                Text("Línea de Aprendizaje", style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
                Spacer(modifier = Modifier.height(8.dp))
                DropdownSimulator(text = "Selecciona una línea tecnológica")
                
                Spacer(modifier = Modifier.height(20.dp))
                
                SenaTextField(
                    value = technologies,
                    onValueChange = { technologies = it },
                    label = "Tecnologías a Utilizar",
                    placeholder = "Ej: React, Node.js, MongoDB"
                )
                Spacer(modifier = Modifier.height(20.dp))
                SenaTextField(
                    value = objectives,
                    onValueChange = { objectives = it },
                    label = "Objetivos Específicos",
                    placeholder = "Describe los objetivos específicos...",
                    modifier = Modifier.heightIn(min = 100.dp)
                )
            }

            // Section 3: Integrantes del Equipo
            SenaSectionHeader(title = "Integrantes del Equipo")
            SenaCard {
                Text("Selecciona los integrantes de tu ficha", style = MaterialTheme.typography.bodySmall, color = SenaTextSecondary)
                Spacer(modifier = Modifier.height(12.dp))
                TeamMemberItem("Maria Gonzalez (Tú)", isSelected = true)
                TeamMemberItem("Juan Perez", isSelected = false)
                TeamMemberItem("Laura Gomez", isSelected = false)
                TeamMemberItem("Ana Martinez", isSelected = false)
            }

            // Section 4: Información Adicional
            SenaSectionHeader(title = "Información Adicional")
            SenaCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Duración (meses)", style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
                        SenaTextField(value = "", onValueChange = {}, label = "", placeholder = "6")
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Fecha Inicio", style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
                        SenaTextField(value = "", onValueChange = {}, label = "", placeholder = "dd/mm/aaaa")
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun DropdownSimulator(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, SenaBorder)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text, style = MaterialTheme.typography.bodyMedium, color = SenaTextSecondary)
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = SenaGreen)
        }
    }
}

@Composable
fun TeamMemberItem(name: String, isSelected: Boolean) {
    var checked by remember { mutableStateOf(isSelected) }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { checked = it },
            colors = CheckboxDefaults.colors(checkedColor = SenaGreen)
        )
        Text(name, style = MaterialTheme.typography.bodyMedium, color = SenaText)
    }
}

@Preview(showBackground = true)
@Composable
fun NewProjectScreenPreview() {
    ProyecTwinTheme {
        NewProjectScreen {}
    }
}
