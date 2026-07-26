package com.example.proyectwin.ui.screens.aprendiz

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun AnalyzingProjectScreen(onCancel: () -> Unit, onAnalysisComplete: () -> Unit) {
    var progress by remember { mutableStateOf(0f) }
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        while (progress < 1f) {
            delay(100)
            progress += 0.02f
        }
        delay(500)
        onAnalysisComplete()
    }

    Scaffold(
        topBar = {
            SenaTopBar(
                title = "Análisis IA",
                onBack = onCancel,
                showProfile = true,
                showNotifications = true
            )
        },
        containerColor = SenaBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SenaCard(elevation = 2.dp) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // IA Spinner Icon
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        color = SenaGreen.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = SenaGreen,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }

                    Text(
                        "Analizando tu proyecto",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = SenaText,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        "Nuestro sistema de inteligencia artificial está revisando tu proyecto en busca de posibles similitudes con otros trabajos registrados.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SenaTextSecondary,
                        textAlign = TextAlign.Center
                    )

                    // Progress Bar
                    Column(modifier = Modifier.fillMaxWidth()) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(CircleShape),
                            color = SenaGreen,
                            trackColor = SenaBorderSoft
                        )
                    }

                    // Steps List
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AnalysisStepItem("Resumen recibido", isCompleted = progress > 0.1f)
                        AnalysisStepItem("Palabras clave identificadas", isCompleted = progress > 0.4f)
                        AnalysisStepItem("Analizando similitudes...", isActive = progress in 0.4f..0.8f, isCompleted = progress > 0.8f)
                        AnalysisStepItem("Generando recomendaciones", isCompleted = progress > 0.95f)
                    }

                    SenaAlertBanner(
                        title = "Procesamiento IA",
                        message = "Este proceso puede tomar unos segundos. Por favor no cierres la aplicación.",
                        icon = Icons.Default.Info,
                        color = SenaInfo
                    )

                    SenaButton(
                        text = "Cancelar Análisis",
                        onClick = onCancel,
                        isPrimary = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun AnalysisStepItem(text: String, isCompleted: Boolean = false, isActive: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (isCompleted) Icons.Default.CheckCircle else if (isActive) Icons.Default.Sync else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isCompleted) SenaSuccess else if (isActive) SenaGreen else SenaTextMuted,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isCompleted || isActive) SenaText else SenaTextMuted
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AnalyzingProjectScreenPreview() {
    ProyecTwinTheme {
        AnalyzingProjectScreen(onCancel = {}, onAnalysisComplete = {})
    }
}
