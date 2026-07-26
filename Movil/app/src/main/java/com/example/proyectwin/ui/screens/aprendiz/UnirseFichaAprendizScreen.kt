package com.example.proyectwin.ui.screens.aprendiz

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnirseFichaAprendizScreen(onBack: () -> Unit, onJoined: () -> Unit) {
    var codigoFicha by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            SenaTopBar(
                title = "ProyecTwin",
                onBack = onBack,
                showProfile = true,
                showNotifications = true
            )
        },
        containerColor = SenaBackground,
        bottomBar = {
            SenaBottomBar {
                SenaButton(
                    text = "Unirse a la Ficha",
                    icon = Icons.AutoMirrored.Filled.Login,
                    onClick = onJoined,
                    modifier = Modifier.weight(1f)
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
                title = "Unirse a una Ficha",
                subtitle = "Ingresa el código de tu programa para unirte al grupo de formación.",
                icon = Icons.Default.PersonAdd
            )

            // Instruction Card
            SenaCard(elevation = 1.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            color = SenaGreen.copy(alpha = 0.1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Numbers, contentDescription = null, tint = SenaGreen)
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("Código de Acceso", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SenaText)
                            Text("Solicita el código a tu instructor", style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
                        }
                    }

                    SenaTextField(
                        value = codigoFicha,
                        onValueChange = { codigoFicha = it },
                        label = "Ingresa el código *",
                        placeholder = "Ej: 2568421",
                        leadingIcon = Icons.Default.Key
                    )
                    
                    SenaAlertBanner(
                        title = "Información Importante",
                        message = "Al unirte a una ficha, tus instructores podrán ver tus proyectos y realizar observaciones.",
                        icon = Icons.Default.Info,
                        color = SenaInfo
                    )
                }
            }

            SenaSectionHeader(title = "Beneficios de unirse")
            
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                UnirseBenefitItem(
                    icon = Icons.Default.Groups,
                    title = "Trabajo Colaborativo",
                    desc = "Forma parte del grupo de trabajo con tus compañeros de ficha."
                )
                UnirseBenefitItem(
                    icon = Icons.Default.Task,
                    title = "Seguimiento Directo",
                    desc = "Recibe observaciones y correcciones de tus instructores en tiempo real."
                )
                UnirseBenefitItem(
                    icon = Icons.Default.Timeline,
                    title = "Historial de Avance",
                    desc = "Mantén un registro claro de la evolución de tus proyectos."
                )
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun UnirseBenefitItem(icon: ImageVector, title: String, desc: String) {
    Row(verticalAlignment = Alignment.Top) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = RoundedCornerShape(10.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, SenaBorderSoft)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = SenaGreen, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = SenaText)
            Text(desc, style = MaterialTheme.typography.labelSmall, color = SenaTextSecondary, lineHeight = 16.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UnirseFichaAprendizScreenPreview() {
    ProyecTwinTheme {
        UnirseFichaAprendizScreen(onBack = {}, onJoined = {})
    }
}
