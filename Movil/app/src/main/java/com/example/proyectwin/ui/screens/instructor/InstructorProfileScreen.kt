package com.example.proyectwin.ui.screens.instructor

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
import com.example.proyectwin.data.mock.MockDataProvider
import com.example.proyectwin.data.model.ProjectStatus
import com.example.proyectwin.navigation.AppNavigation
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*
import com.example.proyectwin.ui.viewmodel.AuthViewModel
import com.example.proyectwin.ui.viewmodel.DashboardViewModel
import com.example.proyectwin.ui.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch
import java.util.Base64

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructorProfileScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    bottomBar: @Composable () -> Unit = {},
    authViewModel: AuthViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel(),
    dashboardViewModel: DashboardViewModel = viewModel()
) {
    val scrollState = rememberScrollState()
    val authState by authViewModel.uiState.collectAsState()
    val user = authState.user
    val scope = rememberCoroutineScope()

    var isEditing by remember { mutableStateOf(false) }
    var name by remember(user) { mutableStateOf(user?.name?.split(" ")?.firstOrNull() ?: "") }
    var lastName by remember(user) { mutableStateOf(user?.name?.split(" ")?.getOrNull(1) ?: "") }
    var email by remember(user) { mutableStateOf(user?.email ?: "") }
    var commentTemplate by remember { mutableStateOf("Estimado aprendiz,\n\nHe revisado tu proyecto y tengo los siguientes comentarios:\n\nAspectos positivos:\n-\n\nAspectos a mejorar:\n-\n\nRecomendaciones:\n-") }

    val instructorProjects = remember(user) {
        MockDataProvider.getProjectsByInstructor(user?.id ?: 0)
    }
    val proyectosCount = instructorProjects.size
    val proyectosActivos = instructorProjects.count { it.estado == ProjectStatus.EN_PROGRESO.value }
    val aprendicesCount = remember(user) {
        MockDataProvider.getAllFichas().flatMap { it.estudiantes }.distinctBy { it.id }.size
    }

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
        topBar = {
            SenaTopBar(
                title = "Perfil Instructor",
                onBack = onBack,
                showProfile = false,
                showNotifications = true,
                onNavigateToAlerts = { onNavigate(AppNavigation.INSTRUCTOR_ALERTS) }
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(senaColors().header, senaColors().green.copy(alpha = 0.8f))
                        )
                    )
            ) {
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .offset(x = (-40).dp, y = (-20).dp)
                        .background(Color.White.copy(alpha = 0.05f), CircleShape)
                )
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .offset(y = (-80).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SenaAvatar(
                    fotoBase64 = user?.fotoPerfil,
                    nombre = user?.name ?: "Instructor",
                    modifier = Modifier.size(110.dp),
                    onClick = { photoPickerLauncher.launch("image/*") }
                )

                Spacer(modifier = Modifier.height(4.dp))
                TextButton(onClick = { photoPickerLauncher.launch("image/*") }) {
                    Text("Cambiar foto", style = MaterialTheme.typography.labelSmall, color = senaColors().green)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = user?.name ?: "Instructor",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = senaColors().text
                )
                Text(
                    text = "${user?.roleDisplayName ?: "Instructor"} — ADSO",
                    style = MaterialTheme.typography.bodyMedium,
                    color = senaColors().textLight
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCardSmall(icon = Icons.Filled.Tasks, value = "$proyectosCount", label = "Proyectos", modifier = Modifier.weight(1f))
                    MetricCardSmall(icon = Icons.Default.CheckCircle, value = "$proyectosActivos", label = "Activos", modifier = Modifier.weight(1f))
                    MetricCardSmall(icon = Icons.Default.People, value = "$aprendicesCount", label = "Aprendices", modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(32.dp))

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
                                SenaButton(text = "Guardar", onClick = {
                                    profileViewModel.updateProfile("$name $lastName", email, user?.telefono)
                                    isEditing = false
                                }, modifier = Modifier.weight(1f))
                            }
                        }
                    } else {
                        Column {
                            SenaSettingsItem(icon = Icons.Default.Person, title = "Nombre Completo", description = user?.name ?: "-")
                            HorizontalDivider(color = senaColors().borderSoft, modifier = Modifier.padding(start = 56.dp))
                            SenaSettingsItem(icon = Icons.Default.Email, title = "Correo Institucional", description = user?.email ?: "-")
                            HorizontalDivider(color = senaColors().borderSoft, modifier = Modifier.padding(start = 56.dp))
                            SenaSettingsItem(icon = Icons.Default.Badge, title = "Documento de Identidad", description = user?.documentoIdentidad ?: "-")

                            Spacer(modifier = Modifier.height(16.dp))
                            SenaButton(
                                text = "Editar Información",
                                onClick = {
                                    name = user?.name?.split(" ")?.firstOrNull() ?: ""
                                    lastName = user?.name?.split(" ")?.getOrNull(1) ?: ""
                                    email = user?.email ?: ""
                                    isEditing = true
                                },
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
                        Text("Plantilla de Comentarios", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = senaColors().textLight)
                        OutlinedTextField(
                            value = commentTemplate,
                            onValueChange = { commentTemplate = it },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                            textStyle = MaterialTheme.typography.bodySmall,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = senaColors().green,
                                unfocusedBorderColor = senaColors().borderSoft,
                                focusedContainerColor = senaColors().background,
                                unfocusedContainerColor = senaColors().background
                            )
                        )

                        HorizontalDivider(color = senaColors().borderSoft, modifier = Modifier.padding(vertical = 8.dp))

                        var notifNuevos by remember { mutableStateOf(true) }
                        var notifPendientes by remember { mutableStateOf(true) }

                        SenaSettingsItem(
                            icon = Icons.Default.NotificationsActive,
                            title = "Nuevos Proyectos",
                            description = "Alertas de registros",
                            trailing = { Switch(checked = notifNuevos, onCheckedChange = { notifNuevos = it }, colors = SwitchDefaults.colors(checkedTrackColor = senaColors().green)) }
                        )
                        SenaSettingsItem(
                            icon = Icons.Default.History,
                            title = "Recordatorios",
                            description = "Revisiones pendientes",
                            trailing = { Switch(checked = notifPendientes, onCheckedChange = { notifPendientes = it }, colors = SwitchDefaults.colors(checkedTrackColor = senaColors().green)) }
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
}

@Composable
fun MetricCardSmall(icon: ImageVector, value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, senaColors().borderSoft)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = senaColors().green, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontWeight = FontWeight.Black, fontSize = 18.sp, color = senaColors().text)
            Text(label, style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
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
