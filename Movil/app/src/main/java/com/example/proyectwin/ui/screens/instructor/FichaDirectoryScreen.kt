package com.example.proyectwin.ui.screens.instructor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectwin.navigation.AppNavigation
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*
import com.example.proyectwin.ui.viewmodel.AuthViewModel
import com.example.proyectwin.ui.viewmodel.FichasUiState
import com.example.proyectwin.ui.viewmodel.FichasViewModel
import kotlinx.coroutines.launch

data class DirectoryMember(
    val name: String,
    val info: String,
    val initials: String,
    val isActive: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FichaDirectoryScreen(
    onBack: () -> Unit,
    onCreateFicha: () -> Unit,
    onNavigate: (String) -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    fichasViewModel: FichasViewModel = viewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val uiState by fichasViewModel.uiState.collectAsState()
    var refreshTrigger by remember { mutableIntStateOf(0) }
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit, refreshTrigger) {
        fichasViewModel.loadAllFichas()
    }

    val isLoading = uiState is FichasUiState.Loading
    LaunchedEffect(isLoading) {
        if (!isLoading) isRefreshing = false
    }

    val ficha = (uiState as? FichasUiState.Success)?.fichas?.firstOrNull { it.estado == "activo" }
    val members = ficha?.estudiantes?.map { student ->
        DirectoryMember(
            name = student.name,
            info = student.email,
            initials = student.name.firstOrNull()?.uppercase() ?: "?",
            isActive = true
        )
    } ?: emptyList()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SenaTopBar(
                title = "ProyecTwin",
                onBack = onBack,
                showProfile = true,
                showNotifications = true
            )
        },
        containerColor = senaColors().background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateFicha,
                containerColor = senaColors().green,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Ficha")
            }
        }
    ) { paddingValues ->
        SenaPullRefresh(
            isRefreshing = isRefreshing,
            onRefresh = { isRefreshing = true; refreshTrigger++ },
            modifier = Modifier.padding(paddingValues)
        ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                SenaPageHeader(
                    title = "Directorio de Ficha",
                    subtitle = "Listado completo de aprendices pertenecientes al programa.",
                    icon = Icons.Default.AddressBook
                )
            }

            when (val state = uiState) {
                is FichasUiState.Loading -> item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = senaColors().green)
                    }
                }
                is FichasUiState.Error -> item {
                    SenaErrorState(message = state.message, onRetry = { fichasViewModel.loadAllFichas() })
                }
                is FichasUiState.Success -> {
                    if (ficha == null) {
                        item {
                            SenaCard(elevation = 1.dp) {
                                Text("No hay fichas activas disponibles", color = senaColors().textMuted)
                            }
                        }
                    } else {
                        item {
                            SenaCard(elevation = 1.dp) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(ficha.codigo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = senaColors().text)
                                        Text(ficha.programa, style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
                                    }
                                    Surface(
                                        color = senaColors().green.copy(alpha = 0.1f),
                                        shape = CircleShape
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Groups, contentDescription = null, tint = senaColors().green, modifier = Modifier.size(14.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("${members.size} Aprendices", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = senaColors().green)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                SenaSectionHeader(title = "Lista de Aprendices")
            }

            items(members) { member ->
                SenaCard(elevation = 0.5.dp, onClick = { onNavigate(AppNavigation.INSTRUCTOR_FICHA_DETAIL.replace("{fichaId}", ficha?.codigo ?: "")) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            color = if (member.isActive) senaColors().green.copy(alpha = 0.1f) else senaColors().borderSoft
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    member.initials, 
                                    fontWeight = FontWeight.Bold, 
                                    color = if (member.isActive) senaColors().green else senaColors().textMuted
                                )
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(member.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = senaColors().text)
                            Text(member.info, style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
                        }
                        IconButton(onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Opciones de aprendiz no disponibles")
                            }
                        }) {
                            Icon(Icons.Default.MoreVert, contentDescription = null, tint = senaColors().textMuted)
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(40.dp)) }
        }
        }
    }
}

// Missing icon
val Icons.Filled.AddressBook: ImageVector get() = Icons.Default.ContactPage

@Preview(showBackground = true)
@Composable
fun FichaDirectoryScreenPreview() {
    ProyecTwinTheme {
        FichaDirectoryScreen(onBack = {}, onCreateFicha = {}, onNavigate = {})
    }
}
