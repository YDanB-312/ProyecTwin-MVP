package com.example.proyectwin.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeContrastTest {

    @Test
    fun elContenidoDelHeaderCumpleContrasteEnAmbosTemas() {
        listOf(lightSenaColors(), darkSenaColors()).forEach { esquema ->
            assertTrue(
                "El título del manual no cumple contraste AA",
                contraste(esquema.onHeader, esquema.header) >= 4.5,
            )
            assertTrue(
                "El texto secundario del manual no cumple contraste AA",
                contraste(esquema.onHeaderMuted, esquema.header) >= 4.5,
            )
        }
    }

    private fun contraste(a: Color, b: Color): Double {
        val la = luminancia(a)
        val lb = luminancia(b)
        val claro = maxOf(la, lb)
        val oscuro = minOf(la, lb)
        return (claro + 0.05) / (oscuro + 0.05)
    }

    private fun luminancia(color: Color): Double {
        val r = canal(color.red)
        val g = canal(color.green)
        val b = canal(color.blue)
        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }

    private fun canal(valor: Float): Double {
        val c = valor.toDouble()
        return if (c <= 0.03928) c / 12.92 else Math.pow((c + 0.055) / 1.055, 2.4)
    }
}
