package com.example.proyectwin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectwin.data.mock.MockDataProvider
import com.example.proyectwin.data.model.Ficha
import com.example.proyectwin.navigation.AppNavigation
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*
import com.example.proyectwin.ui.viewmodel.AuthViewModel
import com.example.proyectwin.ui.viewmodel.FichasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FichaDetailScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    fichasViewModel: FichasViewModel = viewModel()
) {
    val authState by authViewModel.uiState.collectAsState()
    val user = authState.user

    val ficha = remember(user) {
        val targetFichaId = user?.fichaId
        if (targetFichaId != null) {
            MockDataProvider.findFichaById(targetFichaId)
        } else {
            MockDataProvider.getActiveFichas().firstOrNull()
        }
    }

    var lightboxEstudiante by remember { mutableStateOf<com.example.proyectwin.data.model.GeneralUser?>(null) }

    if (lightboxEstudiante != null) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.9f)).clickable { lightboxEstudiante = null },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                SenaAvatar(
                    fotoBase64 = null,
                    nombre = lightboxEstudiante!!.name,
                    modifier = Modifier.size(180.dp)
                )
                Spacer(Modifier.height(20.dp))
                Text(lightboxEstudiante!!.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Aprendiz • Activo", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
            }
            IconButton(
                onClick = { lightboxEstudiante = null },
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White, modifier = Modifier.size(28.dp))
            }
        }
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
        containerColor = senaColors().background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                SenaPageHeader(
                    title = "Mi Ficha",
                    subtitle = "Detalles del programa de formación y compañeros de equipo.",
                    icon = Icons.Default.Groups
                )
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Brush.linearGradient(colors = listOf(senaColors().header, senaColors().green)))
                        .padding(24.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    ficha?.programa ?: "Sin ficha asignada",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    "Código: ${ficha?.codigo ?: "N/A"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                            Surface(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = CircleShape
                            ) {
                                Text(
                                    ficha?.statusDisplay?.uppercase() ?: "N/A",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Copiar código", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.9f))
                            Spacer(Modifier.width(8.dp))
                            ficha?.let {
                                SenaCopyButton(textToCopy = it.codigo, label = it.codigo)
                            }
                        }
                    }
                }
            }

            item {
                SenaSectionHeader(title = "Instructor Encargado")
                SenaCard(elevation = 1.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            color = senaColors().green.copy(alpha = 0.1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.School, contentDescription = null, tint = senaColors().green, modifier = Modifier.size(24.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(ficha?.instructorName ?: "No asignado", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = senaColors().text)
                            Text("Líder de Ficha • ${ficha?.programa ?: ""}", style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
                        }
                    }
                }
            }

            item {
                SenaSectionHeader(
                    title = "Compañeros de Ficha",
                    actionText = "Ver todos",
                    onActionClick = { onNavigate(AppNavigation.INSTRUCTOR_FICHA_DETAIL) }
                )
            }

            val estudiantes = ficha?.estudiantes ?: emptyList()
            if (estudiantes.isEmpty()) {
                item {
                    SenaEmptyState(message = "No hay estudiantes en esta ficha.", icon = Icons.Default.Groups)
                }
            } else {
                items(estudiantes) { estudiante ->
                    SenaCard(elevation = 0.5.dp) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(40.dp).clickable { lightboxEstudiante = estudiante },
                                shape = CircleShape,
                                color = senaColors().green.copy(alpha = 0.1f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        estudiante.initials,
                                        fontWeight = FontWeight.Bold,
                                        color = senaColors().green
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f).clickable {
                                onNavigate(
                                    AppNavigation.APRENDIZ_COMPANERO_DETAIL
                                        .replace("{nombre}", estudiante.name)
                                        .replace("{iniciales}", estudiante.initials)
                                        .replace("{estado}", "Activo")
                                )
                            }) {
                                Text(estudiante.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = senaColors().text)
                                Text("Aprendiz", style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
                            }
                            SenaStatusBadge(status = "Activo")
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(40.dp)) }
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
