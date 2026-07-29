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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectwin.data.mock.MockDataProvider
import com.example.proyectwin.data.model.BugReport
import com.example.proyectwin.data.model.BugReportStatus
import com.example.proyectwin.data.model.BugReportType
import com.example.proyectwin.navigation.AppNavigation
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*
import com.example.proyectwin.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BugReportsScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<BugReportType?>(null) }
    var selectedStatus by remember { mutableStateOf<BugReportStatus?>(null) }

    val reports = remember { MockDataProvider.getAllBugReports() }

    val typeFilters = remember { listOf(null) + BugReportType.entries.toList() }
    val statusFilters = remember { listOf(null) + BugReportStatus.entries.toList() }

    val filteredReports = reports.filter { report ->
        val matchesType = selectedType == null || report.bugType == selectedType
        val matchesStatus = selectedStatus == null || report.bugStatus == selectedStatus
        val matchesSearch = report.reporterName?.contains(searchQuery, ignoreCase = true) == true ||
                report.descripcion.contains(searchQuery, ignoreCase = true) ||
                report.titulo.contains(searchQuery, ignoreCase = true)
        matchesType && matchesStatus && matchesSearch
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
        containerColor = senaColors().background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                SenaPageHeader(
                    title = "Reportes de Fallas",
                    subtitle = "Supervisa y gestiona los errores técnicos reportados por los usuarios.",
                    icon = Icons.Default.BugReport
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
                            color = senaColors().textLight,
                            letterSpacing = 0.5.sp
                        )
                        SenaTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = "",
                            placeholder = "Buscar por usuario o descripción...",
                            leadingIcon = Icons.Default.Search
                        )

                        Text(
                            "Tipo de falla",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = senaColors().textLight,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            typeFilters.forEach { type ->
                                SenaChip(
                                    text = type?.let {
                                        when (it) {
                                            BugReportType.FUNCIONAL -> "Funcional"
                                            BugReportType.VISUAL -> "Visual"
                                            BugReportType.RENDIMIENTO -> "Rendimiento"
                                            BugReportType.SEGURIDAD -> "Seguridad"
                                            BugReportType.OTRO -> "Otro"
                                        }
                                    } ?: "Todos",
                                    color = when (type) {
                                        BugReportType.FUNCIONAL -> senaColors().info
                                        BugReportType.VISUAL -> senaColors().warning
                                        BugReportType.RENDIMIENTO -> senaColors().danger
                                        BugReportType.SEGURIDAD -> senaColors().danger
                                        else -> senaColors().green
                                    },
                                    isSelected = selectedType == type,
                                    onClick = { selectedType = type }
                                )
                            }
                        }

                        Text(
                            "Estado del reporte",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = senaColors().textLight,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            statusFilters.forEach { status ->
                                SenaChip(
                                    text = status?.let {
                                        when (it) {
                                            BugReportStatus.PENDIENTE -> "Pendiente"
                                            BugReportStatus.EN_REVISION -> "En Revisión"
                                            BugReportStatus.RESUELTO -> "Resuelto"
                                            BugReportStatus.CERRADO -> "Cerrado"
                                        }
                                    } ?: "Todos",
                                    color = when (status) {
                                        BugReportStatus.RESUELTO -> senaColors().success
                                        BugReportStatus.PENDIENTE -> senaColors().warning
                                        BugReportStatus.EN_REVISION -> senaColors().info
                                        BugReportStatus.CERRADO -> senaColors().textLight
                                        else -> senaColors().green
                                    },
                                    isSelected = selectedStatus == status,
                                    onClick = { selectedStatus = status }
                                )
                            }
                        }
                    }
                }
            }

            if (filteredReports.isEmpty()) {
                item {
                    SenaEmptyState(
                        message = "No se encontraron reportes que coincidan con la búsqueda.",
                        icon = Icons.Default.SearchOff
                    )
                }
            } else {
                items(filteredReports) { report ->
                    BugReportCard(report) {
                        onNavigate(AppNavigation.ADMIN_BUG_DETAIL.replace("{bugId}", report.id.toString()))
                    }
                }
            }

            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}

@Composable
fun BugReportCard(report: BugReport, onClick: () -> Unit) {
    SenaCard(elevation = 1.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                        "#${report.id}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = senaColors().textSecondary
                    )
                }
                SenaStatusBadge(status = report.statusDisplay)
            }

            Column {
                Text(
                    report.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = senaColors().text,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = senaColors().green, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(report.reporterName ?: "Anónimo", style = MaterialTheme.typography.labelSmall, color = senaColors().textSecondary)
                    Spacer(Modifier.weight(1f))
                    Surface(
                        color = when (report.bugType) {
                            BugReportType.FUNCIONAL -> senaColors().info.copy(alpha = 0.1f)
                            BugReportType.VISUAL -> senaColors().warning.copy(alpha = 0.1f)
                            BugReportType.RENDIMIENTO -> senaColors().danger.copy(alpha = 0.1f)
                            BugReportType.SEGURIDAD -> senaColors().danger.copy(alpha = 0.1f)
                            BugReportType.OTRO -> senaColors().textLight.copy(alpha = 0.1f)
                        },
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            report.typeDisplay,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = when (report.bugType) {
                                BugReportType.FUNCIONAL -> senaColors().info
                                BugReportType.VISUAL -> senaColors().warning
                                BugReportType.RENDIMIENTO -> senaColors().danger
                                BugReportType.SEGURIDAD -> senaColors().danger
                                BugReportType.OTRO -> senaColors().textLight
                            },
                            fontSize = 9.sp
                        )
                    }
                }
            }

            HorizontalDivider(color = senaColors().borderSoft)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onClick) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Ver Detalle", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onClick) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Gestionar", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BugReportsScreenPreview() {
    ProyecTwinTheme {
        BugReportsScreen(onBack = {}, onNavigate = {})
    }
}
