package com.example.proyectwin.ui.screens.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.proyectwin.data.model.Ficha
import com.example.proyectwin.data.model.GeneralUser
import com.example.proyectwin.data.model.Project
import com.example.proyectwin.navigation.AppNavigation
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*
import com.example.proyectwin.ui.util.formatearFechaLocal
import com.example.proyectwin.ui.viewmodel.AdminFichaDetailUiState
import com.example.proyectwin.ui.viewmodel.AdminFichaDetailViewModel

/**
 * Detalle admin de una ficha: información, roster y propuestas. La edición
 * reutiliza el formulario compartido; la eliminación sigue en el listado.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminFichaDetailScreen(
    fichaId: String = "",
    onBack: () -> Unit,
    onNavigate: (String) -> Unit = {},
    adminFichaDetailViewModel: AdminFichaDetailViewModel = hiltViewModel(),
) {
    val uiState by adminFichaDetailViewModel.uiState.collectAsState()
    val refrescando by adminFichaDetailViewModel.refrescando.collectAsState()
    val candidatos by adminFichaDetailViewModel.candidatos.collectAsState()
    val accionError by adminFichaDetailViewModel.accionError.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var filtroPropuestas by rememberSaveable { mutableStateOf("Todas") }
    var mostrarAgregar by remember { mutableStateOf(false) }
    var mostrarCrearAprendiz by remember { mutableStateOf(false) }

    LaunchedEffect(accionError) {
        accionError?.let {
            snackbarHostState.showSnackbar(it)
            adminFichaDetailViewModel.limpiarAccionError()
        }
    }

    LaunchedEffect(fichaId) {
        fichaId.toIntOrNull()?.let { adminFichaDetailViewModel.load(it) }
    }

    val fichaActual = (uiState as? AdminFichaDetailUiState.Success)?.data?.ficha

    Scaffold(
        topBar = {
            SenaTopBar(
                title = "Detalle de ficha",
                onBack = onBack,
                showProfile = true,
                showNotifications = true,
            )
        },
        containerColor = senaColors().background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            fichaActual?.let { ficha ->
                SenaBottomBar {
                    SenaButton(
                        text = "Editar ficha",
                        onClick = {
                            onNavigate(
                                AppNavigation.INSTRUCTOR_CREAR_FICHA.replace(
                                    "{fichaId}",
                                    ficha.id.toString(),
                                ),
                            )
                        },
                        icon = Icons.Default.Edit,
                    )
                }
            }
        },
    ) { paddingValues ->
        when (val estado = uiState) {
            is AdminFichaDetailUiState.Loading ->
                SenaLoadingState(modifier = Modifier.padding(paddingValues))
            is AdminFichaDetailUiState.Error -> Box(
                Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                SenaErrorState(message = estado.message, onRetry = { adminFichaDetailViewModel.recargar() })
            }
            is AdminFichaDetailUiState.Success -> {
                val data = estado.data
                SenaPullRefresh(
                    isRefreshing = refrescando,
                    onRefresh = { adminFichaDetailViewModel.refrescar() },
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        item {
                            SenaPageHeader(
                                title = data.ficha.nombre ?: data.ficha.programa.ifBlank { "Ficha de formación" },
                                subtitle = "Código ${data.ficha.codigo} · N° ${data.ficha.numero ?: "—"} · " +
                                    data.ficha.programa.ifBlank { "Sin programa" },
                                icon = Icons.Default.Groups,
                            )
                        }

                        item {
                            PanelInformacionFicha(
                                ficha = data.ficha,
                                propuestasCount = data.propuestas.size,
                            )
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                SenaSectionHeader(title = "Aprendices (${data.ficha.estudiantes.size})")
                                TextButton(
                                    onClick = {
                                        adminFichaDetailViewModel.cargarCandidatos()
                                        mostrarAgregar = true
                                    },
                                ) { Text("Agregar aprendiz", color = senaColors().green) }
                            }
                        }
                        if (data.ficha.estudiantes.isEmpty()) {
                            item {
                                SenaEmptyState(
                                    message = "Sin aprendices en esta ficha. Usa \"Editar\" en el listado para agregarlos.",
                                    icon = Icons.Default.Groups,
                                )
                            }
                        } else {
                            items(data.ficha.estudiantes, key = { it.id }) { aprendiz ->
                                FilaAprendiz(
                                    aprendiz = aprendiz,
                                    onClick = {
                                        onNavigate(AppNavigation.ADMIN_USER_DETAIL.replace("{userId}", aprendiz.id.toString()))
                                    },
                                    onRemove = { adminFichaDetailViewModel.quitarAprendiz(aprendiz.id) },
                                )
                            }
                        }

                        item { SenaSectionHeader(title = "Propuestas (${data.propuestas.size})") }
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                listOf("Todas", "Borrador", "Pendiente", "Aprobado", "Rechazado").forEach { estado ->
                                    SenaChip(
                                        text = estado,
                                        color = senaColors().green,
                                        isSelected = filtroPropuestas == estado,
                                        onClick = { filtroPropuestas = estado },
                                    )
                                }
                            }
                        }
                        val propuestasFiltradas = data.propuestas.filter {
                            filtroPropuestas == "Todas" || it.estado == filtroPropuestas.lowercase()
                        }
                        when {
                            data.propuestasError -> item {
                                Text(
                                    "No se pudieron cargar las propuestas de la ficha.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = senaColors().textSecondary,
                                )
                            }
                            propuestasFiltradas.isEmpty() -> item {
                                SenaEmptyState(
                                    message = if (data.propuestas.isEmpty()) {
                                        "Esta ficha aún no tiene propuestas asociadas."
                                    } else {
                                        "No hay propuestas con ese estado."
                                    },
                                    icon = Icons.Default.FolderOpen,
                                )
                            }
                            else -> items(propuestasFiltradas, key = { it.id }) { propuesta ->
                                FilaPropuesta(
                                    propuesta = propuesta,
                                    onClick = {
                                        onNavigate(
                                            AppNavigation.ADMIN_PROJECT_DETAIL.replace("{projectId}", propuesta.id.toString()),
                                        )
                                    },
                                )
                            }
                        }

                        item { Spacer(Modifier.height(60.dp)) }
                    }
                }
            }
        }
    }

    if (mostrarAgregar) {
        AlertDialog(
            onDismissRequest = { mostrarAgregar = false },
            title = { Text("Agregar aprendiz") },
            text = {
                if (candidatos.isEmpty()) {
                    Text("No hay aprendices disponibles fuera de esta ficha.")
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                        items(candidatos, key = { it.id }) { usuario ->
                            ListItem(
                                headlineContent = { Text(usuario.name) },
                                supportingContent = { Text(usuario.email) },
                                modifier = Modifier.clickable {
                                    mostrarAgregar = false
                                    adminFichaDetailViewModel.agregarAprendiz(usuario.id)
                                },
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarAgregar = false }) { Text("Cerrar") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarAgregar = false
                        mostrarCrearAprendiz = true
                    },
                ) { Text("Crear cuenta nueva", color = senaColors().green) }
            },
        )
    }

    if (mostrarCrearAprendiz) {
        var nombre by remember { mutableStateOf("") }
        var apellido by remember { mutableStateOf("") }
        var tipoDocumento by remember { mutableStateOf("CC") }
        var numeroDocumento by remember { mutableStateOf("") }
        var correo by remember { mutableStateOf("") }
        var errorLocal by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { mostrarCrearAprendiz = false },
            title = { Text("Crear aprendiz en la ficha") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SenaTextField(value = nombre, onValueChange = { nombre = it }, label = "Nombre")
                    SenaTextField(value = apellido, onValueChange = { apellido = it }, label = "Apellido")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("CC", "TI", "CE", "PPT").forEach { tipo ->
                            SenaChip(
                                text = tipo,
                                color = senaColors().green,
                                isSelected = tipoDocumento == tipo,
                                onClick = { tipoDocumento = tipo },
                            )
                        }
                    }
                    SenaTextField(
                        value = numeroDocumento,
                        onValueChange = { numeroDocumento = it },
                        label = "Número de documento",
                    )
                    SenaTextField(
                        value = correo,
                        onValueChange = { correo = it },
                        label = "Correo",
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Email,
                    )
                    errorLocal?.let {
                        Text(it, color = senaColors().danger, style = MaterialTheme.typography.labelSmall)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        errorLocal = when {
                            nombre.isBlank() || apellido.isBlank() -> "Completa nombre y apellido."
                            numeroDocumento.isBlank() -> "Ingresa el documento."
                            !android.util.Patterns.EMAIL_ADDRESS.matcher(correo.trim()).matches() ->
                                "Ingresa un correo válido."
                            else -> null
                        }
                        if (errorLocal == null) {
                            adminFichaDetailViewModel.crearAprendiz(
                                nombre = nombre.trim(),
                                apellido = apellido.trim(),
                                tipoDocumento = tipoDocumento,
                                numeroDocumento = numeroDocumento.trim(),
                                correo = correo.trim().lowercase(),
                            ) { mensaje ->
                                mostrarCrearAprendiz = false
                                mensaje?.let {
                                    android.widget.Toast.makeText(
                                        context,
                                        it,
                                        android.widget.Toast.LENGTH_LONG,
                                    ).show()
                                }
                            }
                        }
                    },
                ) { Text("Crear", color = senaColors().green) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarCrearAprendiz = false }) { Text("Cancelar") }
            },
        )
    }
}

@Composable
private fun PanelInformacionFicha(
    ficha: Ficha,
    propuestasCount: Int,
) {
    SenaCard(elevation = 1.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Información de la ficha",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = senaColors().text,
                    modifier = Modifier.weight(1f),
                )
                SenaStatusBadge(status = ficha.statusDisplay)
            }

            HorizontalDivider(color = senaColors().borderSoft)

            DetailRow(Icons.Default.School, "Programa de formación", ficha.programa.ifBlank { "Sin programa" })
            DetailRow(Icons.Default.Hub, "Red de conocimiento", ficha.redNombre ?: "Sin red")
            DetailRow(Icons.Default.Badge, "Número de ficha", ficha.numero ?: "—")
            DetailRow(Icons.Default.Event, "Creada", formatearFechaLocal(ficha.createdAt))
            DetailRow(Icons.Default.Groups, "Aprendices", ficha.estudiantes.size.toString())
            DetailRow(Icons.Default.FolderOpen, "Propuestas", propuestasCount.toString())
            // Sin enlace al usuario: `instructorId` es la fila de `instructors`,
            // no el id de `general_users` que espera el detalle de usuario.
            DetailRow(Icons.Default.Person, "Instructor a cargo", ficha.instructorName ?: "Sin asignar")
        }
    }
}

@Composable
private fun FilaAprendiz(aprendiz: GeneralUser, onClick: () -> Unit, onRemove: () -> Unit) {
    SenaCard(elevation = 0.5.dp, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SenaAvatar(
                fotoBase64 = aprendiz.fotoPerfil,
                nombre = aprendiz.name,
                modifier = Modifier.size(40.dp),
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    aprendiz.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = senaColors().text,
                )
                Text(
                    aprendiz.email,
                    style = MaterialTheme.typography.labelSmall,
                    color = senaColors().textLight,
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.PersonRemove,
                    contentDescription = "Retirar aprendiz",
                    tint = senaColors().danger,
                    modifier = Modifier.size(20.dp),
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Ver aprendiz",
                tint = senaColors().textMuted,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun FilaPropuesta(propuesta: Project, onClick: () -> Unit) {
    SenaCard(elevation = 0.5.dp, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    propuesta.title,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = senaColors().text,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "${propuesta.studentName ?: "Sin autor"} · ${formatearFechaLocal(propuesta.createdAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = senaColors().textLight,
                )
            }
            Spacer(Modifier.width(12.dp))
            SenaStatusBadge(status = propuesta.statusDisplay)
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Ver propuesta",
                tint = senaColors().textMuted,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
