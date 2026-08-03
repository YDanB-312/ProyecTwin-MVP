package com.example.proyectwin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectwin.data.mock.MockDataProvider
import com.example.proyectwin.data.model.Notification
import com.example.proyectwin.data.model.NotificationType
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*
import com.example.proyectwin.ui.viewmodel.AuthUiState
import com.example.proyectwin.ui.viewmodel.AuthViewModel

enum class AlertType(val label: String) {
    URGENT("Urgente"),
    WARNING("Advertencia"),
    INFO("Informativa"),
    SUCCESS("Éxito")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    onNavigate: (String) -> Unit,
    onBack: () -> Unit = {},
    profileRoute: String = "aprendiz_profile",
    similarityRoute: String = "aprendiz_similarity/{projectId}",
    detailRoute: String = "aprendiz_detail/{id}",
    bottomBar: @Composable () -> Unit = {},
    authViewModel: AuthViewModel = viewModel()
) {
    val uiState by authViewModel.uiState.collectAsState()
    val user = (uiState as? AuthUiState.LoggedIn)?.user

    var refreshTrigger by remember { mutableIntStateOf(0) }
    var isRefreshing by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("Todos") }
    val filters = listOf("Todos", "Similitud", "Instructor", "Sistema")

    val allAlerts = remember(user, refreshTrigger) {
        MockDataProvider.getNotificationsByUser(user?.id ?: 1)
    }

    val filteredAlerts = if (selectedFilter == "Todos") {
        allAlerts
    } else {
        allAlerts.filter { alert ->
            val category = when (alert.notifType) {
                NotificationType.INFO, NotificationType.SUCCESS -> "Sistema"
                NotificationType.WARNING, NotificationType.ERROR -> "Similitud"
            }
            category == selectedFilter
        }
    }

    Scaffold(
        topBar = {
            SenaTopBar(
                title = "Notificaciones",
                onBack = onBack,
                showNotifications = false,
                onNavigateToProfile = { onNavigate(profileRoute) },
            )
        },
        containerColor = senaColors().background,
        bottomBar = bottomBar
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
                    title = "Centro de Alertas",
                    subtitle = "Mantente al día con el estado de tus proyectos y observaciones.",
                    icon = Icons.Default.Notifications
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filters) { filter ->
                        SenaChip(
                            text = filter,
                            color = senaColors().green,
                            isSelected = selectedFilter == filter,
                            onClick = { selectedFilter = filter }
                        )
                    }
                }
            }

            if (filteredAlerts.isEmpty()) {
                item {
                    SenaEmptyState(
                        message = "No tienes notificaciones en esta categoría.",
                        icon = Icons.Default.NotificationsNone
                    )
                }
            } else {
                items(filteredAlerts, key = { it.id }) { alert ->
                    NotificationCard(alert, onClick = {
                        MockDataProvider.markNotificationAsRead(alert.id)
                        refreshTrigger++
                        val category = when (alert.notifType) {
                            NotificationType.INFO, NotificationType.SUCCESS -> "Sistema"
                            NotificationType.WARNING, NotificationType.ERROR -> "Similitud"
                        }
                        val projectId = alert.projectId ?: 1
                        if (category == "Similitud") onNavigate(similarityRoute.replace("{projectId}", projectId.toString()))
                        else onNavigate(detailRoute.replace("{id}", projectId.toString()))
                    })
                }
            }

            item { Spacer(Modifier.height(40.dp)) }
        }
        }
    }

    LaunchedEffect(refreshTrigger) {
        kotlinx.coroutines.delay(500)
        isRefreshing = false
    }
}

@Composable
fun NotificationCard(alert: Notification, onClick: () -> Unit) {
    val alertType = when (alert.notifType) {
        NotificationType.INFO -> AlertType.INFO
        NotificationType.WARNING -> AlertType.WARNING
        NotificationType.SUCCESS -> AlertType.SUCCESS
        NotificationType.ERROR -> AlertType.URGENT
    }
    val (icon, color) = when (alertType) {
        AlertType.URGENT -> Icons.Default.Warning to senaColors().danger
        AlertType.WARNING -> Icons.Default.History to senaColors().warning
        AlertType.INFO -> Icons.AutoMirrored.Filled.Comment to senaColors().green
        AlertType.SUCCESS -> Icons.Default.CheckCircle to senaColors().success
    }

    val category = when (alert.notifType) {
        NotificationType.INFO, NotificationType.SUCCESS -> "Sistema"
        NotificationType.WARNING, NotificationType.ERROR -> "Similitud"
    }

    val title = when (alert.notifType) {
        NotificationType.INFO -> "Novedad"
        NotificationType.WARNING -> "Advertencia"
        NotificationType.SUCCESS -> "Logro"
        NotificationType.ERROR -> "Urgente"
    }

    SenaCard(
        elevation = if (!alert.leido) 2.dp else 0.5.dp,
        onClick = onClick,
        containerColor = Color.White
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(64.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = color.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        category.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = color,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        alert.createdAt ?: "Recientemente",
                        style = MaterialTheme.typography.labelSmall,
                        color = senaColors().textLight
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = senaColors().text
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    alert.mensaje,
                    style = MaterialTheme.typography.bodySmall,
                    color = senaColors().textSecondary,
                    lineHeight = 18.sp
                )

                if (!alert.leido) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = senaColors().green.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "NUEVO",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = senaColors().green,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AlertsScreenPreview() {
    ProyecTwinTheme {
        AlertsScreen(onNavigate = {})
    }
}
