package com.example.proyectwin.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectwin.navigation.AppNavigation
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*

data class ProjectSimilarityGroup(
    val name: String,
    val totalSims: Int,
    val maxPercentage: Int,
    val status: String,
    val level: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSimilarityListScreen(onBack: () -> Unit, onNavigate: (String) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    val similarityGroups = remember {
        listOf(
            ProjectSimilarityGroup("Sistema de Gestión Académica", 3, 72, "Pendiente", "Crítico"),
            ProjectSimilarityGroup("App de Reciclaje Inteligente", 1, 48, "Revisada", "Moderado"),
            ProjectSimilarityGroup("Red de Sensores Ambientales", 2, 35, "Resuelta", "Bajo"),
            ProjectSimilarityGroup("Portal de Empleo Digital", 1, 65, "Pendiente", "Moderado"),
        )
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
                    title = "Similitudes",
                    subtitle = "Listado de proyectos con coincidencias detectadas por el sistema.",
                    icon = Icons.Default.Search
                )
            }

            item {
                SenaCard(elevation = 1.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            "Filtrar Similitudes",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SenaTextLight,
                            letterSpacing = 0.5.sp
                        )
                        SenaTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = "",
                            placeholder = "Nombre del proyecto...",
                            leadingIcon = Icons.Default.Search
                        )
                    }
                }
            }

            items(similarityGroups) { group ->
                SimilarityGroupCard(group, onClick = { onNavigate(AppNavigation.ADMIN_SIMILARITY_DETAIL) })
            }
            
            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}

@Composable
fun SimilarityGroupCard(group: ProjectSimilarityGroup, onClick: () -> Unit) {
    val levelColor = when(group.level) {
        "Crítico" -> SenaDanger
        "Moderado" -> SenaWarning
        else -> SenaSuccess
    }

    SenaCard(onClick = onClick) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SenaStatusBadge(status = group.status)
                Surface(
                    color = levelColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        group.level.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = levelColor
                    )
                }
            }

            Column {
                Text(group.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SenaText)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Compare, contentDescription = null, tint = SenaTextLight, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("${group.totalSims} coincidencias encontradas", style = MaterialTheme.typography.labelSmall, color = SenaTextSecondary)
                }
            }

            HorizontalDivider(color = SenaBorderSoft)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Máxima Similitud", style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
                    Text("${group.maxPercentage}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = levelColor)
                }
                
                TextButton(onClick = onClick) {
                    Text("Ver Detalles", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdminSimilarityListPreview() {
    ProyecTwinTheme {
        AdminSimilarityListScreen(onBack = {}, onNavigate = {})
    }
}
