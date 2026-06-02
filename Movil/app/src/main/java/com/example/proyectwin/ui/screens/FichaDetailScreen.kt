package com.example.proyectwin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.proyectwin.ui.components.SenaCard
import com.example.proyectwin.ui.components.SenaChip
import com.example.proyectwin.ui.components.SenaTopBar
import com.example.proyectwin.ui.theme.*

@Composable
fun FichaDetailScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            SenaTopBar(title = "Mi Ficha", showProfile = false, showNotifications = false)
        },
        containerColor = SenaBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header with Back
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                    Text(
                        "Información de Ficha",
                        style = MaterialTheme.typography.titleLarge,
                        color = SenaText
                    )
                }
            }

            // Ficha Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(SenaHeader, SenaAccent)
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Análisis y Desarrollo 2568",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "ADSO - Trimestre 3",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                            Surface(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = CircleShape
                            ) {
                                Text(
                                    "Activa",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }

            // Instructor
            item {
                Text(
                    "Instructor Asignado",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SenaText
                )
                Spacer(modifier = Modifier.height(8.dp))
                SenaCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            color = SenaGreen.copy(alpha = 0.1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.School, contentDescription = null, tint = SenaGreen)
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Carlos Ruiz", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            Text("Tecnologías de la Información", style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
                        }
                    }
                }
            }

            // Classmates
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Compañeros (12)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SenaText
                    )
                    TextButton(onClick = { /* TODO */ }) {
                        Text("Ver Directorio", color = SenaGreen)
                    }
                }
            }

            val classmates = listOf(
                MemberItem("MG", "Maria Gonzalez", "Activo"),
                MemberItem("JP", "Juan Perez", "Activo"),
                MemberItem("LG", "Laura Gomez", "Activo"),
                MemberItem("AM", "Ana Martinez", "Activo")
            )

            items(classmates) { member ->
                ClassmateCard(member)
            }
        }
    }
}

@Composable
fun ClassmateCard(member: MemberItem) {
    SenaCard(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = SenaBorder.copy(alpha = 0.5f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(member.initials, color = SenaText, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(member.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(if (member.status == "Activo") "maria.gonzalez@soy.sena.edu.co" else "Inactivo", style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
            }
            SenaChip(
                text = member.status,
                color = if (member.status == "Activo") SenaSuccess else SenaTextLight
            )
        }
    }
}

data class MemberItem(val initials: String, val name: String, val status: String)

@Preview(showBackground = true)
@Composable
fun FichaDetailScreenPreview() {
    ProyecTwinTheme {
        FichaDetailScreen(onBack = {})
    }
}
