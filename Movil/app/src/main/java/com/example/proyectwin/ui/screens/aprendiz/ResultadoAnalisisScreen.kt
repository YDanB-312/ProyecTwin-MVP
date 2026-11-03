package com.example.proyectwin.ui.screens.aprendiz

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.proyectwin.data.model.Similarity
import com.example.proyectwin.ui.components.SenaButton
import com.example.proyectwin.ui.components.SenaCard
import com.example.proyectwin.ui.components.SenaEmptyState
import com.example.proyectwin.ui.components.SenaErrorState
import com.example.proyectwin.ui.components.SenaLoadingState
import com.example.proyectwin.ui.components.SenaPageHeader
import com.example.proyectwin.ui.components.SenaSectionHeader
import com.example.proyectwin.ui.components.SenaTopBar
import com.example.proyectwin.ui.theme.senaColors
import com.example.proyectwin.ui.viewmodel.ResultadoAnalisisData
import com.example.proyectwin.ui.viewmodel.ResultadoAnalisisUiState
import com.example.proyectwin.ui.viewmodel.ResultadoAnalisisViewModel
import kotlin.math.roundToInt

/**
 * Resultado del motor para una propuesta propia: máxima coincidencia, grado
 * relativo al umbral real, listado de pares y recomendaciones.
 */
@Composable
fun ResultadoAnalisisScreen(
    projectId: Int,
    onBack: () -> Unit,
    onProjectDetail: (Int) -> Unit,
    onSimilarityDetail: (Int) -> Unit,
    resultadoViewModel: ResultadoAnalisisViewModel = hiltViewModel(),
) {
    val estado by resultadoViewModel.uiState.collectAsState()

    LaunchedEffect(projectId) {
        if (projectId > 0) resultadoViewModel.load(projectId) else onBack()
    }

    Scaffold(
        topBar = {
            SenaTopBar(
                title = "Resultado del análisis",
                onBack = onBack,
                showProfile = true,
                showNotifications = true,
            )
        },
        containerColor = senaColors().background,
    ) { padding ->
        when (val actual = estado) {
            is ResultadoAnalisisUiState.Loading ->
                SenaLoadingState(modifier = Modifier.padding(padding))
            is ResultadoAnalisisUiState.Error -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                SenaErrorState(message = actual.message, onRetry = { resultadoViewModel.retry() })
            }
            is ResultadoAnalisisUiState.Success -> {
                val data = actual.data
                if (!data.esMia) {
                    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        SenaEmptyState(
                            title = "Análisis no autorizado",
                            message = "Este análisis no pertenece a ninguna de tus propuestas.",
                            icon = Icons.Default.Warning,
                        )
                    }
                    return@Scaffold
                }
                if (data.pares.isEmpty()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        item {
                            SenaEmptyState(
                                title = "Sin coincidencias detectadas",
                                message = "Buenas noticias: \"${data.proyecto.title}\" no presenta similitudes con los demás proyectos de la base.",
                                icon = Icons.Default.CheckCircle,
                            )
                        }
                        item {
                            SenaButton(
                                text = "VOLVER A MIS PROPUESTAS",
                                onClick = onBack,
                                icon = Icons.Default.Folder,
                            )
                        }
                    }
                    return@Scaffold
                }
                ResultadoContenido(
                    data = data,
                    modifier = Modifier.padding(padding),
                    onProjectDetail = onProjectDetail,
                    onSimilarityDetail = onSimilarityDetail,
                    onBack = onBack,
                )
            }
        }
    }
}

@Composable
private fun ResultadoContenido(
    data: ResultadoAnalisisData,
    modifier: Modifier,
    onProjectDetail: (Int) -> Unit,
    onSimilarityDetail: (Int) -> Unit,
    onBack: () -> Unit,
) {
    val colorNivel = when (data.nivel) {
        "alta" -> senaColors().danger
        "media" -> senaColors().warning
        else -> senaColors().success
    }
    val recomendaciones = when (data.nivel) {
        "alta" -> listOf(
            "Revisa las secciones con mayor coincidencia y reescríbelas con tus propias palabras.",
            "Cita adecuadamente todas las fuentes y referencias utilizadas.",
            "Considera rediseñar el enfoque o el alcance para diferenciar tu propuesta.",
            "Conversa con tu instructor sobre los hallazgos del análisis.",
        )
        "media" -> listOf(
            "Compara tu documentación con los proyectos similares y refuerza tus aportes propios.",
            "Amplía la descripción de tu metodología y resultados.",
            "Verifica que las citas y referencias estén completas.",
        )
        else -> listOf(
            "¡Buen trabajo! Tu proyecto muestra un nivel bajo de coincidencia.",
            "Continúa documentando con detalle tu proceso y fuentes.",
            "Documenta este resultado como evidencia de originalidad.",
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            SenaPageHeader(
                title = "Resultado del análisis",
                subtitle = "${data.proyecto.title} · ${data.pares.size} coincidencia(s) detectada(s)",
                icon = Icons.Default.Compare,
            )
        }

        item {
            SenaCard(elevation = 4.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = CircleShape,
                        color = colorNivel.copy(alpha = 0.12f),
                        modifier = Modifier.size(116.dp),
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                "${data.pctMaximo}%",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = colorNivel,
                            )
                            Text(
                                "coincidencia máxima",
                                style = MaterialTheme.typography.labelSmall,
                                color = senaColors().textLight,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            color = colorNivel.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(
                                "Coincidencia ${data.nivel} · grado ${data.grado}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = colorNivel,
                            )
                        }
                        Text(
                            when (data.nivel) {
                                "alta" -> "El sistema encontró coincidencias significativas con otros proyectos registrados."
                                "media" -> "Existen coincidencias parciales que vale la pena revisar."
                                else -> "Tu proyecto es mayormente original frente a la base de datos."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = senaColors().textSecondary,
                        )
                        Text(
                            "Umbral vigente del motor: ${data.umbralPct}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = senaColors().textMuted,
                        )
                    }
                }
            }
        }

        item {
            SenaSectionHeader(title = "Proyectos con similitud (${data.pares.size})")
        }

        items(data.pares, key = { it.id }) { par ->
            FilaResultado(
                par = par,
                baseId = data.proyecto.id,
                onClick = { onSimilarityDetail(par.id) },
            )
        }

        item {
            SenaCard(elevation = 1.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = senaColors().warning)
                    Spacer(Modifier.size(8.dp))
                    Text(
                        "Recomendaciones",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = senaColors().text,
                    )
                }
                HorizontalDivider(color = senaColors().borderSoft)
                recomendaciones.forEach { recomendacion ->
                    Text(
                        "• $recomendacion",
                        style = MaterialTheme.typography.bodySmall,
                        color = senaColors().textSecondary,
                    )
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SenaButton(
                    text = "VOLVER A MIS PROPUESTAS",
                    onClick = onBack,
                    isPrimary = false,
                    icon = Icons.Default.Folder,
                )
                SenaButton(
                    text = "VER MI PROYECTO",
                    onClick = { onProjectDetail(data.proyecto.id) },
                    icon = Icons.Default.Search,
                )
            }
        }

        item { Spacer(Modifier.height(30.dp)) }
    }
}

@Composable
private fun FilaResultado(
    par: Similarity,
    baseId: Int,
    onClick: () -> Unit,
) {
    val contraparteEsSegunda = par.projectId1 == baseId
    val otro = if (contraparteEsSegunda) par.project2 else par.project1
    val titulo = otro?.title
        ?: (if (contraparteEsSegunda) par.project2Title else par.project1Title)
        ?: "Proyecto no disponible"
    val estudiante = if (contraparteEsSegunda) par.project2Student else par.project1Student
    val pct = (par.similitud * 100).roundToInt().coerceIn(0, 100)

    SenaCard(elevation = 1.dp, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    titulo,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = senaColors().text,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    estudiante?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            "Aprendiz: $it",
                            style = MaterialTheme.typography.labelSmall,
                            color = senaColors().textLight,
                        )
                    }
                    par.fecha?.take(10)?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.labelSmall,
                            color = senaColors().textLight,
                        )
                    }
                }
            }
            Text(
                "$pct%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = senaColors().green,
            )
        }
    }
}
