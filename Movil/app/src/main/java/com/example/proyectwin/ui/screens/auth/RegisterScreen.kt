package com.example.proyectwin.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*
import com.example.proyectwin.ui.viewmodel.AuthUiState
import com.example.proyectwin.ui.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    onBackToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("aprendiz") }
    val scrollState = rememberScrollState()
    val uiState by authViewModel.uiState.collectAsState()
    val isSubmitting by authViewModel.isSubmitting.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.LoggedIn) {
            onRegisterSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(senaColors().background)
            .verticalScroll(scrollState)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(Brush.verticalGradient(colors = listOf(Color(0xFF0F172A), senaColors().header)))
                .padding(24.dp)
        ) {
            IconButton(
                onClick = onBackToLogin,
                modifier = Modifier.align(Alignment.TopStart).padding(top = 8.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Nueva Misión",
                    style = MaterialTheme.typography.labelMedium,
                    color = senaColors().accent,
                    letterSpacing = 4.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "CREAR CUENTA",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .offset(y = (-40).dp)
        ) {
            SenaCard(elevation = 12.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(28.dp)) {

                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            SenaTextField(value = name, onValueChange = { name = it }, label = "Nombre", modifier = Modifier.weight(1f), placeholder = "Ej: Maria")
                            SenaTextField(value = lastName, onValueChange = { lastName = it }, label = "Apellido", modifier = Modifier.weight(1f), placeholder = "Ej: Gonzalez")
                        }
                        SenaTextField(value = email, onValueChange = { email = it }, label = "Correo Institucional", leadingIcon = Icons.Default.Email, placeholder = "tu@sena.edu.co")
                        SenaTextField(value = password, onValueChange = { password = it }, label = "Contraseña de Acceso", isPassword = true, leadingIcon = Icons.Default.Lock)
                    }

                    if (uiState is AuthUiState.Error) {
                        SenaAlertBanner(
                            title = "Error",
                            message = (uiState as AuthUiState.Error).message,
                            icon = Icons.Default.Error,
                            color = senaColors().danger
                        )
                    }

                    HorizontalDivider(color = senaColors().borderSoft)

                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("IDENTIFICACIÓN DE ROL", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = senaColors().textMuted, letterSpacing = 1.sp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            PremiumRoleCard(
                                title = "Aprendiz",
                                icon = Icons.Default.School,
                                isSelected = selectedRole == "aprendiz",
                                onClick = { selectedRole = "aprendiz" },
                                modifier = Modifier.weight(1f)
                            )
                            PremiumRoleCard(
                                title = "Instructor",
                                icon = Icons.Default.SupervisorAccount,
                                isSelected = selectedRole == "instructor",
                                onClick = { selectedRole = "instructor" },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    SenaButton(
                        text = "FINALIZAR REGISTRO",
                        onClick = {
                            val fullName = "$name $lastName".trim()
                            if (fullName.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
                                authViewModel.register(fullName, email, password, selectedRole)
                            }
                        },
                        isLoading = isSubmitting,
                        icon = Icons.Default.HowToReg,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            TextButton(
                onClick = onBackToLogin,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("¿YA TIENES CUENTA? INICIA SESIÓN", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = senaColors().textLight)
            }
        }

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
fun PremiumRoleCard(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(100.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() },
        color = if (isSelected) senaColors().green.copy(alpha = 0.05f) else Color.White,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) senaColors().green else senaColors().border
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isSelected) senaColors().green else senaColors().textMuted,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                color = if (isSelected) senaColors().green else senaColors().textSecondary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    ProyecTwinTheme {
        RegisterScreen(onBackToLogin = {}, onRegisterSuccess = {})
    }
}
