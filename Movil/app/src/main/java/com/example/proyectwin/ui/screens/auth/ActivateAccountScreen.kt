package com.example.proyectwin.ui.screens.auth

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.proyectwin.domain.util.PasswordPolicy
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*
import com.example.proyectwin.ui.viewmodel.ActivationState
import com.example.proyectwin.ui.viewmodel.AuthViewModel

/**
 * Activación de una cuenta creada por el administrador. El token llega por
 * correo; la app permite pegarlo (no hay App Links configurados) y valida
 * antes de mostrar el formulario, igual que la web.
 */
@Composable
fun ActivateAccountScreen(
    onBackToLogin: () -> Unit,
    onActivated: () -> Unit,
    tokenInicial: String = "",
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    var token by remember { mutableStateOf(tokenInicial) }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorLocal by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()
    val estado by authViewModel.activationState.collectAsState()

    LaunchedEffect(Unit) {
        if (tokenInicial.isNotBlank()) authViewModel.validateActivation(extraerTokenActivacion(tokenInicial))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(senaColors().background)
            .verticalScroll(scrollState),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
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
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp),
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "Activar Cuenta",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
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
        ) {
            SenaCard(elevation = 8.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Text(
                        "Pega el código del correo de activación. Después define la contraseña con la que ingresarás.",
                        style = MaterialTheme.typography.bodySmall,
                        color = senaColors().textSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                    )

                    when (val actual = estado) {
                        is ActivationState.Success -> {
                            SenaAlertBanner(
                                title = "Cuenta activada",
                                message = actual.message,
                                icon = Icons.Default.CheckCircle,
                                color = senaColors().success,
                            )
                            SenaButton(
                                text = "Ir a iniciar sesión",
                                onClick = {
                                    authViewModel.resetActivationState()
                                    onActivated()
                                },
                                icon = Icons.AutoMirrored.Filled.Login,
                            )
                        }

                        is ActivationState.Ready -> {
                            SenaCard(elevation = 0.dp) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        actual.info.nombre.ifBlank { "Cuenta pendiente" },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        actual.info.correo,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = senaColors().textSecondary,
                                    )
                                }
                            }

                            SenaTextField(
                                value = password,
                                onValueChange = { password = it; errorLocal = null },
                                label = "Contraseña definitiva",
                                placeholder = "Mínimo ${PasswordPolicy.MIN_LENGTH} caracteres",
                                isPassword = true,
                                leadingIcon = Icons.Default.Lock,
                            )
                            SenaTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it; errorLocal = null },
                                label = "Confirmar contraseña",
                                placeholder = "Escribe de nuevo tu clave",
                                isPassword = true,
                                leadingIcon = Icons.Default.CheckCircle,
                            )

                            errorLocal?.let {
                                SenaAlertBanner(
                                    title = "Revisa los datos",
                                    message = it,
                                    icon = Icons.Default.Error,
                                    color = senaColors().danger,
                                )
                            }

                            SenaButton(
                                text = "Activar cuenta",
                                onClick = {
                                    errorLocal = when {
                                        !PasswordPolicy.esValida(password) ->
                                            "La contraseña debe tener al menos ${PasswordPolicy.MIN_LENGTH} caracteres y no ser demasiado común."
                                        password != confirmPassword ->
                                            "La confirmación no coincide con la contraseña."
                                        else -> null
                                    }
                                    if (errorLocal == null) {
                                        authViewModel.activateAccount(
                                            extraerTokenActivacion(token),
                                            password,
                                            confirmPassword,
                                        )
                                    }
                                },
                                isLoading = estado is ActivationState.Activating,
                                icon = Icons.Default.CheckCircle,
                            )
                        }

                        else -> {
                            SenaTextField(
                                value = token,
                                onValueChange = {
                                    token = it
                                    errorLocal = null
                                    if (estado !is ActivationState.Validating) {
                                        authViewModel.resetActivationState()
                                    }
                                },
                                label = "Código de activación",
                                placeholder = "Pega aquí el código del correo",
                                leadingIcon = Icons.Default.VpnKey,
                            )

                            if (actual is ActivationState.Invalid) {
                                SenaAlertBanner(
                                    title = "Enlace inválido",
                                    message = actual.message,
                                    icon = Icons.Default.Error,
                                    color = senaColors().danger,
                                )
                            }
                            if (actual is ActivationState.Error) {
                                SenaAlertBanner(
                                    title = "Error",
                                    message = actual.message,
                                    icon = Icons.Default.Error,
                                    color = senaColors().danger,
                                )
                            }

                            SenaButton(
                                text = "Validar código",
                                onClick = {
                                    errorLocal = if (token.isBlank()) {
                                        "Ingresa el código que llegó a tu correo."
                                    } else null
                                    if (errorLocal == null) {
                                        authViewModel.validateActivation(extraerTokenActivacion(token))
                                    }
                                },
                                isLoading = estado is ActivationState.Validating,
                                icon = Icons.Default.Search,
                            )
                        }
                    }

                    Text(
                        "Si el enlace venció, solicita al administrador que reenvíe la activación.",
                        style = MaterialTheme.typography.labelSmall,
                        color = senaColors().textLight,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

/** Acepta el token pegado o el enlace completo (`?token=` o `#token=`). */
private fun extraerTokenActivacion(texto: String): String {
    val limpio = texto.trim()
    if (limpio.isBlank()) return ""
    return runCatching {
        val uri = Uri.parse(limpio)
        uri.getQueryParameter("token")
            ?: uri.fragment
                ?.substringAfter("token=", "")
                ?.substringBefore("&")
                ?.takeIf { it.isNotBlank() }
            ?: limpio
    }.getOrDefault(limpio)
}
