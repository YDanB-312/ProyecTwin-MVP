package com.example.proyectwin.ui.screens.aprendiz

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectwin.navigation.AppNavigation
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onBack: () -> Unit, onNavigate: (String) -> Unit) {
    val scrollState = rememberScrollState()
    var isEditing by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("Maria") }
    var lastName by remember { mutableStateOf("Gonzalez") }
    var email by remember { mutableStateOf("maria.gonzalez@sena.edu.co") }

    Scaffold(
        topBar = {
            SenaTopBar(
                title = "Mi Perfil", 
                showProfile = false, 
                showNotifications = true,
                onBack = onBack,
                onNavigateToAlerts = { onNavigate(AppNavigation.APRENDIZ_ALERTS) }
            )
        },
        containerColor = SenaBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            // --- HEADER APRENDIZ (GRADIENTE AMIGABLE) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(SenaHeader, SenaGreen)
                        )
                    )
            ) {
                // Decoración círculo
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .offset(x = 280.dp, y = 80.dp)
                        .background(Color.White.copy(alpha = 0.05f), CircleShape)
                )
            }

            // --- PERFIL CARD (FLOTANTE) ---
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .offset(y = (-70).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 10.dp
                ) {
                    Box(
                        modifier = Modifier
                            .padding(5.dp)
                            .fillMaxSize()
                            .background(SenaGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${name.take(1)}${lastName.take(1)}",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "$name $lastName",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = SenaText
                )
                Text(
                    text = "Aprendiz ADSO — Trimestre 3",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SenaTextLight
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Métricas Aprendiz
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCardAprendiz(icon = Icons.Default.Folder, value = "3", label = "Proyectos", modifier = Modifier.weight(1f))
                    MetricCardAprendiz(icon = Icons.Default.CalendarToday, value = "12", label = "Meses", modifier = Modifier.weight(1f))
                    Surface(
                        modifier = Modifier.weight(0.8f).height(80.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = SenaSuccess.copy(alpha = 0.1f)
                    ) {
                        Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Activo", fontWeight = FontWeight.Bold, color = SenaSuccess, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // --- SECCIONES ---
                SenaSectionHeader(title = "Datos del Aprendiz")
                SenaCard(elevation = 1.dp) {
                    if (isEditing) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                SenaTextField(value = name, onValueChange = { name = it }, label = "Nombre", modifier = Modifier.weight(1f))
                                SenaTextField(value = lastName, onValueChange = { lastName = it }, label = "Apellido", modifier = Modifier.weight(1f))
                            }
                            SenaTextField(value = email, onValueChange = { email = it }, label = "Correo Institucional", leadingIcon = Icons.Default.Email)
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                SenaButton(text = "Cerrar", onClick = { isEditing = false }, isPrimary = false, modifier = Modifier.weight(1f))
                                SenaButton(text = "Guardar", onClick = { isEditing = false }, modifier = Modifier.weight(1f))
                            }
                        }
                    } else {
                        Column {
                            SenaSettingsItem(icon = Icons.Default.Person, title = "Nombre Completo", description = "$name $lastName")
                            HorizontalDivider(color = SenaBorderSoft, modifier = Modifier.padding(start = 56.dp))
                            SenaSettingsItem(icon = Icons.Default.Email, title = "Correo Electrónico", description = email)
                            HorizontalDivider(color = SenaBorderSoft, modifier = Modifier.padding(start = 56.dp))
                            SenaSettingsItem(icon = Icons.Default.Badge, title = "Documento de Identidad", description = "1.023.456.789")
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            SenaButton(text = "Editar Perfil", onClick = { isEditing = true }, isPrimary = false, icon = Icons.Default.Edit, modifier = Modifier.height(44.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                SenaSectionHeader(title = "Privacidad y Acceso")
                SenaCard(elevation = 1.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SenaAlertBanner(
                            title = "Seguridad",
                            message = "Se recomienda actualizar tu clave periódicamente.",
                            icon = Icons.Default.Lock,
                            color = SenaInfo
                        )

                        SenaSettingsItem(
                            icon = Icons.Default.VpnKey, 
                            title = "Cambiar Contraseña", 
                            description = "Gestión de credenciales",
                            onClick = { onNavigate(AppNavigation.RESET_PASSWORD) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                SenaSectionHeader(title = "Notificaciones")
                SenaCard(elevation = 1.dp) {
                    Column {
                        var notifSimilitud by remember { mutableStateOf(true) }
                        var notifComentarios by remember { mutableStateOf(true) }
                        
                        SenaSettingsItem(
                            icon = Icons.Default.NotificationsActive, 
                            title = "Similitudes", 
                            description = "Aviso de coincidencias",
                            trailing = { Switch(checked = notifSimilitud, onCheckedChange = { notifSimilitud = it }, colors = SwitchDefaults.colors(checkedTrackColor = SenaGreen)) }
                        )
                        HorizontalDivider(color = SenaBorderSoft, modifier = Modifier.padding(start = 56.dp))
                        SenaSettingsItem(
                            icon = Icons.AutoMirrored.Filled.Chat, 
                            title = "Comentarios", 
                            description = "Retroalimentación técnica",
                            trailing = { Switch(checked = notifComentarios, onCheckedChange = { notifComentarios = it }, colors = SwitchDefaults.colors(checkedTrackColor = SenaGreen)) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                SenaButton(
                    text = "Cerrar Sesión",
                    onClick = { onNavigate(AppNavigation.HOME) },
                    icon = Icons.AutoMirrored.Filled.Logout,
                    containerColor = SenaDanger,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun MetricCardAprendiz(icon: ImageVector, value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, SenaBorderSoft)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = SenaGreen, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.Black, fontSize = 18.sp, color = SenaText)
            Text(label, style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProyecTwinTheme {
        ProfileScreen(onBack = {}, onNavigate = {})
    }
}
