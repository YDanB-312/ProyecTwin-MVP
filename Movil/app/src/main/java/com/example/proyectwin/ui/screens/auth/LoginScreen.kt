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
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    val infiniteTransition = rememberInfiniteTransition(label = "login_bg")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(3000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "glow"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SenaBackground)
            .verticalScroll(scrollState)
    ) {
        // --- PREMIUM LOGIN HEADER ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Brush.verticalGradient(colors = listOf(Color(0xFF0F172A), SenaHeader)))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            // Decorative elements
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 40.dp, y = (-40).dp)
                    .alpha(glowAlpha)
                    .background(SenaGreen.copy(alpha = 0.1f), CircleShape)
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
                            tint = SenaAccent, 
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
                    color = SenaAccent,
                    letterSpacing = 4.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // --- LOGIN FORM CARD ---
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .offset(y = (-40).dp)
        ) {
            SenaCard(elevation = 12.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    SenaTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Correo Institucional",
                        placeholder = "ejemplo@sena.edu.co",
                        leadingIcon = Icons.Default.AlternateEmail
                    )

                    SenaTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Contraseña",
                        placeholder = "••••••••",
                        isPassword = true,
                        leadingIcon = Icons.Default.LockPerson
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = SwitchDefaults.colors(checkedTrackColor = SenaGreen),
                                modifier = Modifier.scale(0.8f)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Recordarme", style = MaterialTheme.typography.bodySmall, color = SenaTextSecondary)
                        }
                        TextButton(onClick = onForgotPasswordClick) {
                            Text("Olvidé mi clave", style = MaterialTheme.typography.labelSmall, color = SenaGreen, fontWeight = FontWeight.Black)
                        }
                    }

                    SenaButton(
                        text = "ENTRAR AL SISTEMA",
                        onClick = {
                            isLoading = true
                            scope.launch {
                                delay(1200)
                                onLoginSuccess()
                            }
                        },
                        isLoading = isLoading,
                        icon = Icons.AutoMirrored.Filled.Login,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Matches the "Crear cuenta" flow
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("¿No tienes cuenta?", style = MaterialTheme.typography.bodySmall, color = SenaTextLight)
                        TextButton(onClick = onRegisterClick) {
                            Text("Regístrate aquí", color = SenaGreen, fontWeight = FontWeight.Black, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // --- QUICK ACCESS (DEVELOPMENT) ---
            Spacer(Modifier.height(24.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, SenaBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("ACCESO RÁPIDO (DEMO)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = SenaTextMuted)
                    
                    QuickAccessRow("Aprendiz", SenaSuccess) { email = "maria.gonzalez@soy.sena.edu.co" }
                    QuickAccessRow("Instructor", SenaInfo) { email = "carlos.ruiz@sena.edu.co" }
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
            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = SenaText)
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
