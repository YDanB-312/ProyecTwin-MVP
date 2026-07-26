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
import com.example.proyectwin.navigation.AppNavigation
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(onBack: () -> Unit, onNavigate: (String) -> Unit) {
    var name by remember { mutableStateOf("Maria") }
    var lastName by remember { mutableStateOf("Gonzalez") }
    var email by remember { mutableStateOf("maria.gonzalez@sena.edu.co") }
    var phone by remember { mutableStateOf("3235421165") }
    
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SenaTopBar(
                title = "ProyecTwin",
                onBack = onBack,
                showProfile = true,
                showNotifications = true
            )
        },
        containerColor = SenaBackground,
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
                        isSaving = true
                        scope.launch {
                            delay(1000)
                            isSaving = false
                            onBack()
                        }
                    }, 
                    isLoading = isSaving,
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
                color = SenaInfo
            )

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
