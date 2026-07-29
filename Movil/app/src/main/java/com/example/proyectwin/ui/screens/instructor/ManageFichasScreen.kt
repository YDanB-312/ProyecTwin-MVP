package com.example.proyectwin.ui.screens.instructor

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.example.proyectwin.data.mock.MockDataProvider
import com.example.proyectwin.navigation.AppNavigation
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*
import com.example.proyectwin.ui.viewmodel.AuthViewModel
import com.example.proyectwin.ui.viewmodel.FichasViewModel

data class FichaItem(
    val code: String,
    val name: String,
    val program: String,
    val students: Int,
    val projects: Int,
    val status: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageFichasScreen(
    onBack: () -> Unit,
    onViewDetail: (String) -> Unit,
    onViewDirectory: (String) -> Unit,
    onCreateFicha: () -> Unit,
    onNavigate: (String) -> Unit,
    bottomBar: @Composable () -> Unit = {},
    authViewModel: AuthViewModel = viewModel(),
    fichasViewModel: FichasViewModel = viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Todas") }
    val filters = listOf("Todas", "Activo", "Inactivo")
    val uiState by fichasViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        fichasViewModel.loadAllFichas()
    }

    val fichas = uiState.fichas.map { ficha ->
        FichaItem(
            code = ficha.codigo,
            name = ficha.programa,
            program = ficha.programa,
            students = ficha.estudiantes.size,
            projects = MockDataProvider.getProjectsByFicha(ficha.id).size,
            status = ficha.statusDisplay
        )
    }

    val filteredFichas = fichas.filter { ficha ->
        val matchesFilter = if (selectedFilter == "Todas") true else ficha.status == selectedFilter
        val matchesSearch = ficha.code.contains(searchQuery, ignoreCase = true) || ficha.name.contains(searchQuery, ignoreCase = true)
        matchesFilter && matchesSearch
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateFicha,
                containerColor = senaColors().green,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.PlusOne, contentDescription = "Nueva Ficha")
            }
        },
        containerColor = senaColors().background,
        bottomBar = bottomBar
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                SenaPageHeader(
                    title = "Gestionar Fichas",
                    subtitle = "Administra los grupos de formación y supervisa el progreso de los aprendices.",
                    icon = Icons.Default.LayerGroup
                )
            }

            // Statistics Bar
            item {
                SenaCard(elevation = 1.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("${fichas.size} Fichas Registradas", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = senaColors().text)
                            Text("Panel de control de instructor", style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
                        }
                        Surface(
                            color = senaColors().green.copy(alpha = 0.1f),
                            shape = CircleShape
                        ) {
                            Icon(
                                Icons.Default.Add, 
                                contentDescription = null, 
                                tint = senaColors().green, 
                                modifier = Modifier.padding(8.dp).size(20.dp)
                            )
                        }
                    }
                }
            }

            // Filters
            item {
                SenaCard(elevation = 0.5.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SenaTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = "Búsqueda rápida",
                            placeholder = "Buscar por código o nombre...",
                            leadingIcon = Icons.Default.Search
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            filters.forEach { filter ->
                                SenaChip(
                                    text = filter,
                                    color = if (filter == "Activo") senaColors().success else if (filter == "Inactivo") senaColors().danger else senaColors().green,
                                    isSelected = selectedFilter == filter,
                                    onClick = { selectedFilter = filter }
                                )
                            }
                        }
                    }
                }
            }

            if (uiState.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = senaColors().green)
                    }
                }
            } else if (filteredFichas.isEmpty()) {
                item {
                    SenaEmptyState(
                        message = "No se encontraron fichas que coincidan con la búsqueda.",
                        icon = Icons.Default.SearchOff
                    )
                }
            } else {
                items(filteredFichas) { ficha ->
                    InstructorFichaCard(
                        ficha = ficha,
                        onViewDetail = { onViewDetail(ficha.code) },
                        onViewDirectory = { onViewDirectory(ficha.code) },
                        onEdit = { onNavigate(AppNavigation.INSTRUCTOR_JOIN_FICHA) }
                    )
                }
            }
            
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun InstructorFichaCard(ficha: FichaItem, onViewDetail: () -> Unit, onViewDirectory: () -> Unit, onEdit: () -> Unit) {
    SenaCard(elevation = 1.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = senaColors().borderSoft,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        ficha.code,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = senaColors().textSecondary
                    )
                }
                SenaStatusBadge(status = ficha.status)
            }

            Column {
                Text(ficha.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = senaColors().text)
                Text(ficha.program, style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FichaStatMini(Icons.Default.Groups, "${ficha.students} Aprendices", Modifier.weight(1f))
                FichaStatMini(Icons.Default.FolderOpen, "${ficha.projects} Proyectos", Modifier.weight(1f))
            }

            HorizontalDivider(color = senaColors().borderSoft)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                FichaActionButton(Icons.Default.Visibility, "Detalle", onViewDetail)
                FichaActionButton(Icons.Default.ContactPage, "Directorio", onViewDirectory)
                FichaActionButton(Icons.Default.Edit, "Editar", onEdit)
            }
        }
    }
}

@Composable
fun FichaStatMini(icon: ImageVector, text: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = senaColors().textLight, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.labelSmall, color = senaColors().textSecondary)
    }
}

@Composable
fun FichaActionButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
        modifier = Modifier.height(36.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}

// Missing icons
val Icons.Filled.LayerGroup: ImageVector get() = Icons.Default.Layers

@Preview(showBackground = true)
@Composable
fun ManageFichasScreenPreview() {
    ProyecTwinTheme {
        ManageFichasScreen(onBack = {}, onViewDetail = {}, onViewDirectory = {}, onCreateFicha = {}, onNavigate = {})
    }
}
