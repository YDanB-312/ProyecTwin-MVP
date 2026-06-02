package com.example.proyectwin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.example.proyectwin.ui.components.SenaButton
import com.example.proyectwin.ui.components.SenaCard
import com.example.proyectwin.ui.components.SenaTextField
import com.example.proyectwin.ui.components.SenaTopBar
import com.example.proyectwin.ui.theme.*

@Composable
fun NewProjectScreen(onBack: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var summary by remember { mutableStateOf("") }
    var technologies by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            SenaTopBar(title = "Nuevo Proyecto", showProfile = false, showNotifications = false)
        },
        containerColor = SenaBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header with Back Button
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
                Text(
                    "Registro de Proyecto",
                    style = MaterialTheme.typography.titleLarge,
                    color = SenaText
                )
            }

            // Section 1: Basic Info
            NewProjectSectionTitle(Icons.Default.Info, "Información Básica")
            SenaCard {
                SenaTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = "Título del Proyecto",
                    placeholder = "Ej: Sistema de Riego Automatizado"
                )
                Spacer(modifier = Modifier.height(16.dp))
                SenaTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = "Resumen",
                    placeholder = "Describe tu proyecto...",
                    modifier = Modifier.heightIn(min = 120.dp)
                )
            }

            // Section 2: Technical Details
            NewProjectSectionTitle(Icons.Default.Settings, "Detalles Técnicos")
            SenaCard {
                Text(
                    "Línea de Aprendizaje",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = SenaText,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                // En un MVP real usaríamos un dropdown, aquí simulamos con un texto estilizado
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SenaBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tecnologías de la Información", color = SenaText)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = SenaTextLight)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                SenaTextField(
                    value = technologies,
                    onValueChange = { technologies = it },
                    label = "Tecnologías a utilizar",
                    placeholder = "Ej: React, Kotlin, MongoDB"
                )
            }

            // Section 3: Team
            NewProjectSectionTitle(Icons.Default.Groups, "Integrantes del Equipo")
            SenaCard {
                TeamMemberItem("Maria Gonzalez (Tú)", true)
                TeamMemberItem("Juan Perez", false)
                TeamMemberItem("Laura Gomez", false)
            }

            // Actions
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SenaButton(
                    text = "Registrar Proyecto",
                    onClick = { /* TODO */ }
                )
                SenaButton(
                    text = "Guardar como Borrador",
                    onClick = { /* TODO */ },
                    isPrimary = false
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun NewProjectSectionTitle(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = SenaGreen, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = SenaText
        )
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
        NewProjectScreen(onBack = {})
    }
}
