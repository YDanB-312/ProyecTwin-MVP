package com.example.proyectwin.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.proyectwin.data.model.DetalleSimilitud
import com.example.proyectwin.ui.theme.senaColors
import kotlin.math.roundToInt

/**
 * Desglose del motor que la web muestra en el detalle de similitud: señales
 * léxicas (palabras/caracteres), tema, cobertura, términos compartidos y
 * pasajes. Es la explicación del porcentaje, no un veredicto.
 */
@Composable
fun DetalleMotorSimilitud(detalles: DetalleSimilitud?) {
    if (detalles == null) return
    SenaSectionHeader(title = "Desglose del motor")
    SenaCard(elevation = 1.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricaMotor("Palabras", detalles.palabras.toString(), Modifier.weight(1f))
                MetricaMotor("Caracteres", detalles.caracteres.toString(), Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricaMotor("Tema", detalles.tema ?: "—", Modifier.weight(1f))
                MetricaMotor("Cobertura", "${detalles.cobertura}%", Modifier.weight(1f))
            }

            if (detalles.terminos.isNotEmpty()) {
                HorizontalDivider(color = senaColors().borderSoft)
                Text(
                    "Términos compartidos",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = senaColors().text,
                )
                detalles.terminos.take(9).chunked(3).forEach { fila ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        fila.forEach { termino ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = senaColors().green.copy(alpha = 0.1f),
                            ) {
                                Text(
                                    termino,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = senaColors().green,
                                )
                            }
                        }
                    }
                }
            }

            if (detalles.pasajes.isNotEmpty()) {
                HorizontalDivider(color = senaColors().borderSoft)
                Text(
                    "Fragmentos coincidentes",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = senaColors().text,
                )
                detalles.pasajes.take(6).forEach { pasaje ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("A", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = senaColors().green)
                            Text(
                                "${(pasaje.score * 100).roundToInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = senaColors().textSecondary,
                            )
                        }
                        Text(
                            pasaje.a,
                            style = MaterialTheme.typography.bodySmall,
                            color = senaColors().textSecondary,
                        )
                        Text(
                            pasaje.b,
                            style = MaterialTheme.typography.bodySmall,
                            color = senaColors().text,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricaMotor(etiqueta: String, valor: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = senaColors().backgroundElevated,
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(etiqueta, style = MaterialTheme.typography.labelSmall, color = senaColors().textLight)
            Text(
                valor,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = senaColors().text,
            )
        }
    }
}
