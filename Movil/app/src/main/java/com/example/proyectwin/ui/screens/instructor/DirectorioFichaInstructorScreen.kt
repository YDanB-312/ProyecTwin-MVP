package com.example.proyectwin.ui.screens.instructor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.proyectwin.data.model.GeneralUser
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.senaColors
import com.example.proyectwin.ui.viewmodel.DetalleFichaUiState
import com.example.proyectwin.ui.viewmodel.DetalleFichaViewModel

/** Directorio de una ficha concreta del instructor, con búsqueda y orden. */
@Composable
fun DirectorioFichaInstructorScreen(
    fichaId: Int,
    onBack: () -> Unit,
    onCompaneroDetail: (Int) -> Unit,
    viewModel: DetalleFichaViewModel = hiltViewModel(),
) {
    val estado by viewModel.uiState.collectAsState()
    var busqueda by rememberSaveable { mutableStateOf("") }
    var ordenAscendente by rememberSaveable { mutableStateOf(true) }
    var pagina by rememberSaveable { mutableIntStateOf(1) }
    val porPagina = 6

    LaunchedEffect(fichaId) { if (fichaId > 0) viewModel.load(fichaId) }

    Scaffold(
        topBar = {
            SenaTopBar(
                title = "Directorio de ficha",
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
            ) { SenaErrorState(message = actual.message, onRetry = viewModel::retry) }
            is DetalleFichaUiState.Success -> {
                val ficha = actual.data.ficha
                val filtrados = ficha.estudiantes
                    .filter {
                        busqueda.isBlank() ||
                            it.name.contains(busqueda, true) ||
                            it.email.contains(busqueda, true)
                    }
                    .let { lista -> if (ordenAscendente) lista.sortedBy { it.name.lowercase() } else lista.sortedByDescending { it.name.lowercase() } }
                val totalPaginas = ((filtrados.size + porPagina - 1) / porPagina).coerceAtLeast(1)
                val actualPagina = pagina.coerceIn(1, totalPaginas)
                val visibles = filtrados.drop((actualPagina - 1) * porPagina).take(porPagina)

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    item {
                        SenaPageHeader(
                            title = ficha.nombre ?: "Ficha de formación",
                            subtitle = "${ficha.codigo} · ${ficha.programa.ifBlank { "Sin programa" }}",
                            icon = Icons.Default.Groups,
                        )
                    }
                    item {
                        SenaTextField(
                            value = busqueda,
                            onValueChange = { busqueda = it; pagina = 1 },
                            label = "",
                            placeholder = "Buscar por nombre o correo...",
                            leadingIcon = Icons.Default.Search,
                        )
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SenaChip(
                                text = "A–Z",
                                color = senaColors().green,
                                isSelected = ordenAscendente,
                                onClick = { ordenAscendente = true },
                            )
                            SenaChip(
                                text = "Z–A",
                                color = senaColors().green,
                                isSelected = !ordenAscendente,
                                onClick = { ordenAscendente = false },
                            )
                        }
                    }
                    if (visibles.isEmpty()) {
                        item {
                            SenaEmptyState(
                                message = if (ficha.estudiantes.isEmpty()) {
                                    "Esta ficha aún no tiene aprendices."
                                } else {
                                    "No hay aprendices que coincidan con la búsqueda."
                                },
                                icon = Icons.Default.SearchOff,
                            )
                        }
                    } else {
                        items(visibles, key = { it.id }) { aprendiz ->
                            FilaDirectorio(aprendiz) { onCompaneroDetail(aprendiz.id) }
                        }
                    }
                    if (totalPaginas > 1) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                TextButton(enabled = actualPagina > 1, onClick = { pagina = actualPagina - 1 }) {
                                    Text("Anterior")
                                }
                                Text(
                                    "Página $actualPagina de $totalPaginas",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = senaColors().textSecondary,
                                )
                                TextButton(enabled = actualPagina < totalPaginas, onClick = { pagina = actualPagina + 1 }) {
                                    Text("Siguiente")
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(40.dp)) }
                }
            }
        }
    }
}

@Composable
private fun FilaDirectorio(usuario: GeneralUser, onClick: () -> Unit) {
    SenaCard(elevation = 0.5.dp, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SenaAvatar(
                fotoBase64 = usuario.fotoPerfil,
                nombre = usuario.name,
                modifier = Modifier.size(44.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(usuario.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(usuario.email, style = MaterialTheme.typography.labelSmall, color = senaColors().textSecondary)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = "Ver perfil", tint = senaColors().textMuted)
        }
    }
}
