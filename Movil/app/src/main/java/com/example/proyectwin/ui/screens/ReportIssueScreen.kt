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
import com.example.proyectwin.ui.components.SenaChip
import com.example.proyectwin.ui.components.SenaTextField
import com.example.proyectwin.ui.components.SenaTopBar
import com.example.proyectwin.ui.theme.*

@Composable
fun ReportIssueScreen(onBack: () -> Unit) {
    var description by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            SenaTopBar(title = "Reportar Falla", showProfile = false, showNotifications = false)
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
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
                Text(
                    "Nuevo Reporte",
                    style = MaterialTheme.typography.titleLarge,
                    color = SenaText
                )
            }

            // Form Card
            SenaCard {
                Text(
                    "Tipo de Falla",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = SenaText
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Simulated Dropdown
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
                        Text("Error del sistema", color = SenaText)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = SenaTextLight)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                SenaTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Descripción Detallada",
                    placeholder = "Describe el problema encontrado...",
                    modifier = Modifier.heightIn(min = 120.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                SenaButton(
                    text = "Enviar Reporte",
                    onClick = { /* TODO */ }
                )
            }

            // History Section
            Text(
                "Historial de Reportes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SenaText
            )

            IssueHistoryItem("#001", "Sistema no permite subir archivos", "Resuelto", SenaSuccess)
            IssueHistoryItem("#002", "Error en datos del perfil", "En Proceso", SenaGreen)
            IssueHistoryItem("#003", "No puedo editar entregables", "Pendiente", SenaWarning)
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun IssueHistoryItem(id: String, desc: String, status: String, color: Color) {
    SenaCard(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(id, fontWeight = FontWeight.Bold, color = SenaGreen, modifier = Modifier.width(50.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(desc, style = MaterialTheme.typography.bodyMedium, color = SenaText, maxLines = 1)
                Text("10 Oct 2023", style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
            }
            SenaChip(text = status, color = color)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReportIssueScreenPreview() {
    ProyecTwinTheme {
        ReportIssueScreen {}
    }
}
