package com.example.proyectwin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val authState by authViewModel.uiState.collectAsState()
    val profileState by profileViewModel.uiState.collectAsState()
    val user = authState.user

    var name by remember(user) { mutableStateOf(user?.name?.split(" ")?.firstOrNull() ?: "") }
    var lastName by remember(user) { mutableStateOf(user?.name?.split(" ")?.drop(1)?.joinToString(" ") ?: "") }
    var email by remember(user) { mutableStateOf(user?.email ?: "") }
    var phone by remember(user) { mutableStateOf(user?.telefono ?: "") }

    LaunchedEffect(profileState.saveSuccess) {
        if (profileState.saveSuccess) {
            profileViewModel.clearSaveSuccess()
            onBack()
        }
    }

    Scaffold(
        topBar = {
            SenaTopBar(
                title = "ProyecTwin",
                onBack = onBack,
                showProfile = true,
                showNotifications = true
            )
        },
        containerColor = senaColors().background,
        bottomBar = {
            SenaBottomBar {
                SenaButton(
                    text = "Cancelar", 
                    onClick = onBack, 
                    isPrimary = false, 
                    modifier = Modifier.weight(1f)
                )
                SenaButton(
                    text = "Guardar Cambios", 
                    onClick = {
                        profileViewModel.updateProfile(
                            "$name $lastName".trim(),
                            email,
                            phone.ifBlank { null }
                        )
                    }, 
                    isLoading = profileState.isSaving,
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Save
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            SenaPageHeader(
                title = "Editar Perfil",
                subtitle = "Actualiza tu información personal y de contacto en el sistema.",
                icon = Icons.Default.Edit
            )

            SenaSectionHeader(title = "Información Básica")
            SenaCard(elevation = 1.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SenaTextField(
                            value = name, 
                            onValueChange = { name = it }, 
                            label = "Nombre *",
                            modifier = Modifier.weight(1f)
                        )
                        SenaTextField(
                            value = lastName, 
                            onValueChange = { lastName = it }, 
                            label = "Apellido *",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    SenaTextField(
                        value = email, 
                        onValueChange = { email = it }, 
                        label = "Correo Electrónico *",
                        leadingIcon = Icons.Default.Email,
                        keyboardType = KeyboardType.Email
                    )
                    
                    SenaTextField(
                        value = phone, 
                        onValueChange = { phone = it }, 
                        label = "Teléfono de Contacto",
                        leadingIcon = Icons.Default.Phone,
                        keyboardType = KeyboardType.Phone
                    )
                }
            }

            Text(
                text = "Credenciales demo — Los cambios se reflejarán en tiempo real.",
                style = MaterialTheme.typography.bodySmall,
                color = senaColors().textMuted,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            SenaSectionHeader(title = "Seguridad")
            SenaCard(elevation = 1.dp) {
                SenaSettingsItem(
                    icon = Icons.Default.Lock, 
                    title = "Cambiar Contraseña", 
                    description = "Se te redirigirá a la pantalla de cambio de clave.",
                    onClick = { onNavigate(AppNavigation.RESET_PASSWORD) }
                )
            }

            SenaAlertBanner(
                title = "Privacidad de Datos",
                message = "Tu información solo es visible para instructores y personal administrativo autorizado.",
                icon = Icons.Default.Shield,
                color = senaColors().info
            )

            if (profileState.error != null) {
                SenaAlertBanner(
                    title = "Error",
                    message = profileState.error!!,
                    icon = Icons.Default.Error,
                    color = senaColors().danger
                )
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditProfileScreenPreview() {
    ProyecTwinTheme {
        EditProfileScreen(onBack = {}, onNavigate = {})
    }
}
