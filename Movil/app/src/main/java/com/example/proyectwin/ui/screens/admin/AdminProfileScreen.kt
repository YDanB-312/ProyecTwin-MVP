package com.example.proyectwin.ui.screens.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectwin.navigation.AppNavigation
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*
import com.example.proyectwin.ui.viewmodel.AuthViewModel
import com.example.proyectwin.ui.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch
import java.util.Base64

@Composable
fun AdminProfileScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    bottomBar: @Composable () -> Unit = {},
    authViewModel: AuthViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {
    val scrollState = rememberScrollState()
    var showLogoutSessionsDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val authState by authViewModel.uiState.collectAsState()
    val user = authState.user

    val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                val inputStream = context.contentResolver.openInputStream(it)
                val bytes = inputStream?.readBytes()
                inputStream?.close()
                bytes?.let { b ->
                    val base64 = Base64.getEncoder().encodeToString(b)
                    profileViewModel.updateFoto(base64)
                    authViewModel.getSessionManager().updateFoto(base64)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SenaTopBar(
                title = "Perfil Admin",
                onBack = onBack,
                showProfile = false,
                showNotifications = false
            )
        },
        containerColor = senaColors().background,
        bottomBar = bottomBar
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            // --- HEADER ADMIN (DEGRADADO PROFUNDO) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF022C22), senaColors().header)
                        )
                    )
            ) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .offset(x = 100.dp, y = (-40).dp)
                        .background(Color.White.copy(alpha = 0.03f), CircleShape)
                )
            }

            // --- PERFIL CARD (FLOTANTE) ---
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .offset(y = (-80).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SenaAvatar(
                    fotoBase64 = user?.fotoPerfil,
                    nombre = user?.name ?: "Admin",
                    modifier = Modifier.size(110.dp),
                    onClick = { photoPickerLauncher.launch("image/*") }
                )

                Spacer(modifier = Modifier.height(4.dp))
                TextButton(onClick = { photoPickerLauncher.launch("image/*") }) {
                    Text("Cambiar foto", style = MaterialTheme.typography.labelSmall, color = senaColors().green)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = user?.name ?: "Admin Sistema",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = senaColors().text
                )
                Text(
                    text = user?.roleDisplayName ?: "Control Maestro de Plataforma",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF0D9488)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Métricas Admin
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCardAdmin(icon = Icons.Default.People, value = "156", label = "Usuarios", modifier = Modifier.weight(1f))
                    MetricCardAdmin(icon = Icons.Default.BugReport, value = "8", label = "Bugs", modifier = Modifier.weight(1f))
                    MetricCardAdmin(icon = Icons.Default.Notifications, value = "6", label = "Alertas", modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(32.dp))

                // --- SECCIONES ---
                SenaSectionHeader(title = "Gestión Maestra")
                SenaCard(elevation = 1.dp) {
                    Column {
                        SenaSettingsItem(
                            icon = Icons.Default.Badge,
                            title = "Documento",
                            description = user?.documentoIdentidad ?: "No registrado"
                        )
                        HorizontalDivider(color = senaColors().borderSoft, modifier = Modifier.padding(start = 56.dp))
                        SenaSettingsItem(
                            icon = Icons.Default.Email,
                            title = "Correo Institucional",
                            description = user?.email ?: "No registrado"
                        )
                        HorizontalDivider(color = senaColors().borderSoft, modifier = Modifier.padding(start = 56.dp))
                        SenaSettingsItem(
                            icon = Icons.Default.Phone,
                            title = "Teléfono",
                            description = user?.telefono ?: "No registrado"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                SenaSectionHeader(title = "Seguridad de Red")
                SenaCard(elevation = 1.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SenaAlertBanner(
                            title = "Último Acceso",
                            message = "Sesión activa hoy 08:30 AM desde IP 192.168.1.1",
                            icon = Icons.Default.Shield,
                            color = senaColors().warning
                        )

                        SenaSettingsItem(
                            icon = Icons.Default.Lock, 
                            title = "Cambiar Contraseña Maestra", 
                            description = "Protocolo de alta seguridad",
                            onClick = { onNavigate(AppNavigation.RESET_PASSWORD) }
                        )
                        
                        SenaButton(
                            text = "Matar Todas las Sesiones",
                            onClick = { showLogoutSessionsDialog = true },
                            isPrimary = false,
                            containerColor = senaColors().danger,
                            icon = Icons.AutoMirrored.Filled.Logout,
                            modifier = Modifier.height(44.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                SenaSectionHeader(title = "Configuración Global")
                SenaCard(elevation = 1.dp) {
                    Column {
                        var notifCorreo by remember { mutableStateOf(true) }
                        var alertasUsuarios by remember { mutableStateOf(true) }
                        
                        SenaSettingsItem(
                            icon = Icons.Default.Email, 
                            title = "Logs por Correo", 
                            description = "Reportes diarios automáticos",
                            trailing = { Switch(checked = notifCorreo, onCheckedChange = { notifCorreo = it }, colors = SwitchDefaults.colors(checkedTrackColor = senaColors().green)) }
                        )
                        HorizontalDivider(color = senaColors().borderSoft, modifier = Modifier.padding(start = 56.dp))
                        SenaSettingsItem(
                            icon = Icons.Default.PersonAdd, 
                            title = "Alerta de Tráfico", 
                            description = "Aviso por nuevos registros",
                            trailing = { Switch(checked = alertasUsuarios, onCheckedChange = { alertasUsuarios = it }, colors = SwitchDefaults.colors(checkedTrackColor = senaColors().green)) }
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        SenaButton(
                            text = "Sincronizar Cambios", 
                            onClick = { 
                                scope.launch {
                                    snackbarHostState.showSnackbar("Configuración de red actualizada")
                                }
                            }, 
                            icon = Icons.Default.Save,
                            modifier = Modifier.height(44.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                SenaButton(
                    text = "Cerrar Sesión",
                    onClick = {
                        authViewModel.logout()
                        onNavigate(AppNavigation.HOME)
                    },
                    icon = Icons.AutoMirrored.Filled.Logout,
                    containerColor = senaColors().danger,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    if (showLogoutSessionsDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutSessionsDialog = false },
            title = { Text("Seguridad de Red") },
            text = { Text("¿Confirma el cierre forzoso de todas las sesiones activas en la red ProyecTwin?") },
            confirmButton = {
                TextButton(onClick = { showLogoutSessionsDialog = false }) {
                    Text("Matar Sesiones", color = senaColors().danger, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutSessionsDialog = false }) {
                    Text("Cancelar")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun MetricCardAdmin(icon: ImageVector, value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF1F5F9),
        border = androidx.compose.foundation.BorderStroke(1.dp, senaColors().border)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF0F172A), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontWeight = FontWeight.Black, fontSize = 18.sp, color = senaColors().text)
            Text(label, style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdminProfileScreenPreview() {
    ProyecTwinTheme {
        AdminProfileScreen(onBack = {}, onNavigate = {})
    }
}
