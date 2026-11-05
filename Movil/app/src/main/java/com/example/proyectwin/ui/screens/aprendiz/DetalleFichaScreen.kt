package com.example.proyectwin.ui.screens.aprendiz

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.proyectwin.data.model.ClassGroupStatus
import com.example.proyectwin.data.model.GeneralUser
import com.example.proyectwin.data.model.Project
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.senaColors
import com.example.proyectwin.ui.util.formatearFechaLocal
import com.example.proyectwin.ui.viewmodel.DetalleFichaUiState
import com.example.proyectwin.ui.viewmodel.DetalleFichaViewModel

/**
 * Detalle de una ficha para el aprendiz: información, instructor, compañeros y
 * propuestas. La web lo expone en `/aprendiz/detalle-ficha/:id`; Android solo
 * tenía "Mi Ficha", así que este flujo cubre fichas de compañeros/instructores.
 */
@Composable
fun DetalleFichaScreen(
    fichaId: Int,
    onBack: () -> Unit,
    onCompaneroDetail: (Int) -> Unit,
    onInstructorDetail: (Int) -> Unit,
    onProjectDetail: (Int) -> Unit,
    viewModel: DetalleFichaViewModel = hiltViewModel(),
) {
    val estado by viewModel.uiState.collectAsState()
    LaunchedEffect(fichaId) { if (fichaId > 0) viewModel.load(fichaId) }

    Scaffold(
        topBar = {
            SenaTopBar(
                title = "ProyecTwin",
                onBack = onBack,
                showProfile = true,
                showNotifications = true,
            )
        },
        containerColor = senaColors().background,
    ) { padding ->
        when (val actual = estado) {
            is DetalleFichaUiState.Loading -> SenaLoadingState(modifier = Modifier.padding(padding))
            is DetalleFichaUiState.Error -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                SenaErrorState(message = actual.message, onRetry = viewModel::retry)
            }
            is DetalleFichaUiState.Success -> {
                val data = actual.data
                val ficha = data.ficha
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    item {
                        SenaPageHeader(
                            title = ficha.nombre ?: "Ficha de formación",
                            subtitle = ficha.programa.ifBlank { "Programa de formación" },
                            icon = Icons.Default.Groups,
                        )
                    }

                    item {
                        SenaCard(elevation = 1.dp) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        "Información de la ficha",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        ficha.statusDisplay,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (ficha.classGroupStatus == ClassGroupStatus.ACTIVO) {
                                            senaColors().success
                                        } else {
                                            senaColors().warning
                                        },
                                    )
                                }
                                DatoFicha("Código", ficha.codigo)
                                ficha.numero?.takeIf { it.isNotBlank() }?.let { DatoFicha("Número", it) }
                                DatoFicha("Programa", ficha.programa.ifBlank { "Sin programa" })
                                DatoFicha("Instructor", ficha.instructorName ?: "Sin asignar")
                                DatoFicha("Aprendices", ficha.estudiantes.size.toString())
                                ficha.createdAt?.let { DatoFicha("Creada", formatearFechaLocal(it)) }
                                if (ficha.instructorUserId != null) {
                                    SenaButton(
                                        text = "Ver perfil del instructor",
                                        onClick = { onInstructorDetail(ficha.instructorUserId) },
                                        isPrimary = false,
                                        icon = Icons.Default.Person,
                                    )
                                }
                            }
                        }
                    }

                    item { SenaSectionHeader(title = "Compañeros (${ficha.estudiantes.size})") }
                    if (ficha.estudiantes.isEmpty()) {
                        item { SenaEmptyState(message = "Esta ficha aún no tiene aprendices.", icon = Icons.Default.Groups) }
                    } else {
                        items(ficha.estudiantes, key = { it.id }) { aprendiz ->
                            FilaPersonaFicha(aprendiz) { onCompaneroDetail(aprendiz.id) }
                        }
                    }

                    item { SenaSectionHeader(title = "Propuestas de la ficha (${data.propuestas.size})") }
                    if (data.propuestas.isEmpty()) {
                        item { SenaEmptyState(message = "Esta ficha aún no tiene propuestas.", icon = Icons.Default.FolderOpen) }
                    } else {
                        items(data.propuestas, key = { it.id }) { propuesta ->
                            FilaPropuestaFicha(propuesta) { onProjectDetail(propuesta.id) }
                        }
                    }

                    item { Spacer(Modifier.height(40.dp)) }
                }
            }
        }
    }
}

@Composable
private fun DatoFicha(etiqueta: String, valor: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(etiqueta, style = MaterialTheme.typography.bodySmall, color = senaColors().textLight)
        Text(
            valor,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = senaColors().text,
        )
    }
}

@Composable
private fun FilaPersonaFicha(usuario: GeneralUser, onClick: () -> Unit) {
    SenaCard(elevation = 0.5.dp, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = senaColors().green.copy(alpha = 0.1f)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        usuario.name.split(" ").joinToString("") { it.take(1) }.take(2),
                        fontWeight = FontWeight.Bold,
                        color = senaColors().green,
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(usuario.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text(usuario.email, style = MaterialTheme.typography.labelSmall, color = senaColors().textSecondary)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = "Ver perfil", tint = senaColors().textMuted)
        }
    }
}

@Composable
private fun FilaPropuestaFicha(proyecto: Project, onClick: () -> Unit) {
    SenaCard(elevation = 0.5.dp, onClick = onClick) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SenaStatusBadge(status = proyecto.statusDisplay)
                Text(formatearFechaLocal(proyecto.createdAt), style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
            }
            Text(proyecto.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text(
                proyecto.studentName ?: "Sin aprendiz",
                style = MaterialTheme.typography.labelSmall,
                color = senaColors().textSecondary,
            )
        }
    }
}
