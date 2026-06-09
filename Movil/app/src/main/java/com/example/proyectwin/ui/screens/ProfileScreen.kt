package com.example.proyectwin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onNavigate: (String) -> Unit) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    
    // Dialog States
    var showPersonalDataDialog by remember { mutableStateOf(false) }
    var showSecurityDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SenaTopBar(
                title = "Mi Perfil",
                showProfile = false,
                onNavigateToAlerts = { onNavigate("main/alerts") }
            )
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
            // Profile Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Brush.verticalGradient(colors = listOf(SenaHeader, SenaGreen)))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(90.dp),
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 8.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(60.dp), tint = SenaGreen)
                            
                            // Edit Icon Overlay
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(28.dp)
                                    .clickable { showPersonalDataDialog = true },
                                shape = CircleShape,
                                color = SenaGreen,
                                shadowElevation = 4.dp
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Editar",
                                    modifier = Modifier.padding(6.dp),
                                    tint = Color.White
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Maria Gonzalez", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    Text("Aprendiz - ADSO", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                }
            }

            // Stats row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStatItemV3("3", "Proyectos")
                ProfileStatItemV3("12", "Meses")
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ProfileMenuSectionV2("INFORMACIÓN PERSONAL") {
                    ProfileMenuItemV2(Icons.Default.Badge, "Documento", "1023456789")
                    ProfileMenuItemV2(Icons.Default.Email, "Correo institucional", "maria.gonzalez@sena.edu.co")
                    ProfileMenuItemV2(Icons.Default.School, "Programa de Formación", "Análisis y desarrollo de Software")
                    ProfileMenuItemV2(Icons.Default.EditNote, "Modificar mis datos", "Actualiza tu información", onClick = { showPersonalDataDialog = true })
                }

                ProfileMenuSectionV2("FORMACIÓN Y FICHA") {
                    ProfileMenuItemV2(Icons.Default.Groups, "Mi Ficha (ADSO-2568)", "Ver mis compañeros", onClick = { onNavigate("ficha/detail") })
                }

                ProfileMenuSectionV2("SOPORTE Y SEGURIDAD") {
                    ProfileMenuItemV2(Icons.Default.BugReport, "Reportar una falla", "Enviar informe de error", onClick = { onNavigate("report/issue") })
                    ProfileMenuItemV2(Icons.Default.Lock, "Seguridad", "Cambiar mi contraseña", onClick = { showSecurityDialog = true })
                }

                SenaButton(
                    text = "Cerrar Sesión",
                    onClick = { /* TODO */ },
                    isPrimary = false,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // --- DIALOGS ---

    if (showPersonalDataDialog) {
        ProfileEditDialog(
            title = "Editar Información Personal",
            onDismiss = { showPersonalDataDialog = false },
            onSave = {
                scope.launch {
                    delay(1500)
                    showPersonalDataDialog = false
                }
            }
        ) {
            SenaTextField(value = "Maria", onValueChange = {}, label = "Nombre", leadingIcon = Icons.Default.Person)
            Spacer(modifier = Modifier.height(12.dp))
            SenaTextField(value = "Gonzalez", onValueChange = {}, label = "Apellido")
            Spacer(modifier = Modifier.height(12.dp))
            SenaTextField(value = "3235421165", onValueChange = {}, label = "Teléfono", leadingIcon = Icons.Default.Phone)
            Spacer(modifier = Modifier.height(12.dp))
            SenaTextField(value = "maria.gonzalez@sena.edu.co", onValueChange = {}, label = "Correo", leadingIcon = Icons.Default.Email)
        }
    }

    if (showSecurityDialog) {
        ProfileEditDialog(
            title = "Cambiar Contraseña",
            onDismiss = { showSecurityDialog = false },
            onSave = {
                scope.launch {
                    delay(1500)
                    showSecurityDialog = false
                }
            }
        ) {
            SenaTextField(value = "", onValueChange = {}, label = "Contraseña Actual", isPassword = true, leadingIcon = Icons.Default.Lock)
            Spacer(modifier = Modifier.height(12.dp))
            SenaTextField(value = "", onValueChange = {}, label = "Nueva Contraseña", isPassword = true, leadingIcon = Icons.Default.VpnKey)
            Spacer(modifier = Modifier.height(12.dp))
            SenaTextField(value = "", onValueChange = {}, label = "Confirmar Contraseña", isPassword = true)
        }
    }
}

@Composable
fun ProfileEditDialog(
    title: String,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    var isSavingInternal by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { if (!isSavingInternal) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(top = 40.dp),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black), color = SenaText)
                    if (!isSavingInternal) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    content()
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SenaButton(text = "Cancelar", onClick = onDismiss, isPrimary = false, modifier = Modifier.weight(1f), enabled = !isSavingInternal)
                    SenaButton(
                        text = "Guardar", 
                        onClick = { 
                            isSavingInternal = true
                            onSave()
                        }, 
                        modifier = Modifier.weight(1f),
                        isLoading = isSavingInternal
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileStatItemV3(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black), color = SenaGreen)
        Text(label, style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
    }
}

@Composable
fun ProfileMenuSectionV2(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(title, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = SenaTextMuted, modifier = Modifier.padding(start = 8.dp, bottom = 8.dp))
        SenaCard(content = content)
    }
}

@Composable
fun ProfileMenuItemV2(icon: ImageVector, title: String, sub: String, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp).background(SenaGreen.copy(alpha = 0.05f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = SenaGreen, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = SenaText)
            Text(sub, style = MaterialTheme.typography.labelSmall, color = SenaTextSecondary)
        }
        if (onClick != null) {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SenaTextMuted)
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
