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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*

/**
 * Confirmación del envío de recuperación. En Android el enlace del correo
 * apunta al frontend web, por lo que aquí se ofrece continuar pegando el
 * código (token) directamente en la app. En modo local, `reset_url` permite
 * prellenarlo.
 */
@Composable
fun ConfirmationScreen(
    correo: String,
    resetUrl: String?,
    onGoToLogin: () -> Unit,
    onEnterCode: (correo: String, token: String) -> Unit,
) {
    val token = resetUrl?.let { runCatching { Uri.parse(it).getQueryParameter("token") }.getOrNull() }.orEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(senaColors().background)
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(Brush.verticalGradient(colors = listOf(senaColors().header, senaColors().green)))
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
                        Icon(Icons.Default.MarkEmailRead, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text("Revisa tu correo", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .offset(y = (-32).dp)
        ) {
            SenaCard(elevation = 8.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Text(
                        "Si el correo está registrado, enviamos un enlace para restablecer tu contraseña.",
                        style = MaterialTheme.typography.bodySmall,
                        color = senaColors().textSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (correo.isNotBlank()) {
                        Text(
                            correo,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = senaColors().text,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (resetUrl != null) {
                        SenaAlertBanner(
                            title = "Modo local",
                            message = "El servidor devolvió el enlace directamente porque estás en desarrollo.",
                            icon = Icons.Default.Info,
                            color = senaColors().info
                        )
                    }

                    SenaButton(
                        text = "INGRESAR CÓDIGO",
                        onClick = { onEnterCode(correo, token) },
                        icon = Icons.Default.VpnKey
                    )

                    SenaButton(
                        text = "VOLVER AL INICIO",
                        onClick = onGoToLogin,
                        isPrimary = false,
                        icon = Icons.AutoMirrored.Filled.ArrowBack
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
