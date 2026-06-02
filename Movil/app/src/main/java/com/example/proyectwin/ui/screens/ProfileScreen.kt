package com.example.proyectwin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.proyectwin.ui.components.SenaButton
import com.example.proyectwin.ui.components.SenaCard
import com.example.proyectwin.ui.components.SenaTopBar
import com.example.proyectwin.ui.theme.*

@Composable
fun ProfileScreen(onNavigate: (String) -> Unit) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            SenaTopBar(title = "Mi Perfil", showProfile = false)
        },
        containerColor = SenaBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Header Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(SenaHeader, SenaGreen)
                        )
                    ),
                contentAlignment = Alignment.BottomCenter
            ) {
                Surface(
                    modifier = Modifier
                        .offset(y = 50.dp)
                        .size(100.dp),
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = SenaGreen
                        )
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(30.dp),
                            shape = CircleShape,
                            color = SenaGreen,
                            shadowElevation = 4.dp
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.padding(6.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp))

            Text(
                "Maria Gonzalez",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = SenaText
            )
            Text(
                "Aprendiz - Análisis y desarrollo de Software",
                style = MaterialTheme.typography.bodyMedium,
                color = SenaTextLight
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStatItem("3", "Proyectos")
                ProfileStatItem("12", "Meses")
                ProfileStatItem("85%", "Avance")
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Settings Sections
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProfileSectionTitle("Información Personal")
                SenaCard(onClick = { /* TODO */ }) {
                    ProfileInfoRow(Icons.Default.Badge, "Documento", "1023456789")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SenaBorder)
                    ProfileInfoRow(Icons.Default.Email, "Correo", "maria.gonzalez@sena.edu.co")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SenaBorder)
                    ProfileInfoRow(Icons.Default.Phone, "Teléfono", "3235421165")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SenaBorder)
                    ProfileInfoRow(Icons.Default.School, "Programa", "ADSO")
                }

                ProfileSectionTitle("Formación")
                SenaCard(onClick = { onNavigate("ficha/detail") }) {
                    ProfileInfoRow(Icons.Default.Groups, "Mi Ficha", "ADSO-2568")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Toca para ver detalles de tu ficha", style = MaterialTheme.typography.labelSmall, color = SenaGreen)
                }

                ProfileSectionTitle("Soporte")
                SenaCard(onClick = { onNavigate("report/issue") }) {
                    ProfileInfoRow(Icons.Default.BugReport, "Reportar Falla", "Informar problemas")
                }

                ProfileSectionTitle("Seguridad")
                SenaCard {
                    ProfileInfoRow(Icons.Default.Lock, "Contraseña", "••••••••")
                    Spacer(modifier = Modifier.height(16.dp))
                    SenaButton(
                        text = "Cambiar Contraseña",
                        onClick = { /* TODO */ },
                        isPrimary = false,
                        modifier = Modifier.height(48.dp)
                    )
                }

                SenaButton(
                    text = "Cerrar Sesión",
                    onClick = { /* TODO */ },
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ProfileStatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = SenaGreen, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
    }
}

@Composable
fun ProfileSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = SenaText,
        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
    )
}

@Composable
fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = SenaTextLight, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = SenaText)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProyecTwinTheme {
        ProfileScreen(onNavigate = {})
    }
}
