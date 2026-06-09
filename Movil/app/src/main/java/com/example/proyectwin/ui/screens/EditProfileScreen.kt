package com.example.proyectwin.ui.screens

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(onBack: () -> Unit) {
    var firstName by remember { mutableStateOf("Maria") }
    var lastName by remember { mutableStateOf("Gonzalez") }
    var phone by remember { mutableStateOf("3235421165") }
    var email by remember { mutableStateOf("maria.gonzalez@sena.edu.co") }
    
    // Dropdown States
    var expandedProgram by remember { mutableStateOf(false) }
    var selectedProgram by remember { mutableStateOf("Análisis y Desarrollo de Software") }
    val programs = listOf("Análisis y Desarrollo de Software", "Tecnología en Sistemas", "Diseño Multimedia")

    var expandedCenter by remember { mutableStateOf(false) }
    var selectedCenter by remember { mutableStateOf("Centro de Tecnologías para la Academia") }
    val centers = listOf("Centro de Tecnologías para la Academia", "Centro de Diseño y Metrología", "Centro de Electricidad")

    var isSaving by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SenaTopBar(title = "Editar Perfil", showProfile = false, showNotifications = false)
        },
        containerColor = SenaBackground,
        bottomBar = {
            Surface(tonalElevation = 8.dp, shadowElevation = 16.dp, color = Color.White) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SenaButton(
                        text = "Cancelar", 
                        onClick = onBack, 
                        isPrimary = false, 
                        modifier = Modifier.weight(1f),
                        enabled = !isSaving
                    )
                    SenaButton(
                        text = "Guardar", 
                        onClick = { 
                            scope.launch {
                                isSaving = true
                                delay(1500) // Simulación de red
                                isSaving = false
                                snackbarHostState.showSnackbar("Perfil actualizado con éxito")
                                delay(500)
                                onBack()
                            }
                        }, 
                        modifier = Modifier.weight(1f),
                        isLoading = isSaving
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            IconButton(onClick = onBack) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = SenaGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Regresar", color = SenaGreen, style = MaterialTheme.typography.labelLarge)
                }
            }

            SenaSectionHeader(title = "Datos Personales")
            
            SenaCard {
                SenaTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = "Nombre",
                    placeholder = "Tu nombre",
                    leadingIcon = Icons.Default.Person
                )
                Spacer(modifier = Modifier.height(16.dp))
                SenaTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = "Apellido",
                    placeholder = "Tu apellido"
                )
                Spacer(modifier = Modifier.height(16.dp))
                SenaTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Teléfono",
                    placeholder = "Ej: 323 542 1165",
                    keyboardType = KeyboardType.Phone,
                    leadingIcon = Icons.Default.Phone
                )
                Spacer(modifier = Modifier.height(16.dp))
                SenaTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Correo Electrónico",
                    placeholder = "tu@correo.com",
                    keyboardType = KeyboardType.Email,
                    leadingIcon = Icons.Default.Email,
                    imeAction = ImeAction.Done
                )
            }

            SenaSectionHeader(title = "Formación")
            SenaCard {
                // Real Material 3 Exposed Dropdown Menu for Program
                Text("Programa de Formación", style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = expandedProgram,
                    onExpandedChange = { expandedProgram = !expandedProgram }
                ) {
                    OutlinedTextField(
                        value = selectedProgram,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProgram) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SenaGreen,
                            unfocusedBorderColor = SenaBorder
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedProgram,
                        onDismissRequest = { expandedProgram = false }
                    ) {
                        programs.forEach { program ->
                            DropdownMenuItem(
                                text = { Text(program) },
                                onClick = {
                                    selectedProgram = program
                                    expandedProgram = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Real Material 3 Exposed Dropdown Menu for Center
                Text("Centro de Formación", style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = expandedCenter,
                    onExpandedChange = { expandedCenter = !expandedCenter }
                ) {
                    OutlinedTextField(
                        value = selectedCenter,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCenter) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SenaGreen,
                            unfocusedBorderColor = SenaBorder
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCenter,
                        onDismissRequest = { expandedCenter = false }
                    ) {
                        centers.forEach { center ->
                            DropdownMenuItem(
                                text = { Text(center) },
                                onClick = {
                                    selectedCenter = center
                                    expandedCenter = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditProfileScreenPreview() {
    ProyecTwinTheme {
        EditProfileScreen {}
    }
}
