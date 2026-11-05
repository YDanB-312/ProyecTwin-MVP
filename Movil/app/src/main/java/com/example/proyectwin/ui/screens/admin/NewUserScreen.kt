package com.example.proyectwin.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.proyectwin.data.model.ActivacionUsuario
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*
import com.example.proyectwin.ui.viewmodel.AdminViewModel

/**
 * Alta de usuario (solo admin): la cuenta nace pendiente y el backend envía un
 * enlace de activación al correo personal. No se generan claves temporales.
 */
@Composable
fun NewUserScreen(
    onBack: () -> Unit,
    adminViewModel: AdminViewModel = hiltViewModel(),
) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var apellido by rememberSaveable { mutableStateOf("") }
    var tipoDocumento by rememberSaveable { mutableStateOf("CC") }
    var numeroDocumento by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var rol by rememberSaveable { mutableStateOf("aprendiz") }
    var errorLocal by remember { mutableStateOf<String?>(null) }
    var enviando by remember { mutableStateOf(false) }
    var errorServidor by remember { mutableStateOf<String?>(null) }
    var resultadoAlta by remember { mutableStateOf<ActivacionUsuario?>(null) }
    var reenviando by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    resultadoAlta?.let { datos ->
        AlertDialog(
            onDismissRequest = { },
            icon = {
                Icon(
                    if (datos.enviada) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (datos.enviada) senaColors().success else senaColors().warning,
                )
            },
            title = { Text("Usuario creado", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "La cuenta de ${datos.usuario.name} quedó pendiente de activación.",
                        style = MaterialTheme.typography.bodySmall,
                        color = senaColors().textSecondary,
                    )
                    Text(
                        if (datos.enviada) "Enviamos el enlace de activación al correo registrado."
                        else "El correo no pudo enviarse. Reenvía la activación desde aquí o desde el detalle del usuario.",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (datos.enviada) senaColors().success else senaColors().warning,
                    )
                    Surface(
                        color = senaColors().inputBackground,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Correo personal", style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
                                Text(datos.correo, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = { clipboard.setText(AnnotatedString(datos.correo)) }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", tint = senaColors().green)
                            }
                        }
                    }
                    if (datos.mensaje.isNotBlank()) {
                        Text(datos.mensaje, style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!datos.enviada) {
                        TextButton(
                            enabled = !reenviando,
                            onClick = {
                                reenviando = true
                                adminViewModel.reenviarActivacion(datos.usuario.id) { resultado ->
                                    reenviando = false
                                    resultado.fold(
                                        onSuccess = { mensaje ->
                                            resultadoAlta = datos.copy(enviada = true, mensaje = mensaje)
                                            Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
                                        },
                                        onFailure = { error ->
                                            Toast.makeText(
                                                context,
                                                error.message ?: "No se pudo reenviar la activación.",
                                                Toast.LENGTH_LONG,
                                            ).show()
                                        },
                                    )
                                }
                            },
                        ) {
                            Text(if (reenviando) "Reenviando…" else "Reenviar activación", color = senaColors().green)
                        }
                    }
                    TextButton(onClick = { resultadoAlta = null; onBack() }) {
                        Text("Listo", color = senaColors().green, fontWeight = FontWeight.Bold)
                    }
                }
            },
        )
    }

    Scaffold(
        topBar = {
            SenaTopBar(
                title = "Nuevo Usuario",
                onBack = onBack,
                showProfile = true,
                showNotifications = true,
            )
        },
        containerColor = senaColors().background,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            SenaPageHeader(
                title = "Crear Usuario",
                subtitle = "El sistema genera usuario y contraseña temporal automáticamente.",
                icon = Icons.Default.PersonAdd,
            )

            errorLocal?.let {
                SenaAlertBanner(title = "Revisa el formulario", message = it, icon = Icons.Default.Error, color = senaColors().danger)
            }
            errorServidor?.let {
                SenaAlertBanner(title = "Error del servidor", message = it, icon = Icons.Default.Error, color = senaColors().danger)
            }

            SenaCard {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SenaTextField(value = nombre, onValueChange = { nombre = it; errorLocal = null }, label = "Nombre *", placeholder = "Ej: Ana María")
                    SenaTextField(value = apellido, onValueChange = { apellido = it; errorLocal = null }, label = "Apellido *", placeholder = "Ej: Gómez Ruiz")

                    Text("Tipo de documento *", style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
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
                        onValueChange = { numeroDocumento = it; errorLocal = null },
                        label = "Número de documento *",
                        placeholder = "Ej: 1098765432",
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                    )
                    SenaTextField(
                        value = correo,
                        onValueChange = { correo = it; errorLocal = null },
                        label = "Correo institucional *",
                        placeholder = "usuario@sena.edu.co",
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Email,
                    )

                    Text("Rol *", style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("aprendiz" to "Aprendiz", "instructor" to "Instructor").forEach { (valor, etiqueta) ->
                            SenaChip(
                                text = etiqueta,
                                color = senaColors().green,
                                isSelected = rol == valor,
                                onClick = { rol = valor },
                            )
                        }
                    }
                }
            }

            SenaButton(
                text = "CREAR USUARIO",
                onClick = {
                    errorLocal = when {
                        nombre.isBlank() || apellido.isBlank() -> "Nombre y apellido son obligatorios."
                        numeroDocumento.isBlank() -> "El número de documento es obligatorio."
                        !android.util.Patterns.EMAIL_ADDRESS.matcher(correo.trim()).matches() -> "Ingresa un correo válido."
                        else -> null
                    }
                    if (errorLocal == null && !enviando) {
                        enviando = true
                        adminViewModel.createUser(
                            nombre = nombre.trim(),
                            apellido = apellido.trim(),
                            tipoDocumento = tipoDocumento,
                            numeroDocumento = numeroDocumento.trim(),
                            correo = correo.trim().lowercase(),
                            rol = rol,
                        ) { resultado ->
                            enviando = false
                            resultado.fold(
                                onSuccess = { resultadoAlta = it },
                                onFailure = { errorServidor = it.message ?: "No se pudo crear el usuario." },
                            )
                        }
                    }
                },
                isLoading = enviando,
                icon = Icons.Default.PersonAdd,
            )

            Spacer(Modifier.height(40.dp))
        }
    }
}
