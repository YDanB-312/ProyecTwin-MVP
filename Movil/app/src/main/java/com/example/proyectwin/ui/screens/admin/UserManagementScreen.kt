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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectwin.navigation.AppNavigation
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*
import com.example.proyectwin.ui.viewmodel.AdminViewModel
import com.example.proyectwin.ui.viewmodel.AuthViewModel
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
fun UserManagementScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    bottomBar: @Composable () -> Unit = {},
    authViewModel: AuthViewModel = viewModel(),
    adminViewModel: AdminViewModel = viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Todos") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var userToDelete by remember { mutableStateOf<UserItem?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val adminState by adminViewModel.uiState.collectAsState()

    LaunchedEffect(adminState.isLoading) {
        if (!adminState.isLoading) isRefreshing = false
    }

    if (adminState.isLoading && adminState.users.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SenaSkeletonLine(width = 180.dp, height = 24.dp)
                SenaSkeletonLine(width = 260.dp, height = 14.dp)
                Spacer(Modifier.height(12.dp))
                repeat(4) { SenaSkeletonCard(lines = 3, hasAvatar = true) }
            }
        }
        return
    }

    val roles = remember(adminState.users) {
        listOf("Todos") + adminState.users.map { it.roleDisplayName }.distinct().sorted()
    }

    val usersList = remember(adminState.users) {
        adminState.users.map { user ->
            UserItem(
                name = user.name,
                document = user.documentoIdentidad ?: "",
                email = user.email,
                role = user.roleDisplayName,
                status = "Activo"
            )
        }
    }

    val filteredUsers = usersList.filter { user ->
        val matchesRole = if (selectedRole == "Todos") true else user.role == selectedRole
        val matchesSearch = user.name.contains(searchQuery, ignoreCase = true) || user.email.contains(searchQuery, ignoreCase = true) || user.document.contains(searchQuery, ignoreCase = true)
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
        containerColor = senaColors().background,
        bottomBar = bottomBar,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigate(AppNavigation.ADMIN_NEW_USER) },
                containerColor = senaColors().green,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Nuevo Usuario")
            }
        }
    ) { paddingValues ->
        SenaPullRefresh(
            isRefreshing = isRefreshing,
            onRefresh = { isRefreshing = true; adminViewModel.refresh() },
            modifier = Modifier.padding(paddingValues)
        ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
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
                SenaFilterBar(title = "Filtros de búsqueda") {
                    SenaTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = "",
                        placeholder = "Buscar por nombre, correo o documento...",
                        leadingIcon = Icons.Default.Search
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        roles.forEach { role ->
                            SenaChip(
                                text = role,
                                color = when(role) {
                                    "Aprendiz" -> senaColors().success
                                    "Instructor" -> senaColors().warning
                                    "Administrador" -> senaColors().danger
                                    else -> senaColors().green
                                },
                                isSelected = selectedRole == role,
                                onClick = { selectedRole = role }
                            )
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
                    Text("Eliminar", color = senaColors().danger)
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
                        color = senaColors().green.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                user.name.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = senaColors().green
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            user.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = senaColors().text
                        )
                        Text(user.email, style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
                    }
                }
                SenaStatusBadge(status = user.status)
            }

            HorizontalDivider(color = senaColors().borderSoft)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Rol y Documento", style = MaterialTheme.typography.labelSmall, color = senaColors().textLight, fontSize = 9.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Badge,
                            contentDescription = null,
                            tint = senaColors().green,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "${user.role} • ${user.document}",
                            style = MaterialTheme.typography.bodySmall,
                            color = senaColors().textSecondary
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledIconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = senaColors().borderSoft)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = senaColors().green, modifier = Modifier.size(18.dp))
                    }
                    FilledIconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = senaColors().borderSoft)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = senaColors().danger, modifier = Modifier.size(18.dp))
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
