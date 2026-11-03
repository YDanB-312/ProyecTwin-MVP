package com.example.proyectwin.ui.screens.aprendiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.proyectwin.data.model.Ficha
import com.example.proyectwin.ui.components.SenaAvatar
import com.example.proyectwin.ui.components.SenaCard
import com.example.proyectwin.ui.components.SenaEmptyState
import com.example.proyectwin.ui.components.SenaErrorState
import com.example.proyectwin.ui.components.SenaLoadingState
import com.example.proyectwin.ui.components.SenaPageHeader
import com.example.proyectwin.ui.components.SenaSectionHeader
import com.example.proyectwin.ui.components.SenaStatusBadge
import com.example.proyectwin.ui.components.SenaTopBar
import com.example.proyectwin.ui.theme.senaColors
import com.example.proyectwin.ui.viewmodel.DetalleInstructorUiState
import com.example.proyectwin.ui.viewmodel.DetalleInstructorViewModel

/** Perfil del instructor asignado y sus fichas (equivalente a DetalleInstructor web). */
@Composable
fun DetalleInstructorScreen(
    userId: Int,
    onBack: () -> Unit,
    onFichaDetail: (Int) -> Unit,
    detalleInstructorViewModel: DetalleInstructorViewModel = hiltViewModel(),
) {
    val estado by detalleInstructorViewModel.uiState.collectAsState()

    LaunchedEffect(userId) { detalleInstructorViewModel.load(userId) }

    Scaffold(
        topBar = {
            SenaTopBar(
                title = "Mi instructor",
                onBack = onBack,
                showProfile = true,
                showNotifications = true,
            )
        },
        containerColor = senaColors().background,
    ) { padding ->
        when (val actual = estado) {
            is DetalleInstructorUiState.Loading ->
                SenaLoadingState(modifier = Modifier.padding(padding))
            is DetalleInstructorUiState.Error -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                SenaErrorState(message = actual.message, onRetry = { detalleInstructorViewModel.retry() })
            }
            is DetalleInstructorUiState.Success -> {
                val data = actual.data
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    item {
                        SenaPageHeader(
                            title = "Mi Instructor",
                            subtitle = "Conoce a quien acompaña tu proceso de formación",
                            icon = Icons.Default.School,
                        )
                    }

                    if (data.instructor.id == 0) {
                        item {
                            SenaEmptyState(
                                title = "Sin instructor asignado",
                                message = "Aún no tienes una ficha con instructor asignado. Cuando coordinación te asigne una ficha, verás aquí a tu instructor.",
                                icon = Icons.Default.SearchOff,
                            )
                        }
                        return@LazyColumn
                    }

                    item {
                        SenaCard(elevation = 1.dp) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                SenaAvatar(
                                    fotoBase64 = data.instructor.fotoPerfil,
                                    nombre = data.instructor.name,
                                    modifier = Modifier.size(96.dp),
                                )
                                Text(
                                    data.instructor.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = senaColors().text,
                                )
                                Surface(shape = CircleShape, color = senaColors().green.copy(alpha = 0.1f)) {
                                    Text(
                                        "Instructor SENA",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = senaColors().green,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                                if (data.instructor.email.isNotBlank()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Email,
                                            contentDescription = null,
                                            tint = senaColors().textLight,
                                            modifier = Modifier.size(16.dp),
                                        )
                                        Spacer(Modifier.size(6.dp))
                                        Text(
                                            data.instructor.email,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = senaColors().textSecondary,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        SenaSectionHeader(title = "Fichas a cargo (${data.fichas.size})")
                    }

                    if (data.fichas.isEmpty()) {
                        item {
                            SenaEmptyState(
                                message = "Este instructor no tiene fichas asignadas actualmente.",
                                icon = Icons.Default.School,
                            )
                        }
                    } else {
                        items(data.fichas, key = { it.id }) { ficha ->
                            FichaInstructorCard(ficha = ficha, onClick = { onFichaDetail(ficha.id) })
                        }
                    }

                    item { Spacer(Modifier.height(30.dp)) }
                }
            }
        }
    }
}

@Composable
private fun FichaInstructorCard(ficha: Ficha, onClick: () -> Unit) {
    SenaCard(elevation = 1.dp, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    ficha.nombre ?: ficha.programa.ifBlank { "Ficha de formación" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = senaColors().text,
                )
                Text(
                    "${ficha.codigo} · ${ficha.programa.ifBlank { "Sin programa" }}",
                    style = MaterialTheme.typography.labelSmall,
                    color = senaColors().textSecondary,
                )
            }
            SenaStatusBadge(status = ficha.statusDisplay)
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = senaColors().textLight)
        }
    }
}
