package com.example.proyectwin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*

@Composable
fun ProjectDetailScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = SenaBackground,
        bottomBar = {
            Surface(tonalElevation = 8.dp, shadowElevation = 16.dp, color = Color.White) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SenaButton(text = "Editar Propuesta", onClick = {}, isPrimary = false, modifier = Modifier.weight(1f))
                    SenaButton(text = "Ver Similitudes", onClick = {}, modifier = Modifier.weight(1f))
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            // Mobile Hero Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(Brush.verticalGradient(colors = listOf(SenaHeader, SenaGreen)))
            ) {
                Column(modifier = Modifier.padding(24.dp).align(Alignment.BottomStart)) {
                    IconButton(onClick = onBack, modifier = Modifier.offset(x = (-12).dp, y = (-40).dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                    SenaChip(text = "Aprobado", color = SenaSuccess)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Sistema IoT para Agricultura de Precisión",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                        color = Color.White
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                // Info Grid (Mobile Optimized)
                SenaCard {
                    DetailRow(Icons.Default.School, "Programa", "ADSO - Trimestre 3")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SenaBorderSoft)
                    DetailRow(Icons.Default.CalendarToday, "Creado el", "15 de marzo, 2026")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SenaBorderSoft)
                    DetailRow(Icons.Default.Person, "Instructor", "Carlos Ruiz")
                }

                SenaSectionHeader(title = "Descripción del Proyecto")
                SenaCard {
                    Text(
                        "Desarrollo de un sistema basado en sensores IoT para monitorear variables ambientales en cultivos, permitiendo la toma de decisiones en tiempo real para optimizar el riego y la fertilización.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = SenaTextSecondary,
                        lineHeight = 24.sp
                    )
                }

                SenaSectionHeader(title = "Equipo de Trabajo")
                SenaCard {
                    TeamMemberRowV2("MG", "Maria Gonzalez", "Líder de Proyecto", isLider = true)
                    Spacer(modifier = Modifier.height(12.dp))
                    TeamMemberRowV2("JP", "Juan Perez", "Desarrollador Backend", isLider = false)
                    Spacer(modifier = Modifier.height(12.dp))
                    TeamMemberRowV2("LG", "Laura Gomez", "Diseñadora UI/UX", isLider = false)
                }

                SenaSectionHeader(title = "Comentarios Recientes")
                ObservationCardV2(
                    author = "Carlos Ruiz",
                    content = "El enfoque IoT es excelente. Asegúrense de documentar bien el protocolo de comunicación MQTT.",
                    date = "Ayer, 4:30 PM"
                )

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = SenaGreen, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
            Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = SenaText)
        }
    }
}

@Composable
fun TeamMemberRowV2(initials: String, name: String, role: String, isLider: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = if (isLider) SenaGreen else SenaBorderSoft
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(initials, color = if (isLider) Color.White else SenaText, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = SenaText)
            Text(role, style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
        }
    }
}

@Composable
fun ObservationCardV2(author: String, content: String, date: String) {
    SenaCard(containerColor = SenaBorderSoft.copy(alpha = 0.3f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(32.dp).background(SenaGreen, CircleShape), contentAlignment = Alignment.Center) {
                Text(author.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(author, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = SenaText)
            Spacer(modifier = Modifier.weight(1f))
            Text(date, style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(content, style = MaterialTheme.typography.bodyMedium, color = SenaTextSecondary)
    }
}

@Preview(showBackground = true)
@Composable
fun ProjectDetailScreenPreview() {
    ProyecTwinTheme {
        ProjectDetailScreen {}
    }
}
