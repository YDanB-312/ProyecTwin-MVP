package com.example.proyectwin.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*

@Composable
fun NotFoundScreen(
    onGoHome: () -> Unit,
    onGoLogin: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(senaColors().background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                color = senaColors().green.copy(alpha = 0.05f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("404", fontSize = 48.sp, fontWeight = FontWeight.Black, color = senaColors().green)
                }
            }
            
            Spacer(Modifier.height(32.dp))
            
            Text(
                "¡Uy! Página No Encontrada",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = senaColors().text,
                textAlign = TextAlign.Center
            )
            
            Spacer(Modifier.height(12.dp))
            
            Text(
                "La página que buscas no existe o ha sido movida temporalmente. Verifica la dirección o regresa al inicio.",
                style = MaterialTheme.typography.bodyMedium,
                color = senaColors().textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 280.dp)
            )
            
            Spacer(Modifier.height(48.dp))
            
            SenaButton(
                text = "Volver al Inicio",
                onClick = onGoHome,
                icon = Icons.Default.Home
            )
            
            Spacer(Modifier.height(16.dp))
            
            SenaButton(
                text = "Iniciar Sesión",
                onClick = onGoLogin,
                isPrimary = false,
                icon = Icons.AutoMirrored.Filled.Login
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotFoundScreenPreview() {
    ProyecTwinTheme {
        NotFoundScreen(onGoHome = {}, onGoLogin = {})
    }
}
