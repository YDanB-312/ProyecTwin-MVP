package com.example.proyectwin.ui.screens.instructor

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
fun InstructorProfileScreen(onBack: () -> Unit, onNavigate: (String) -> Unit) {
    val scrollState = rememberScrollState()
    var isEditing by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("Carlos") }
    var lastName by remember { mutableStateOf("Ruiz") }
    var email by remember { mutableStateOf("carlos.ruiz@sena.edu.co") }
    var commentTemplate by remember { mutableStateOf("Estimado aprendiz,\n\nHe revisado tu proyecto y tengo los siguientes comentarios:\n\nAspectos positivos:\n-\n\nAspectos a mejorar:\n-\n\nRecomendaciones:\n-") }

    Scaffold(
        topBar = {
            SenaTopBar(
                title = "Perfil Instructor",
                onBack = onBack,
                showProfile = false,
                showNotifications = true,
                onNavigateToAlerts = { onNavigate(AppNavigation.INSTRUCTOR_ALERTS) }
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
            // --- HEADER PREMIUM ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(SenaHeader, SenaGreen.copy(alpha = 0.8f))
                        )
                    )
            ) {
                // Decoración abstracta sutil
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .offset(x = (-40).dp, y = (-20).dp)
                        .background(Color.White.copy(alpha = 0.05f), CircleShape)
                )
            }

            // --- PERFIL CARD (FLOTANTE) ---
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .offset(y = (-80).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(110.dp),
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 12.dp
                ) {
                    Box(
                        modifier = Modifier
                            .padding(6.dp)
                            .fillMaxSize()
                            .background(SenaGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.SupervisorAccount,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(52.dp)
                        )
                        // Badge de Cámara
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(32.dp),
                            shape = CircleShape,
                            color = SenaHeader,
                            border = androidx.compose.foundation.BorderStroke(2.dp, Color.White)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.CameraAlt, contentDescription = "Cambiar", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Carlos Ruiz",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = SenaText
                )
                Text(
                    text = "Instructor Liderazgo Técnico — ADSO",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SenaTextLight
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Métricas Estilizadas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCardSmall(icon = Icons.Filled.Tasks, value = "24", label = "Proyectos", modifier = Modifier.weight(1f))
                    MetricCardSmall(icon = Icons.Default.CheckCircle, value = "156", label = "Revisiones", modifier = Modifier.weight(1f))
                    MetricCardSmall(icon = Icons.Default.Star, value = "4.8", label = "Rating", modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(32.dp))

                // --- SECCIONES DE AJUSTES ---
                SenaSectionHeader(title = "Información Personal")
                SenaCard(elevation = 1.dp) {
                    if (isEditing) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                SenaTextField(value = name, onValueChange = { name = it }, label = "Nombre", modifier = Modifier.weight(1f))
                                SenaTextField(value = lastName, onValueChange = { lastName = it }, label = "Apellido", modifier = Modifier.weight(1f))
                            }
                            SenaTextField(value = email, onValueChange = { email = it }, label = "Correo Institucional", leadingIcon = Icons.Default.Email)
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                SenaButton(text = "Cancelar", onClick = { isEditing = false }, isPrimary = false, modifier = Modifier.weight(1f))
                                SenaButton(text = "Guardar", onClick = { isEditing = false }, modifier = Modifier.weight(1f))
                            }
                        }
                    } else {
                        Column {
                            SenaSettingsItem(icon = Icons.Default.Person, title = "Nombre Completo", description = "$name $lastName")
                            HorizontalDivider(color = SenaBorderSoft, modifier = Modifier.padding(start = 56.dp))
                            SenaSettingsItem(icon = Icons.Default.Email, title = "Correo Institucional", description = email)
                            HorizontalDivider(color = SenaBorderSoft, modifier = Modifier.padding(start = 56.dp))
                            SenaSettingsItem(icon = Icons.Default.Badge, title = "Código Instructor", description = "INS-2023-001")
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            SenaButton(
                                text = "Editar Información", 
                                onClick = { isEditing = true }, 
                                isPrimary = false, 
                                icon = Icons.Default.Edit,
                                modifier = Modifier.height(44.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                SenaSectionHeader(title = "Evaluación")
                SenaCard(elevation = 1.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Plantilla de Comentarios", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SenaTextLight)
                        OutlinedTextField(
                            value = commentTemplate,
                            onValueChange = { commentTemplate = it },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                            textStyle = MaterialTheme.typography.bodySmall,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SenaGreen,
                                unfocusedBorderColor = SenaBorderSoft,
                                focusedContainerColor = SenaBackground,
                                unfocusedContainerColor = SenaBackground
                            )
                        )
                        
                        HorizontalDivider(color = SenaBorderSoft, modifier = Modifier.padding(vertical = 8.dp))
                        
                        var notifNuevos by remember { mutableStateOf(true) }
                        var notifPendientes by remember { mutableStateOf(true) }
                        
                        SenaSettingsItem(
                            icon = Icons.Default.NotificationsActive, 
                            title = "Nuevos Proyectos", 
                            description = "Alertas de registros",
                            trailing = { Switch(checked = notifNuevos, onCheckedChange = { notifNuevos = it }, colors = SwitchDefaults.colors(checkedTrackColor = SenaGreen)) }
                        )
                        SenaSettingsItem(
                            icon = Icons.Default.History, 
                            title = "Recordatorios", 
                            description = "Revisiones pendientes",
                            trailing = { Switch(checked = notifPendientes, onCheckedChange = { notifPendientes = it }, colors = SwitchDefaults.colors(checkedTrackColor = SenaGreen)) }
                        )
                        
                        SenaButton(text = "Guardar Preferencias", onClick = { }, icon = Icons.Default.Save, modifier = Modifier.height(44.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                SenaSectionHeader(title = "Seguridad")
                SenaCard(elevation = 1.dp) {
                    SenaSettingsItem(
                        icon = Icons.Default.Lock, 
                        title = "Cambiar Contraseña", 
                        description = "Actualiza tu acceso",
                        onClick = { onNavigate(AppNavigation.RESET_PASSWORD) }
                    )
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
fun MetricCardSmall(icon: ImageVector, value: String, label: String, modifier: Modifier = Modifier) {
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
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontWeight = FontWeight.Black, fontSize = 18.sp, color = SenaText)
            Text(label, style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InstructorProfilePreview() {
    ProyecTwinTheme {
        InstructorProfileScreen(onBack = {}, onNavigate = {})
    }
}
