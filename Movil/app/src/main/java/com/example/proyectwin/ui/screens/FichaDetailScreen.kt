package com.example.proyectwin.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*

data class MemberItem(val initials: String, val name: String, val status: String)

@Composable
fun FichaDetailScreen(onBack: () -> Unit, onNavigate: (String) -> Unit) {
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
            item {
                IconButton(onClick = onBack) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = SenaGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Volver", color = SenaGreen, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            // Ficha Hero Card
            item {
                SenaCard(containerColor = SenaHeader) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Analisis y Desarrollo 2568",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
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
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(modifier = Modifier.size(6.dp), shape = CircleShape, color = SenaSuccess) {}
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Activa",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("CÓDIGO: ADSO-2568", color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // instructor Assigned
            item {
                SenaSectionHeader(title = "Instructor Asignado")
                SenaCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(56.dp),
                            shape = CircleShape,
                            color = SenaGreen.copy(alpha = 0.1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.School, contentDescription = null, tint = SenaGreen)
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Carlos Ruiz", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.ExtraBold, color = SenaText)
                            Text("Tecnologías de la Información", style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
                        }
                    }
                }
            }

            item {
                SenaSectionHeader(
                    title = "Compañeros (12)",
                    actionText = "Ver Directorio",
                    onActionClick = { onNavigate("ficha/directory") }
                )
            }

            val classmates = listOf(
                MemberItem("MG", "Maria Gonzalez", "Activo"),
                MemberItem("JP", "Juan Perez", "Activo"),
                MemberItem("LG", "Laura Gomez", "Activo"),
                MemberItem("AM", "Ana Martinez", "Activo"),
                MemberItem("DS", "Diana Sanchez", "Inactivo")
            )

            items(classmates) { member ->
                ClassmateCardV2(member)
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun ClassmateCardV2(member: MemberItem) {
    SenaCard(modifier = Modifier.padding(vertical = 2.dp), elevation = 0.5.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = if (member.status == "Activo") SenaGreen.copy(alpha = 0.1f) else SenaBorderSoft
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(member.initials, color = if (member.status == "Activo") SenaGreen else SenaTextLight, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(member.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SenaText)
                Text("Aprendiz ADSO", style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
            }
            SenaChip(
                text = member.status,
                color = if (member.status == "Activo") SenaSuccess else SenaTextMuted
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FichaDetailScreenPreview() {
    ProyecTwinTheme {
        FichaDetailScreen(onBack = {}, onNavigate = {})
    }
}
