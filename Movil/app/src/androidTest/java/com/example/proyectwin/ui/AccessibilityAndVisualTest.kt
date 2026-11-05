package com.example.proyectwin.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import com.example.proyectwin.ui.components.SenaButton
import com.example.proyectwin.ui.components.SenaCard
import com.example.proyectwin.ui.components.SenaTextField
import com.example.proyectwin.ui.components.TwoPaneListDetail
import com.example.proyectwin.ui.theme.ProyecTwinTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Verificación de accesibilidad básica y captura visual de componentes.
 * No sustituye una revisión con TalkBack, pero fija semántica, área táctil y
 * que el render no quede en blanco.
 */
class AccessibilityAndVisualTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun botonesYcampos_tienenSemaanticaYAreaTactil() {
        compose.setContent {
            ProyecTwinTheme {
                Column(modifier = Modifier.padding(16.dp)) {
                    SenaTextField(
                        value = "ana@example.com",
                        onValueChange = {},
                        label = "Correo electrónico",
                    )
                    SenaButton(text = "Entrar", onClick = {})
                }
            }
        }

        compose.onNodeWithText("Correo electrónico").assertExists()
        compose.onNodeWithText("Entrar")
            .assertHasClickAction()
            .assertHeightIsAtLeast(48.dp)
    }

    @Test
    fun dosPanelesExpanded_muestraListaYDetalle() {
        compose.setContent {
            ProyecTwinTheme {
                TwoPaneListDetail(
                    expanded = true,
                    selectedId = 7,
                    onSelected = {},
                    list = { Text("Lista de prueba") },
                    detail = { id, _ -> Text("Detalle $id") },
                )
            }
        }

        compose.onNodeWithText("Lista de prueba").assertExists()
        compose.onNodeWithText("Detalle 7").assertExists()
    }

    @Test
    fun dosPanelesSinSeleccion_muestraEstadoVacio() {
        compose.setContent {
            ProyecTwinTheme {
                TwoPaneListDetail(
                    expanded = true,
                    selectedId = null,
                    onSelected = {},
                    list = { Text("Lista disponible") },
                    detail = { id, _ -> Text("Detalle $id") },
                )
            }
        }

        compose.onNodeWithText("Lista disponible").assertExists()
        compose.onNodeWithText("Sin selección").assertExists()
    }

    @Test
    fun tarjetaSeleccionada_seRenderiza() {
        compose.setContent {
            ProyecTwinTheme {
                SenaCard(selected = true) {
                    Text("Tarjeta seleccionada")
                }
            }
        }

        compose.onNodeWithText("Tarjeta seleccionada").assertExists()
    }

    @Test
    fun renderDeComponentes_noQuedaEnBlanco() {
        compose.setContent {
            ProyecTwinTheme {
                Column(modifier = Modifier.padding(16.dp)) {
                    SenaButton(text = "Acción principal", onClick = {})
                }
            }
        }

        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        assertTrue("La captura debe tener ancho", bitmap.width > 100)
        assertTrue("La captura debe tener alto", bitmap.height > 100)

        val colores = mutableSetOf<Int>()
        var x = 0
        while (x < bitmap.width) {
            var y = 0
            while (y < bitmap.height) {
                colores.add(bitmap.getPixel(x, y))
                y += 8
            }
            x += 8
        }
        assertTrue("El render debe tener más de un color", colores.size > 2)
    }
}
