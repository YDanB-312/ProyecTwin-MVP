package com.example.proyectwin.ui.screens.auth

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
import androidx.compose.ui.draw.clip
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
fun ResetPasswordScreen(
    onBackToLogin: () -> Unit,
    onResetSuccess: () -> Unit,
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SenaBackground)
            .verticalScroll(scrollState)
    ) {
        // Decorative Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Brush.verticalGradient(colors = listOf(SenaHeader, SenaGreen)))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.LockReset, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text("Nueva Contraseña", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
            }
            IconButton(
                onClick = onBackToLogin,
                modifier = Modifier.align(Alignment.TopStart).padding(top = 16.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
            }
        }

        // Card Section
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .offset(y = (-32).dp)
        ) {
            SenaCard(elevation = 8.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    Text(
                        "Crea una nueva contraseña segura para tu cuenta. Asegúrate de que sea diferente a las anteriores.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SenaTextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    SenaTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Contraseña Nueva",
                        placeholder = "Mínimo 8 caracteres",
                        isPassword = true,
                        leadingIcon = Icons.Default.Lock
                    )

                    SenaTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = "Confirmar Contraseña",
                        placeholder = "Escribe de nuevo tu clave",
                        isPassword = true,
                        leadingIcon = Icons.Default.CheckCircle
                    )

                    SenaButton(
                        text = "Actualizar Contraseña",
                        onClick = {
                            isLoading = true
                            scope.launch {
                                delay(1500)
                                isLoading = false
                                onResetSuccess()
                            }
                        },
                        isLoading = isLoading,
                        icon = Icons.Default.Save
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ResetPasswordScreenPreview() {
    ProyecTwinTheme {
        ResetPasswordScreen(onBackToLogin = {}, onResetSuccess = {})
    }
}
