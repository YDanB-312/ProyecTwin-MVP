package com.example.proyectwin.ui.screens.auth

import android.util.Patterns
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.proyectwin.R
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.screens.ManualPdfDialog
import com.example.proyectwin.ui.theme.*
import com.example.proyectwin.ui.viewmodel.AuthUiState
import com.example.proyectwin.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onForgotPasswordClick: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorLocal by remember { mutableStateOf<String?>(null) }
    var mostrarManualPdf by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val uiState by authViewModel.uiState.collectAsState()
    val isSubmitting by authViewModel.isSubmitting.collectAsState()
    // La navegación tras el login (rol + must_change_password) la decide
    // MainActivity observando el estado de sesión.

    val infiniteTransition = rememberInfiniteTransition(label = "login_bg")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(3000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "glow"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(senaColors().background)
            .verticalScroll(scrollState)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Brush.verticalGradient(colors = listOf(senaColors().background, senaColors().header)))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 40.dp, y = (-40).dp)
                    .alpha(glowAlpha)
                    .background(senaColors().green.copy(alpha = 0.1f), CircleShape)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Logo oficial de ProyecTwin (sin filtros ni rediseño).
                Image(
                    painter = painterResource(id = R.drawable.logo_proyectwin),
                    contentDescription = "Logo de ProyecTwin",
                    modifier = Modifier
                        .width(200.dp)
                        .aspectRatio(656f / 380f),
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "ProyecTwin",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = (-1).sp
                )
                Text(
                    "ACCESO A PLATAFORMA",
                    style = MaterialTheme.typography.labelMedium,
                    color = senaColors().accent,
                    letterSpacing = 4.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .offset(y = (-40).dp)
        ) {
            SenaCard(elevation = 12.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    SenaTextField(
                        value = correo,
                        onValueChange = { correo = it; errorLocal = null; authViewModel.clearError() },
                        label = "Correo electrónico",
                        placeholder = "correo@dominio.com",
                        leadingIcon = Icons.Default.Email,
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Email
                    )

                    SenaTextField(
                        value = password,
                        onValueChange = { password = it; errorLocal = null; authViewModel.clearError() },
                        label = "Contraseña",
                        placeholder = "••••••••",
                        isPassword = true,
                        leadingIcon = Icons.Default.LockPerson
                    )

                    errorLocal?.let {
                        SenaAlertBanner(
                            title = "Revisa los datos",
                            message = it,
                            icon = Icons.Default.Error,
                            color = senaColors().danger
                        )
                    }

                    if (uiState is AuthUiState.Error) {
                        val error = uiState as AuthUiState.Error
                        val mensaje = if (error.code == "cuenta_no_activada") {
                            "Tu cuenta aún no está habilitada. Contacta al administrador."
                        } else {
                            error.message
                        }
                        SenaAlertBanner(
                            title = "Error",
                            message = mensaje,
                            icon = Icons.Default.Error,
                            color = senaColors().danger
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onForgotPasswordClick) {
                            Text("Olvidé mi clave", style = MaterialTheme.typography.labelSmall, color = senaColors().green, fontWeight = FontWeight.Black)
                        }
                    }

                    TextButton(
                        onClick = { mostrarManualPdf = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            "Manual de usuario (PDF)",
                            style = MaterialTheme.typography.labelSmall,
                            color = senaColors().green,
                            fontWeight = FontWeight.Black,
                        )
                    }

                    SenaButton(
                        text = "ENTRAR AL SISTEMA",
                        onClick = {
                            errorLocal = when {
                                correo.isBlank() -> "Ingresa tu correo electrónico."
                                !Patterns.EMAIL_ADDRESS.matcher(correo.trim()).matches() -> "Ingresa un correo válido."
                                password.isBlank() -> "Ingresa tu contraseña."
                                else -> null
                            }
                            if (errorLocal == null) authViewModel.login(correo, password)
                        },
                        isLoading = isSubmitting,
                        icon = Icons.AutoMirrored.Filled.Login,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        "¿No tienes cuenta? Solicítala al administrador de tu centro.",
                        style = MaterialTheme.typography.bodySmall,
                        color = senaColors().textLight,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

        }

        Spacer(Modifier.height(40.dp))
    }

    if (mostrarManualPdf) {
        ManualPdfDialog(onDismiss = { mostrarManualPdf = false })
    }
}

@Composable
fun QuickAccessRow(label: String, color: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = color.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
            Spacer(Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = senaColors().text)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.TouchApp, contentDescription = null, tint = color.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    ProyecTwinTheme {
        LoginScreen(onForgotPasswordClick = {})
    }
}
