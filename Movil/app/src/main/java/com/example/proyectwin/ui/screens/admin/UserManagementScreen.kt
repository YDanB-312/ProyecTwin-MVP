package com.example.proyectwin.ui.screens.admin

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectwin.navigation.AppNavigation
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*
import kotlinx.coroutines.launch

data class UserItem(
    val name: String,
    val document: String,
    val email: String,
    val role: String,
    val status: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(onBack: () -> Unit, onNavigate: (String) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Todos") }
    val roles = listOf("Todos", "Aprendiz", "Instructor", "Administrador")
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var userToDelete by remember { mutableStateOf<UserItem?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    val dummyUsers = remember {
        listOf(
            UserItem("Ana Martinez Lopez", "1023456789", "ana.martinez@soy.sena.edu.co", "Aprendiz", "Activo"),
            UserItem("Juan Perez Gomez", "1045678901", "juan.perez@soy.sena.edu.co", "Aprendiz", "Inactivo"),
            UserItem("Carlos Rodriguez Diaz", "79876543", "carlos.rodriguez@sena.edu.co", "Instructor", "Activo"),
            UserItem("Diego Munoz Herrera", "80123456", "diego.munoz@sena.edu.co", "Administrador", "Activo"),
        )
    }

    val filteredUsers = dummyUsers.filter { user ->
        val matchesRole = if (selectedRole == "Todos") true else user.role == selectedRole
        val matchesSearch = user.name.contains(searchQuery, ignoreCase = true) || user.email.contains(searchQuery, ignoreCase = true)
        matchesRole && matchesSearch
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SenaTopBar(
                title = "ProyecTwin",
                onBack = onBack,
                showProfile = true,
                showNotifications = true,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigate(AppNavigation.ADMIN_NEW_USER) },
                containerColor = SenaGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Nuevo Usuario")
            }
        },
        containerColor = SenaBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                SenaPageHeader(
                    title = "Gestión de Usuarios",
                    subtitle = "Administra las cuentas, roles y permisos de acceso al sistema.",
                    icon = Icons.Default.ManageAccounts
                )
            }

            // Filter Section
            item {
                SenaCard(elevation = 1.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            "Filtros de búsqueda",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SenaTextLight,
                            letterSpacing = 0.5.sp
                        )
                        SenaTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = "",
                            placeholder = "Buscar por nombre, correo o documento...",
                            leadingIcon = Icons.Default.Search
                        )
                        
                        Text(
                            "Filtrar por rol",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SenaTextLight,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            roles.forEach { role ->
                                SenaChip(
                                    text = role,
                                    color = when(role) {
                                        "Aprendiz" -> SenaSuccess
                                        "Instructor" -> SenaWarning
                                        "Administrador" -> SenaDanger
                                        else -> SenaGreen
                                    },
                                    isSelected = selectedRole == role,
                                    onClick = { selectedRole = role }
                                )
                            }
                        }
                    }
                }
            }

            if (filteredUsers.isEmpty()) {
                item {
                    SenaEmptyState(
                        message = "No hay usuarios que coincidan con los criterios de búsqueda.",
                        icon = Icons.Default.GroupOff
                    )
                }
            } else {
                items(filteredUsers) { user ->
                    UserCard(
                        user = user,
                        onEdit = { onNavigate(AppNavigation.ADMIN_USER_DETAIL.replace("{userId}", "1")) },
                        onDelete = { 
                            userToDelete = user
                            showDeleteDialog = true 
                        }
                    )
                }
            }
            
            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Usuario") },
            text = { Text("¿Estás seguro de que deseas eliminar a ${userToDelete?.name}? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = { 
                    showDeleteDialog = false
                    scope.launch {
                        snackbarHostState.showSnackbar("Usuario eliminado")
                    }
                }) {
                    Text("Eliminar", color = SenaDanger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun UserCard(user: UserItem, onEdit: () -> Unit, onDelete: () -> Unit) {
    SenaCard {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = SenaGreen.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                user.name.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = SenaGreen
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            user.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = SenaText
                        )
                        Text(user.email, style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
                    }
                }
                SenaStatusBadge(status = user.status)
            }

            HorizontalDivider(color = SenaBorderSoft)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Rol y Documento", style = MaterialTheme.typography.labelSmall, color = SenaTextLight, fontSize = 9.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Badge, 
                            contentDescription = null, 
                            tint = SenaGreen, 
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "${user.role} • ${user.document}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SenaTextSecondary
                        )
                    }
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledIconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = SenaBorderSoft)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = SenaGreen, modifier = Modifier.size(18.dp))
                    }
                    FilledIconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = SenaBorderSoft)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = SenaDanger, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserManagementPreview() {
    ProyecTwinTheme {
        UserManagementScreen(onBack = {}, onNavigate = {})
    }
}
