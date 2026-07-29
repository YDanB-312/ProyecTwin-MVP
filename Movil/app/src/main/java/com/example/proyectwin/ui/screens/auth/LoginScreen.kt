package com.example.proyectwin.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*
import com.example.proyectwin.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val uiState by authViewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isLoggedIn, uiState.user) {
        if (uiState.isLoggedIn && uiState.user != null) {
            onLoginSuccess(uiState.user!!.role)
        }
    }

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
                .background(Brush.verticalGradient(colors = listOf(Color(0xFF0F172A), senaColors().header)))
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
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            tint = senaColors().accent,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
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
                        value = email,
                        onValueChange = { email = it; authViewModel.clearError() },
                        label = "Correo Institucional",
                        placeholder = "ejemplo@sena.edu.co",
                        leadingIcon = Icons.Default.AlternateEmail
                    )

                    SenaTextField(
                        value = password,
                        onValueChange = { password = it; authViewModel.clearError() },
                        label = "Contraseña",
                        placeholder = "••••••••",
                        isPassword = true,
                        leadingIcon = Icons.Default.LockPerson
                    )

                    if (uiState.error != null) {
                        SenaAlertBanner(
                            title = "Error",
                            message = uiState.error!!,
                            icon = Icons.Default.Error,
                            color = senaColors().danger
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = SwitchDefaults.colors(checkedTrackColor = senaColors().green),
                                modifier = Modifier.scale(0.8f)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Recordarme", style = MaterialTheme.typography.bodySmall, color = senaColors().textSecondary)
                        }
                        TextButton(onClick = onForgotPasswordClick) {
                            Text("Olvidé mi clave", style = MaterialTheme.typography.labelSmall, color = senaColors().green, fontWeight = FontWeight.Black)
                        }
                    }

                    SenaButton(
                        text = "ENTRAR AL SISTEMA",
                        onClick = { authViewModel.login(email, password) },
                        isLoading = uiState.isLoading,
                        icon = Icons.AutoMirrored.Filled.Login,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("¿No tienes cuenta?", style = MaterialTheme.typography.bodySmall, color = senaColors().textLight)
                        TextButton(onClick = onRegisterClick) {
                            Text("Regístrate aquí", color = senaColors().green, fontWeight = FontWeight.Black, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, senaColors().border)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("ACCESO RÁPIDO (DEMO)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = senaColors().textMuted)
                    Text("Usa: aprendiz@test.com • instructor@test.com • admin@test.com", style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
                    Text("Contraseña: 123456", style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)

                    QuickAccessRow("Aprendiz (aprendiz@test.com)", senaColors().success) { email = "aprendiz@test.com"; password = "123456" }
                    QuickAccessRow("Instructor (instructor@test.com)", senaColors().info) { email = "instructor@test.com"; password = "123456" }
                    QuickAccessRow("Admin (admin@test.com)", senaColors().warning) { email = "admin@test.com"; password = "123456" }
                }
            }
        }

        Spacer(Modifier.height(40.dp))
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
        LoginScreen(onLoginSuccess = {}, onRegisterClick = {}, onForgotPasswordClick = {})
    }
}
