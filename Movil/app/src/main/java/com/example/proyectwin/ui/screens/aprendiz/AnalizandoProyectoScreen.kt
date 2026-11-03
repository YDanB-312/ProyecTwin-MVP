package com.example.proyectwin.ui.screens.aprendiz

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.proyectwin.ui.components.SenaAlertBanner
import com.example.proyectwin.ui.components.SenaButton
import com.example.proyectwin.ui.components.SenaCard
import com.example.proyectwin.ui.components.SenaPageHeader
import com.example.proyectwin.ui.components.SenaTopBar
import com.example.proyectwin.ui.theme.senaColors
import com.example.proyectwin.ui.viewmodel.ResultadoAnalisisUiState
import com.example.proyectwin.ui.viewmodel.ResultadoAnalisisViewModel
import kotlinx.coroutines.delay

private val PASOS = listOf(
    "Obteniendo el contenido de la propuesta...",
    "Comparando con proyectos de la base de datos...",
    "Calculando porcentajes de similitud...",
    "Generando recomendaciones...",
)

private val TIPS = listOf(
    "Los proyectos con descripciones detalladas obtienen análisis más precisos.",
    "Cita siempre las fuentes que usaste en tu documentación.",
    "Puedes ver el detalle de cada similitud desde tu proyecto.",
    "Un porcentaje bajo no garantiza originalidad: revisa las coincidencias.",
    "Actualiza tus palabras clave para mejorar las comparaciones futuras.",
)

/**
 * Confirmación real de que el motor ya dejó disponible el resultado. La barra
 * solo avanza hasta 90% mientras el backend responde; no simula un 100%.
 */
@Composable
fun AnalizandoProyectoScreen(
    projectId: Int,
    miembrosFallidos: Int = 0,
    onFinished: (Int) -> Unit,
    onBack: () -> Unit,
    resultadoViewModel: ResultadoAnalisisViewModel = hiltViewModel(),
) {
    val estado by resultadoViewModel.uiState.collectAsState()
    var progreso by remember { mutableIntStateOf(0) }
    var tip by remember { mutableIntStateOf(0) }

    LaunchedEffect(projectId) {
        if (projectId > 0) resultadoViewModel.load(projectId) else onBack()
    }
    LaunchedEffect(Unit) {
        while (progreso < 90) {
            delay(90)
            progreso = (progreso + 2).coerceAtMost(90)
        }
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1800)
            tip = (tip + 1) % TIPS.size
        }
    }
    LaunchedEffect(estado) {
        if (estado is ResultadoAnalisisUiState.Success) {
            progreso = 100
            delay(500)
            onFinished(projectId)
        }
    }

    Scaffold(
        topBar = {
            SenaTopBar(
                title = "Analizando proyecto",
                onBack = onBack,
                showProfile = false,
                showNotifications = false,
            )
        },
        containerColor = senaColors().background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (estado is ResultadoAnalisisUiState.Error) {
                SenaAlertBanner(
                    title = "No se pudo analizar la propuesta",
                    message = (estado as ResultadoAnalisisUiState.Error).message,
                    icon = Icons.Default.Error,
                    color = senaColors().danger,
                )
                SenaButton(text = "REINTENTAR ANÁLISIS", onClick = { resultadoViewModel.retry() }, icon = Icons.Default.Timelapse)
                SenaButton(
                    text = "VOLVER A MIS PROPUESTAS",
                    onClick = onBack,
                    isPrimary = false,
                    icon = Icons.Default.RadioButtonUnchecked,
                )
                return@Column
            }

            SenaPageHeader(
                title = "Analizando propuesta...",
                subtitle = "ProyecTwin compara tu propuesta con la base académica. Solo toma unos segundos.",
                icon = Icons.Default.Science,
            )

            if (miembrosFallidos > 0) {
                SenaAlertBanner(
                    title = "Equipo incompleto",
                    message = "La propuesta se creó, pero no se pudieron vincular $miembrosFallidos integrante(s) del equipo. Puedes agregarlos luego desde el detalle.",
                    icon = Icons.Default.Warning,
                    color = senaColors().warning,
                )
            }

            SenaCard(elevation = 4.dp) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(132.dp),
                            color = senaColors().green,
                            trackColor = senaColors().borderSoft,
                            strokeWidth = 10.dp,
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "$progreso%",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = senaColors().green,
                            )
                            Text(
                                "procesando",
                                style = MaterialTheme.typography.labelSmall,
                                color = senaColors().textLight,
                            )
                        }
                    }
                    LinearProgressIndicator(
                        progress = { progreso / 100f },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = senaColors().green,
                        trackColor = senaColors().borderSoft,
                    )
                    val pasoActual = (progreso / 25).coerceIn(0, PASOS.lastIndex)
                    PASOS.forEachIndexed { index, paso ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(
                                when {
                                    index < pasoActual -> Icons.Default.CheckCircle
                                    index == pasoActual -> Icons.Default.Timelapse
                                    else -> Icons.Default.RadioButtonUnchecked
                                },
                                contentDescription = null,
                                tint = when {
                                    index < pasoActual -> senaColors().success
                                    index == pasoActual -> senaColors().green
                                    else -> senaColors().textMuted
                                },
                                modifier = Modifier.size(20.dp),
                            )
                            Text(
                                paso,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (index <= pasoActual) senaColors().text else senaColors().textMuted,
                            )
                        }
                    }
                }
            }

            Surface(
                color = senaColors().warning10,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = senaColors().warning)
                    Text(
                        "Tip: ${TIPS[tip]}",
                        style = MaterialTheme.typography.bodySmall,
                        color = senaColors().textSecondary,
                    )
                }
            }
        }
    }
}
