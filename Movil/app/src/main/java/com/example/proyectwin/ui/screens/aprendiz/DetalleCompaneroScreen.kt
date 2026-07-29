package com.example.proyectwin.ui.screens.aprendiz

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleCompaneroScreen(
    nombre: String,
    iniciales: String,
    estado: String,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    var showLightbox by remember { mutableStateOf(false) }

    if (showLightbox) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.9f)).clickable { showLightbox = false },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                SenaAvatar(
                    fotoBase64 = null,
                    nombre = nombre,
                    modifier = Modifier.size(200.dp)
                )
                Spacer(Modifier.height(20.dp))
                Text(nombre, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Aprendiz • $estado", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
            }
            IconButton(
                onClick = { showLightbox = false },
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White, modifier = Modifier.size(28.dp))
            }
        }
    }

    Scaffold(
        topBar = {
            SenaTopBar(
                title = "Perfil Compañero",
                onBack = onBack,
                showProfile = true,
                showNotifications = true
            )
        },
        containerColor = senaColors().background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            // Header con Degradado (Similar a Perfil)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Brush.verticalGradient(colors = listOf(senaColors().header, senaColors().green)))
            )

            // Tarjeta Flotante
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .offset(y = (-60).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(100.dp).clickable { showLightbox = true },
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Box(
                        modifier = Modifier.padding(4.dp).fillMaxSize().background(senaColors().green, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(iniciales, color = Color.White, fontWeight = FontWeight.Black, fontSize = 32.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(nombre, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = senaColors().text)
                SenaStatusBadge(status = estado)

                Spacer(modifier = Modifier.height(32.dp))

                SenaSectionHeader(title = "Información Académica")
                SenaCard(elevation = 1.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        DetailRow(Icons.Default.School, "Programa", "Análisis y Desarrollo 2568")
                        HorizontalDivider(color = senaColors().borderSoft)
                        DetailRow(Icons.Default.Badge, "Ficha", "ADSO-2568")
                        HorizontalDivider(color = senaColors().borderSoft)
                        DetailRow(Icons.Default.Mail, "Contacto", "${nombre.replace(" ", ".").lowercase()}@soy.sena.edu.co")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                SenaButton(
                    text = "Volver a Mi Ficha",
                    onClick = onBack,
                    isPrimary = false,
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetalleCompaneroScreenPreview() {
    ProyecTwinTheme {
        DetalleCompaneroScreen(
            nombre = "Juan Pérez",
            iniciales = "JP",
            estado = "Activo",
            onBack = {}
        )
    }
}
