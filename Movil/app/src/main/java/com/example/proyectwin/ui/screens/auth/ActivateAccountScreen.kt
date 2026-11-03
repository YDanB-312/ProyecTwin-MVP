package com.example.proyectwin.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.proyectwin.domain.util.PasswordPolicy
import com.example.proyectwin.ui.components.SenaAlertBanner
import com.example.proyectwin.ui.components.SenaButton
import com.example.proyectwin.ui.components.SenaCard
import com.example.proyectwin.ui.components.SenaTextField
import com.example.proyectwin.ui.theme.senaColors
import com.example.proyectwin.ui.viewmodel.ActivationUiState
import com.example.proyectwin.ui.viewmodel.AuthViewModel
import java.net.URLDecoder

/**
 * Activación de la cuenta creada por el administrador. El enlace del correo
 * usa el fragmento `#token=...`; el usuario puede pegar el enlace completo o
 * el código. El token no se registra ni se muestra después de validarlo.
 */
@Composable
fun ActivateAccountScreen(
    tokenInicial: String = "",
    onBackToLogin: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val estado by authViewModel.activationState.collectAsState()
    var entrada by remember { mutableStateOf(tokenInicial) }
    var token by remember { mutableStateOf(extraerToken(tokenInicial)) }
    var password by remember { mutableStateOf("") }
    var confirmacion by remember { mutableStateOf("") }
    var errorLocal by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(tokenInicial) {
        if (tokenInicial.isNotBlank()) {
            token = extraerToken(tokenInicial)
            authViewModel.validarActivacion(token)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(senaColors().background)
            .verticalScroll(rememberScrollState()),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .background(Brush.verticalGradient(colors = listOf(senaColors().header, senaColors().green)))
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.VpnKey, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "Activa tu cuenta",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
            }
            IconButton(
                onClick = onBackToLogin,
                modifier = Modifier.align(Alignment.TopStart).statusBarsPadding().padding(top = 16.dp),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
            }
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .offset(y = (-32).dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SenaCard(elevation = 8.dp) {
                when (val actual = estado) {
                    is ActivationUiState.Success -> {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = senaColors().success, modifier = Modifier.size(44.dp))
                        Text(
                            "Cuenta activada",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = senaColors().text,
                        )
                        Text(
                            "Tu contraseña quedó establecida. Ya puedes iniciar sesión con tu correo electrónico.",
                            style = MaterialTheme.typography.bodySmall,
                            color = senaColors().textSecondary,
                        )
                        SenaButton(text = "IR AL LOGIN", onClick = onBackToLogin, icon = Icons.Default.CheckCircle)
                    }

                    is ActivationUiState.Form -> {
                        Text(
                            "Hola${actual.datos.nombre.takeIf { it.isNotBlank() }?.let { ", $it" } ?: ""}. Define la contraseña con la que entrarás a ProyecTwin.",
                            style = MaterialTheme.typography.bodySmall,
                            color = senaColors().textSecondary,
                        )
                        SenaTextField(
                            value = actual.datos.correo,
                            onValueChange = {},
                            label = "Correo electrónico",
                            leadingIcon = Icons.Default.Email,
                            enabled = false,
                        )
                        SenaTextField(
                            value = password,
                            onValueChange = { password = it; errorLocal = null; authViewModel.limpiarErrorActivacion() },
                            label = "Nueva contraseña",
                            placeholder = "Mínimo $PasswordPolicy.MIN_LENGTH caracteres",
                            isPassword = true,
                            leadingIcon = Icons.Default.Lock,
                        )
                        SenaTextField(
                            value = confirmacion,
                            onValueChange = { confirmacion = it; errorLocal = null; authViewModel.limpiarErrorActivacion() },
                            label = "Confirmar contraseña",
                            isPassword = true,
                            leadingIcon = Icons.Default.CheckCircle,
                        )
                        val error = errorLocal ?: actual.error
                        if (error != null) {
                            SenaAlertBanner(
                                title = "Revisa los datos",
                                message = error,
                                icon = Icons.Default.Error,
                                color = senaColors().danger,
                            )
                        }
                        SenaButton(
                            text = "ACTIVAR MI CUENTA",
                            onClick = {
                                errorLocal = when {
                                    !PasswordPolicy.esValida(password) ->
                                        "La contraseña debe tener al menos $PasswordPolicy.MIN_LENGTH caracteres y no ser demasiado común."
                                    password != confirmacion -> "Las contraseñas no coinciden."
                                    else -> null
                                }
                                if (errorLocal == null) {
                                    authViewModel.activarCuenta(token, password, confirmacion)
                                }
                            },
                            isLoading = actual.enviando,
                            icon = Icons.Default.CheckCircle,
                        )
                    }

                    is ActivationUiState.Invalid -> {
                        SenaAlertBanner(
                            title = "Enlace inválido o vencido",
                            message = actual.message,
                            icon = Icons.Default.Error,
                            color = senaColors().danger,
                        )
                        CampoEnlace(entrada = entrada, onEntrada = { entrada = it; token = extraerToken(it) })
                        SenaButton(
                            text = "REINTENTAR",
                            onClick = { authViewModel.validarActivacion(token) },
                            icon = Icons.Default.VpnKey,
                        )
                    }

                    is ActivationUiState.Error -> {
                        SenaAlertBanner(
                            title = "No pudimos validar el enlace",
                            message = actual.message,
                            icon = Icons.Default.Error,
                            color = senaColors().danger,
                        )
                        CampoEnlace(entrada = entrada, onEntrada = { entrada = it; token = extraerToken(it) })
                        SenaButton(
                            text = "REINTENTAR",
                            onClick = { authViewModel.validarActivacion(token) },
                            icon = Icons.Default.VpnKey,
                        )
                    }

                    ActivationUiState.Validating,
                    ActivationUiState.Idle -> {
                        Text(
                            "Pega el enlace que recibiste por correo o el código de activación.",
                            style = MaterialTheme.typography.bodySmall,
                            color = senaColors().textSecondary,
                        )
                        CampoEnlace(entrada = entrada, onEntrada = { entrada = it; token = extraerToken(it) })
                        SenaButton(
                            text = "VALIDAR ENLACE",
                            onClick = { authViewModel.validarActivacion(token) },
                            isLoading = estado is ActivationUiState.Validating,
                            icon = Icons.Default.VpnKey,
                        )
                        Text(
                            "Si el enlace venció, pide al administrador que reenvíe la activación.",
                            style = MaterialTheme.typography.labelSmall,
                            color = senaColors().textMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
            TextButton(onClick = onBackToLogin, modifier = Modifier.fillMaxWidth()) {
                Text("Volver al login", color = senaColors().green, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CampoEnlace(entrada: String, onEntrada: (String) -> Unit) {
    SenaTextField(
        value = entrada,
        onValueChange = onEntrada,
        label = "Enlace o código de activación",
        placeholder = "Pega el enlace del correo",
        leadingIcon = Icons.Default.VpnKey,
        keyboardType = KeyboardType.Uri,
    )
}

/** Acepta el código plano o un enlace con `#token=...`/`?token=...`. */
internal fun extraerToken(texto: String): String {
    val limpio = texto.trim()
    if (limpio.isEmpty()) return ""
    val match = Regex("[?#&]token=([^&\\s]+)").find(limpio)
    val valor = match?.groupValues?.getOrNull(1) ?: limpio
    return runCatching { URLDecoder.decode(valor, "UTF-8") }.getOrDefault(valor)
}
