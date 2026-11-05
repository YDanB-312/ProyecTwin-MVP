package com.example.proyectwin.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.proyectwin.ui.components.SenaButton
import com.example.proyectwin.ui.components.SenaCard
import com.example.proyectwin.ui.theme.senaColors
import com.example.proyectwin.ui.viewmodel.AuthViewModel

/** 403 de rol: la cuenta está autenticada pero no tiene un rol reconocido. */
@Composable
fun AccessDeniedScreen(
    onLogout: () -> Unit,
    onGoToLogin: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(senaColors().background, senaColors().header))),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        SenaCard(elevation = 8.dp) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Surface(
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    color = senaColors().danger.copy(alpha = 0.1f),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Block,
                            contentDescription = null,
                            tint = senaColors().danger,
                            modifier = Modifier.size(36.dp),
                        )
                    }
                }
                Text(
                    "403 · Acceso denegado",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    "Tu cuenta no tiene un rol habilitado para entrar a ProyecTwin. Contacta al administrador o inicia sesión con otra cuenta.",
                    style = MaterialTheme.typography.bodySmall,
                    color = senaColors().textSecondary,
                    textAlign = TextAlign.Center,
                )
                SenaButton(
                    text = "Cerrar sesión",
                    onClick = {
                        authViewModel.logout()
                        onLogout()
                    },
                    icon = Icons.Default.Logout,
                )
                TextButton(onClick = onGoToLogin) {
                    Text("Volver al inicio de sesión", color = senaColors().green)
                }
            }
        }
    }
}
