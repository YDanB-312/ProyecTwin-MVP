package com.example.proyectwin.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewUserScreen(onBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRol by remember { mutableStateOf("Aprendiz") }
    val roles = listOf("Aprendiz", "Instructor", "Administrador")
    
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
        containerColor = senaColors().background,
        bottomBar = {
            SenaBottomBar {
                SenaButton(text = "Cancelar", onClick = onBack, isPrimary = false, modifier = Modifier.weight(1f))
                SenaButton(
                    text = "Crear Usuario", 
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
                    icon = Icons.Default.PersonAdd
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
                title = "Nuevo Usuario",
                subtitle = "Registra una nueva cuenta en el sistema y asigna los permisos correspondientes.",
                icon = Icons.Default.UserAdd
            )

            SenaSectionHeader(title = "Datos del Usuario")
            SenaCard(elevation = 1.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SenaTextField(value = name, onValueChange = { name = it }, label = "Nombre *", modifier = Modifier.weight(1f))
                        SenaTextField(value = lastName, onValueChange = { lastName = it }, label = "Apellido *", modifier = Modifier.weight(1f))
                    }
                    SenaTextField(value = email, onValueChange = { email = it }, label = "Correo Institucional *", leadingIcon = Icons.Default.Email)
                    SenaTextField(value = password, onValueChange = { password = it }, label = "Contraseña Temporal *", isPassword = true, leadingIcon = Icons.Default.Lock)
                }
            }

            SenaSectionHeader(title = "Asignación de Rol")
            SenaCard(elevation = 1.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Selecciona el rol jerárquico", style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
                    
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedRol,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true).fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = senaColors().green,
                                unfocusedBorderColor = senaColors().border
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            roles.forEach { rol ->
                                DropdownMenuItem(
                                    text = { Text(rol) },
                                    onClick = {
                                        selectedRol = rol
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (selectedRol == "Aprendiz") {
                SenaSectionHeader(title = "Detalles del Aprendiz")
                SenaCard {
                    SenaTextField(value = "", onValueChange = {}, label = "Código de Ficha", placeholder = "Ej: 2568421")
                }
            }

            SenaAlertBanner(
                title = "Activación Automática",
                message = "Al crear el usuario, se le enviará un correo de bienvenida con sus credenciales de acceso.",
                icon = Icons.Default.Info,
                color = senaColors().info
            )

            Spacer(Modifier.height(80.dp))
        }
    }
}

// Missing icon
val Icons.Filled.UserAdd: ImageVector get() = Icons.Default.PersonAdd

@Preview(showBackground = true)
@Composable
fun NewUserScreenPreview() {
    ProyecTwinTheme {
        NewUserScreen(onBack = {})
    }
}
