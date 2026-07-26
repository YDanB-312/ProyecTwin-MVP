package com.example.proyectwin.ui.screens.instructor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectwin.navigation.AppNavigation
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*
import kotlinx.coroutines.launch

data class DirectoryMember(
    val name: String,
    val info: String,
    val initials: String,
    val isActive: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FichaDirectoryScreen(onBack: () -> Unit, onCreateFicha: () -> Unit, onNavigate: (String) -> Unit) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    val members = remember {
        listOf(
            DirectoryMember("Ana Martínez", "Última conexión: Hoy 14:30", "AM", true),
            DirectoryMember("Juan Pérez", "Última conexión: Hoy 11:15", "JP", true),
            DirectoryMember("Laura Gómez", "Última conexión: Ayer", "LG", true),
            DirectoryMember("Diana Sánchez", "Última conexión: 05/05/2026", "DS", false),
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SenaTopBar(
                title = "ProyecTwin",
                onBack = onBack,
                showProfile = true,
                showNotifications = true
            )
        },
        containerColor = SenaBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateFicha,
                containerColor = SenaGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Ficha")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                SenaPageHeader(
                    title = "Directorio de Ficha",
                    subtitle = "Listado completo de aprendices pertenecientes al programa.",
                    icon = Icons.Default.AddressBook
                )
            }

            // Ficha Badge Card
            item {
                SenaCard(elevation = 1.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("ADSO-2568", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SenaText)
                            Text("Análisis y Desarrollo de Sistemas", style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
                        }
                        Surface(
                            color = SenaGreen.copy(alpha = 0.1f),
                            shape = CircleShape
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Groups, contentDescription = null, tint = SenaGreen, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("${members.size} Aprendices", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SenaGreen)
                            }
                        }
                    }
                }
            }

            item {
                SenaSectionHeader(title = "Lista de Aprendices")
            }

            items(members) { member ->
                SenaCard(elevation = 0.5.dp, onClick = { onNavigate(AppNavigation.INSTRUCTOR_FICHA_DETAIL) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            color = if (member.isActive) SenaGreen.copy(alpha = 0.1f) else SenaBorderSoft
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    member.initials, 
                                    fontWeight = FontWeight.Bold, 
                                    color = if (member.isActive) SenaGreen else SenaTextMuted
                                )
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(member.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SenaText)
                            Text(member.info, style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
                        }
                        IconButton(onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Opciones de aprendiz no disponibles")
                            }
                        }) {
                            Icon(Icons.Default.MoreVert, contentDescription = null, tint = SenaTextMuted)
                        }
                    }
                }
            }
            
            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}

// Missing icon
val Icons.Filled.AddressBook: ImageVector get() = Icons.Default.ContactPage

@Preview(showBackground = true)
@Composable
fun FichaDirectoryScreenPreview() {
    ProyecTwinTheme {
        FichaDirectoryScreen(onBack = {}, onCreateFicha = {}, onNavigate = {})
    }
}
