package com.example.proyectwin.ui.screens.instructor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.example.proyectwin.ui.viewmodel.FichasUiState
import com.example.proyectwin.ui.viewmodel.FichasViewModel
import kotlinx.coroutines.launch

data class MemberItem(val name: String, val email: String, val status: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleFichaInstructorScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    fichaId: String = "",
    authViewModel: AuthViewModel = viewModel(),
    fichasViewModel: FichasViewModel = viewModel()
) {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val uiState by fichasViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        fichasViewModel.loadActiveFichas()
    }

    val allFichas = (uiState as? FichasUiState.Success)?.fichas.orEmpty()
    val ficha = allFichas.firstOrNull { it.codigo == fichaId || it.id.toString() == fichaId }

    val members = ficha?.estudiantes?.map { student ->
        MemberItem(
            name = student.name,
            email = student.email,
            status = "Activo"
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
        bottomBar = {
            if (ficha != null) {
                SenaBottomBar {
                    SenaButton(
                        text = "Copiar Código", 
                        onClick = { 
                            scope.launch {
                                snackbarHostState.showSnackbar("Código copiado al portapapeles")
                            }
                        }, 
                        icon = Icons.Default.ContentCopy, 
                        modifier = Modifier.weight(1f)
                    )
                    SenaButton(
                        text = "Editar Ficha",
                        onClick = { onNavigate(AppNavigation.INSTRUCTOR_CREAR_FICHA) },
                        isPrimary = false,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is FichasUiState.Loading -> Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = senaColors().green)
            }
            is FichasUiState.Error -> Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                SenaErrorState(message = state.message, onRetry = { fichasViewModel.loadActiveFichas() })
            }
            is FichasUiState.Success -> {
                if (ficha == null) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Ficha no encontrada", color = senaColors().textMuted)
                    }
                } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                SenaPageHeader(
                    title = "Gestión de Ficha",
                    subtitle = "Información detallada y administración de aprendices por grupo.",
                    icon = Icons.Default.Groups
                )

                // Header Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Brush.linearGradient(colors = listOf(senaColors().header, senaColors().green)))
                        .padding(24.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(ficha.codigo, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(ficha.programa, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                            }
                            SenaStatusBadge(status = ficha.statusDisplay)
                        }
                        
                        HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.padding(end = 12.dp),
                                color = Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "Código: ${ficha.codigo}", 
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall, 
                                    fontWeight = FontWeight.Black, 
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(Icons.Default.School, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Trimestre 3", style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }
                    }
                }

                SenaSectionHeader(
                    title = "Aprendices (${members.size})",
                    actionText = "Exportar",
                    onActionClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Exportando listado de aprendices...")
                        }
                    }
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    members.forEach { member ->
                        SenaCard(elevation = 0.5.dp) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(40.dp),
                                    shape = CircleShape,
                                    color = if (member.status == "Activo") senaColors().green.copy(alpha = 0.1f) else senaColors().borderSoft
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            member.name.take(1), 
                                            fontWeight = FontWeight.Bold, 
                                            color = if (member.status == "Activo") senaColors().green else senaColors().textMuted
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(member.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = senaColors().text)
                                    Text(member.email, style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
                                }
                                SenaStatusBadge(status = member.status)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(40.dp))
            }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetalleFichaInstructorPreview() {
    ProyecTwinTheme {
        DetalleFichaInstructorScreen(onBack = {}, onNavigate = {})
    }
}
