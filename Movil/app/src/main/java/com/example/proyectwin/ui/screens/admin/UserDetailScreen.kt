package com.example.proyectwin.ui.screens.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.proyectwin.data.local.ImagenUtils
import com.example.proyectwin.data.model.GeneralUser
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*
import com.example.proyectwin.ui.viewmodel.AdminUserAction
import com.example.proyectwin.ui.viewmodel.AdminUserUiState
import com.example.proyectwin.ui.viewmodel.UserDetailViewModel
import kotlinx.coroutines.launch

/**
 * Detalle real de una cuenta (admin): edición, estado, rol, ficha del aprendiz
 * y credenciales (restablecer/reenviar). Las reglas finas las valida backend.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(
    userId: String = "",
    onBack: () -> Unit,
    onNavigate: (String) -> Unit = {},
    userDetailViewModel: UserDetailViewModel = hiltViewModel(),
) {
    val uiState by userDetailViewModel.uiState.collectAsState()
    val accion by userDetailViewModel.accion.collectAsState()
    val fichas by userDetailViewModel.fichasDisponibles.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var dialogoEditar by rememberSaveable { mutableStateOf(false) }
    var dialogoEliminar by remember { mutableStateOf(false) }
    var dialogoEliminarDefinitivo by remember { mutableStateOf(false) }
    var dialogoFicha by remember { mutableStateOf(false) }
    var decisionEliminacion by remember { mutableStateOf<AdminUserAction.DecisionEliminacion?>(null) }
    var hashConfirmacion by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        userId.toIntOrNull()?.let { userDetailViewModel.load(it) }
    }

    LaunchedEffect(accion) {
        when (val estado = accion) {
            is AdminUserAction.Saved -> userDetailViewModel.resetAccion()
            is AdminUserAction.Message -> {
                snackbarHostState.showSnackbar(estado.text)
                userDetailViewModel.resetAccion()
            }
            is AdminUserAction.Error -> {
                snackbarHostState.showSnackbar(estado.message)
                userDetailViewModel.resetAccion()
            }
            is AdminUserAction.DecisionEliminacion -> {
                decisionEliminacion = estado
                hashConfirmacion = estado.hash
                userDetailViewModel.resetAccion()
            }
            is AdminUserAction.Deleted -> {
                userDetailViewModel.resetAccion()
                onBack()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            SenaTopBar(
                title = "Detalle de Usuario",
                onBack = onBack,
                showProfile = true,
                showNotifications = true,
            )
        },
        containerColor = senaColors().background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        when (val estado = uiState) {
            is AdminUserUiState.Loading ->
                SenaLoadingState(modifier = Modifier.padding(paddingValues))
            is AdminUserUiState.Error -> Box(
                Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                SenaErrorState(message = estado.message, onRetry = { userDetailViewModel.recargar() })
            }
            is AdminUserUiState.Success -> {
                val data = estado.data
                val usuario = data.usuario
                val enProceso = accion is AdminUserAction.Loading

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    SenaPageHeader(
                        title = usuario.name,
                        subtitle = usuario.roleDisplayName,
                        icon = Icons.Default.Person,
                    )

                    SenaCard(elevation = 1.dp) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            SenaAvatar(fotoBase64 = usuario.fotoPerfil, nombre = usuario.name, modifier = Modifier.size(96.dp))
                            Text(usuario.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(usuario.email, style = MaterialTheme.typography.bodySmall, color = senaColors().textSecondary)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SenaStatusBadge(status = if (usuario.estado) "Activo" else "Suspendido")
                                Surface(shape = CircleShape, color = senaColors().green.copy(alpha = 0.1f)) {
                                    Text(
                                        usuario.roleDisplayName,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = senaColors().green,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        }
                    }

                    SenaSectionHeader(title = "Datos de la cuenta")
                    SenaCard(elevation = 1.dp) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            DatoCuenta("Documento", "${usuario.tipoDocumento ?: ""} ${usuario.documentoIdentidad ?: "—"}".trim())
                            DatoCuenta(
                                "Acceso",
                                when {
                                    usuario.role == "admin" -> "No aplica para administradores"
                                    usuario.emailVerifiedAt != null -> "Contraseña establecida"
                                    usuario.credencialesError != null -> "Falló el envío: ${usuario.credencialesError}"
                                    usuario.credencialesEnviadasEn != null ->
                                        "Pendiente de activación (último envío ${usuario.credencialesEnviadasEn.take(10)})"
                                    else -> "Pendiente de activación"
                                },
                            )
                            if (data.aprendiz != null) {
                                val ficha = data.fichaActual
                                DatoCuenta(
                                    "Ficha",
                                    ficha?.let { "${it.nombre ?: it.programa} (${it.codigo})" } ?: "Sin ficha",
                                )
                            }
                        }
                    }

                    SenaSectionHeader(title = "Acciones")
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SenaButton(
                            text = "EDITAR DATOS",
                            onClick = { dialogoEditar = true },
                            isPrimary = false,
                            enabled = !enProceso,
                            icon = Icons.Default.Edit,
                        )
                        SenaButton(
                            text = if (usuario.estado) "SUSPENDER CUENTA" else "ACTIVAR CUENTA",
                            onClick = { userDetailViewModel.cambiarEstado(!usuario.estado) },
                            isPrimary = false,
                            enabled = !enProceso,
                            icon = if (usuario.estado) Icons.Default.Block else Icons.Default.CheckCircle,
                            containerColor = if (usuario.estado) senaColors().warning10 else senaColors().success10,
                        )
                        if (usuario.role != "admin") {
                            val pendienteActivacion = usuario.emailVerifiedAt == null
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                SenaButton(
                                    text = if (pendienteActivacion) "Enviar activación" else "Restablecer",
                                    onClick = { userDetailViewModel.restablecerCredenciales() },
                                    isPrimary = false,
                                    enabled = !enProceso,
                                    icon = Icons.Default.LockReset,
                                    modifier = Modifier.weight(1f),
                                    minWidth = 0.dp,
                                )
                                if (pendienteActivacion) {
                                    SenaButton(
                                        text = "Reenviar",
                                        onClick = { userDetailViewModel.reenviarActivacion() },
                                        isPrimary = false,
                                        enabled = !enProceso && usuario.estado,
                                        icon = Icons.Default.Send,
                                        modifier = Modifier.weight(1f),
                                        minWidth = 0.dp,
                                    )
                                }
                            }
                        }
                        if (data.aprendiz != null) {
                            SenaButton(
                                text = "CAMBIAR DE FICHA",
                                onClick = { dialogoFicha = true },
                                isPrimary = false,
                                enabled = !enProceso,
                                icon = Icons.Default.SwapHoriz,
                            )
                        }
                        SenaButton(
                            text = "ELIMINAR USUARIO",
                            onClick = { dialogoEliminar = true },
                            isPrimary = false,
                            enabled = !enProceso,
                            icon = Icons.Default.Delete,
                            containerColor = senaColors().danger10,
                        )
                    }

                    Spacer(Modifier.height(40.dp))
                }

                if (dialogoEditar) {
                    EditarUsuarioDialog(
                        usuario = usuario,
                        onDismiss = { dialogoEditar = false },
                        onGuardar = { editado ->
                            dialogoEditar = false
                            userDetailViewModel.guardar(editado)
                        },
                    )
                }

                if (dialogoFicha) {
                    ModalBottomSheet(onDismissRequest = { dialogoFicha = false }) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text("Mover a otra ficha", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                "Actual: ${data.fichaActual?.codigo ?: "sin ficha"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = senaColors().textSecondary,
                            )
                            fichas.filter { it.id != data.fichaActual?.id }.forEach { ficha ->
                                Surface(
                                    onClick = {
                                        dialogoFicha = false
                                        userDetailViewModel.moverAprendiz(ficha.id)
                                    },
                                    color = senaColors().backgroundElevated,
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(ficha.nombre ?: ficha.programa, fontWeight = FontWeight.Bold)
                                        Text("${ficha.codigo} · ${ficha.statusDisplay}", style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
                                    }
                                }
                            }
                            TextButton(onClick = {
                                dialogoFicha = false
                                userDetailViewModel.moverAprendiz(null)
                            }) { Text("Quitar de la ficha actual", color = senaColors().danger) }
                            Spacer(Modifier.height(20.dp))
                        }
                    }
                }

                if (dialogoEliminar) {
                    var confirmacion by remember(usuario.id) { mutableStateOf("") }
                    AlertDialog(
                        onDismissRequest = { dialogoEliminar = false },
                        title = { Text("Eliminar usuario") },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    "¿Seguro que deseas eliminar a \"${usuario.name}\"? Se eliminarán su cuenta y los registros asociados. Si tiene historial relacionado, se te pedirá elegir entre desactivar la cuenta o eliminar definitivamente todos sus registros. Esta acción no se puede deshacer.",
                                )
                                SenaTextField(
                                    value = confirmacion,
                                    onValueChange = { confirmacion = it },
                                    label = "Escribe ELIMINAR para confirmar",
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    dialogoEliminar = false
                                    userDetailViewModel.eliminar()
                                },
                                enabled = confirmacion.trim() == "ELIMINAR",
                            ) { Text("Eliminar", color = senaColors().danger) }
                        },
                        dismissButton = {
                            TextButton(onClick = { dialogoEliminar = false }) { Text("Cancelar") }
                        },
                    )
                }

                decisionEliminacion?.let { decision ->
                    AlertDialog(
                        onDismissRequest = { decisionEliminacion = null },
                        title = { Text("Usuario con historial") },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    "${decision.message.ifBlank { "El usuario tiene registros históricos." }} " +
                                        "Desactivar la cuenta (recomendado) conserva sus registros y bloquea el acceso. " +
                                        "Eliminar definitivamente borra al usuario y sus registros en cascada.",
                                )
                                val resumen = resumenImpacto(decision.impacto)
                                if (resumen.isNotBlank()) {
                                    Text(
                                        "Impacto: $resumen",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = senaColors().textSecondary,
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(
                                enabled = decision.puedeDesactivar,
                                onClick = {
                                    decisionEliminacion = null
                                    userDetailViewModel.cambiarEstado(false)
                                },
                            ) { Text("Desactivar cuenta", color = senaColors().green, fontWeight = FontWeight.Bold) }
                        },
                        dismissButton = {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = { decisionEliminacion = null }) { Text("Cancelar") }
                                TextButton(
                                    onClick = {
                                        decisionEliminacion = null
                                        dialogoEliminarDefinitivo = true
                                    },
                                ) { Text("Eliminar definitivamente…", color = senaColors().danger) }
                            }
                        },
                    )
                }

                if (dialogoEliminarDefinitivo) {
                    var confirmacion by remember(usuario.id) { mutableStateOf("") }
                    AlertDialog(
                        onDismissRequest = { dialogoEliminarDefinitivo = false },
                        title = { Text("Eliminar definitivamente") },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    "Se eliminará la cuenta de \"${usuario.name}\" y, en cascada, sus propuestas, historial, observaciones, reportes y sesiones. Las similitudes compartidas también se eliminarán. Esta acción no se puede deshacer.",
                                )
                                SenaTextField(
                                    value = confirmacion,
                                    onValueChange = { confirmacion = it },
                                    label = "Escribe ELIMINAR DEFINITIVAMENTE",
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    dialogoEliminarDefinitivo = false
                                    userDetailViewModel.eliminar(confirmar = true, hash = hashConfirmacion)
                                },
                                enabled = confirmacion.trim() == "ELIMINAR DEFINITIVAMENTE",
                            ) { Text("Eliminar definitivamente", color = senaColors().danger) }
                        },
                        dismissButton = {
                            TextButton(onClick = { dialogoEliminarDefinitivo = false }) { Text("Cancelar") }
                        },
                    )
                }
            }
        }
    }
}

private fun resumenImpacto(impacto: Map<String, Int>): String = listOf(
    "propuestas" to "propuesta(s) propia(s)",
    "propuestas_fichas" to "propuesta(s) de sus fichas",
    "fichas_a_cargo" to "ficha(s) a cargo",
    "similitudes" to "similitud(es)",
    "observaciones" to "observación(es)",
    "reportes" to "reporte(s)",
    "historial" to "registro(s) de historial",
    "participaciones" to "participación(es) en equipos",
    "asignaciones" to "propuesta(s) asignada(s) como instructor",
).mapNotNull { (clave, etiqueta) ->
    impacto[clave]?.takeIf { it > 0 }?.let { "$it $etiqueta" }
}.joinToString(", ")

@Composable
private fun DatoCuenta(etiqueta: String, valor: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(etiqueta, modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
        Text(valor, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = senaColors().text)
    }
}

@Composable
private fun EditarUsuarioDialog(
    usuario: GeneralUser,
    onDismiss: () -> Unit,
    onGuardar: (GeneralUser) -> Unit,
) {
    var nombre by rememberSaveable(usuario.id) { mutableStateOf(usuario.nombre ?: usuario.name.substringBefore(' ')) }
    var apellido by rememberSaveable(usuario.id) { mutableStateOf(usuario.apellido ?: usuario.name.substringAfter(' ', "")) }
    var correo by rememberSaveable(usuario.id) { mutableStateOf(usuario.email) }
    var tipoDocumento by rememberSaveable(usuario.id) { mutableStateOf(usuario.tipoDocumento ?: "CC") }
    var numeroDocumento by rememberSaveable(usuario.id) { mutableStateOf(usuario.documentoIdentidad.orEmpty()) }
    var rol by rememberSaveable(usuario.id) { mutableStateOf(usuario.role) }
    var estado by rememberSaveable(usuario.id) { mutableStateOf(usuario.estado) }
    // La foto puede ser Base64 grande: no se guarda en el Bundle.
    var fotoUrl by remember(usuario.id) { mutableStateOf(usuario.fotoPerfil) }
    var procesandoFoto by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val selectorFoto = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            scope.launch {
                procesandoFoto = true
                fotoUrl = ImagenUtils.uriAFotoDataUrl(context, it) ?: fotoUrl
                procesandoFoto = false
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar usuario", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SenaAvatar(
                        fotoBase64 = fotoUrl,
                        nombre = "$nombre $apellido".trim(),
                        modifier = Modifier.size(64.dp),
                    )
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        TextButton(
                            enabled = !procesandoFoto,
                            onClick = { selectorFoto.launch("image/*") },
                        ) {
                            Text(
                                if (procesandoFoto) "Procesando…" else "Cambiar foto",
                                color = senaColors().green,
                            )
                        }
                        if (fotoUrl != null) {
                            TextButton(
                                enabled = !procesandoFoto,
                                onClick = { fotoUrl = null },
                            ) {
                                Text("Quitar foto", color = senaColors().danger)
                            }
                        }
                    }
                }
                SenaTextField(value = nombre, onValueChange = { nombre = it }, label = "Nombre")
                SenaTextField(value = apellido, onValueChange = { apellido = it }, label = "Apellido")
                SenaTextField(value = correo, onValueChange = { correo = it }, label = "Correo")
                Text("Tipo de documento", style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
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
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                )
                Text("Rol", style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("aprendiz", "instructor", "admin").forEach { valor ->
                        SenaChip(
                            text = valor.replaceFirstChar { it.uppercase() },
                            color = senaColors().green,
                            isSelected = rol == valor,
                            onClick = { rol = valor },
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Cuenta activa", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                    Switch(checked = estado, onCheckedChange = { estado = it })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onGuardar(
                    usuario.copy(
                        nombre = nombre.trim(),
                        apellido = apellido.trim(),
                        name = "$nombre $apellido".trim(),
                        email = correo.trim().lowercase(),
                        tipoDocumento = tipoDocumento,
                        documentoIdentidad = numeroDocumento.trim(),
                        fotoPerfil = fotoUrl,
                        role = rol,
                        estado = estado,
                    ),
                )
            }) { Text("Guardar", color = senaColors().green, fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
