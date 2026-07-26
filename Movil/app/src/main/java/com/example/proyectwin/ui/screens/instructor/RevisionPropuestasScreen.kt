package com.example.proyectwin.ui.screens.instructor

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

data class ProposalItem(
    val id: Int,
    val title: String,
    val student: String,
    val program: String,
    val date: String,
    val status: String,
    val technologies: String,
    val summary: String,
    val similarity: Int,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevisionPropuestasScreen(onBack: () -> Unit, onProjectDetail: (Int) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("Todos") }
    val statuses = listOf("Todos", "Pendiente", "Aprobado", "Rechazado", "Observado")
    
    val dummyPropuestas = remember {
        listOf(
            ProposalItem(1, "Sistema IoT para Agricultura", "Ana Martínez", "ADSO", "15 nov 2023", "Pendiente", "Arduino, Python, Firebase", "Sistemas de monitoreo inteligente...", 45),
            ProposalItem(2, "App Móvil para Turismo Local", "Juan Pérez", "Multimedia", "14 nov 2023", "Pendiente", "React Native, Node.js, MongoDB", "Promoción del turismo local con rutas culturales...", 0),
            ProposalItem(3, "Plataforma E-learning para Música", "Laura Gómez", "ADSO", "12 nov 2023", "Observado", "React, Django, PostgreSQL", "Aprendizaje de teoría musical interactiva...", 0),
        )
    }

    val filteredPropuestas = dummyPropuestas.filter { proposal ->
        val matchesStatus = if (selectedStatus == "Todos") true else proposal.status == selectedStatus
        val matchesSearch = proposal.title.contains(searchQuery, ignoreCase = true) || proposal.student.contains(searchQuery, ignoreCase = true)
        matchesStatus && matchesSearch
    }

    Scaffold(
        topBar = {
            SenaTopBar(
                title = "ProyecTwin",
                onBack = onBack,
                showProfile = true,
                showNotifications = true
            )
        },
        containerColor = SenaBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                SenaPageHeader(
                    title = "Revisión de Propuestas",
                    subtitle = "Evalúa las propuestas de proyectos enviadas por los aprendices.",
                    icon = Icons.AutoMirrored.Filled.List
                )
            }

            // Filter Section
            item {
                SenaCard(elevation = 1.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            "Filtros de revisión",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SenaTextLight,
                            letterSpacing = 0.5.sp
                        )
                        SenaTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = "",
                            placeholder = "Buscar por proyecto o aprendiz...",
                            leadingIcon = Icons.Default.Search
                        )
                        
                        Text(
                            "Filtrar por estado",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SenaTextLight,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            statuses.forEach { status ->
                                SenaChip(
                                    text = status,
                                    color = when(status) {
                                        "Aprobado" -> SenaSuccess
                                        "Pendiente" -> SenaWarning
                                        "Observado" -> SenaAccent
                                        "Rechazado" -> SenaDanger
                                        else -> SenaGreen
                                    },
                                    isSelected = selectedStatus == status,
                                    onClick = { selectedStatus = status }
                                )
                            }
                        }
                    }
                }
            }

            if (filteredPropuestas.isEmpty()) {
                item {
                    SenaEmptyState(
                        message = "No hay propuestas que coincidan con los filtros seleccionados.",
                        icon = Icons.Default.SearchOff
                    )
                }
            } else {
                items(filteredPropuestas) { proposal ->
                    ProposalReviewCard(proposal, onDetailClick = { onProjectDetail(proposal.id) })
                }
            }
            
            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}

@Composable
fun ProposalReviewCard(proposal: ProposalItem, onDetailClick: () -> Unit) {
    SenaCard {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SenaStatusBadge(status = proposal.status)
                Text(proposal.date, style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    proposal.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SenaText
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = SenaGreen, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "${proposal.student} • ${proposal.program}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SenaTextSecondary
                    )
                }
            }

            if (proposal.similarity > 0) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = SenaDanger.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SenaDanger.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = SenaDanger, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "${proposal.similarity}% de similitud detectada",
                            style = MaterialTheme.typography.labelSmall,
                            color = SenaDanger,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Text(
                text = proposal.summary,
                style = MaterialTheme.typography.bodySmall,
                color = SenaTextSecondary,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SenaButton(
                    text = "Revisar Propuesta",
                    onClick = onDetailClick,
                    icon = Icons.Default.Visibility,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RevisionPropuestasPreview() {
    ProyecTwinTheme {
        RevisionPropuestasScreen(onBack = {}, onProjectDetail = {})
    }
}
