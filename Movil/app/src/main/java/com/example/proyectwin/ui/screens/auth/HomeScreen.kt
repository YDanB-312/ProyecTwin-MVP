package com.example.proyectwin.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
fun HomeScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val scrollState = rememberScrollState()
    val uiState by authViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        if (uiState is AuthUiState.LoggedIn) {
            // Redirect handled by AppNavigation checking initial route
        }
    }

    if (uiState is AuthUiState.Loading) {
        SenaLoadingState(message = "Verificando sesión...")
        return
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
                .height(280.dp)
                .background(Brush.verticalGradient(colors = listOf(senaColors().header, senaColors().green)))
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    "Bienvenido a ProyecTwin",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Text(
                    "Plataforma inteligente de gestión y similitudes",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Column(
            modifier = Modifier
                .padding(24.dp)
                .offset(y = (-40).dp)
        ) {
            SenaCard(elevation = 8.dp) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Optimiza tus proyectos de formación del SENA",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = senaColors().text,
                        textAlign = TextAlign.Center
                    )

                    SenaButton(
                        text = "Ingresar Ahora",
                        onClick = onLoginClick,
                        icon = Icons.Default.Login
                    )

                    SenaButton(
                        text = "Crear Cuenta",
                        onClick = onRegisterClick,
                        isPrimary = false,
                        icon = Icons.Default.PersonAdd
                    )
                }
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                "Acceso por Rol",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = senaColors().text
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                HomeRoleCard(
                    title = "Aprendiz",
                    icon = Icons.Default.School,
                    desc = "Gestiona tus proyectos y recibe feedback.",
                    color = senaColors().green,
                    modifier = Modifier.weight(1f)
                )
                HomeRoleCard(
                    title = "Instructor",
                    icon = Icons.Default.SupervisorAccount,
                    desc = "Evalúa y supervisa el progreso.",
                    color = senaColors().accent,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                "¿Cómo funciona?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = senaColors().text
            )

            HomeStepItem(1, "Regístrate", "Crea tu cuenta según tu rol institucional.")
            HomeStepItem(2, "Sube tu Proyecto", "Registra tu propuesta y analiza similitudes.")
            HomeStepItem(3, "Mejora", "Recibe retroalimentación y evoluciona tu idea.")
        }

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
fun HomeRoleCard(title: String, icon: ImageVector, desc: String, color: Color, modifier: Modifier = Modifier) {
    SenaCard(modifier = modifier, elevation = 2.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = color.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = senaColors().text)
            Spacer(Modifier.height(4.dp))
            Text(desc, style = MaterialTheme.typography.labelSmall, color = senaColors().textSecondary, textAlign = TextAlign.Center, lineHeight = 14.sp)
        }
    }
}

@Composable
fun HomeStepItem(number: Int, title: String, desc: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = CircleShape,
            color = senaColors().header
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(number.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = senaColors().text)
            Text(desc, style = MaterialTheme.typography.labelSmall, color = senaColors().textSecondary)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    ProyecTwinTheme {
        HomeScreen(onLoginClick = {}, onRegisterClick = {})
    }
}
