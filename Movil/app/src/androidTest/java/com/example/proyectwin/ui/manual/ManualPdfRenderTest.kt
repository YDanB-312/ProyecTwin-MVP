package com.example.proyectwin.ui.manual

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.proyectwin.BuildConfig
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ManualPdfRenderTest {

    @Test
    fun renderizaTodasLasPaginasDelManual() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val archivo = ManualPdfGenerator.obtener(
            context = context,
            secciones = ManualContent.secciones,
            faqs = ManualContent.preguntasFrecuentes,
            versionName = BuildConfig.VERSION_NAME,
        )

        ParcelFileDescriptor.open(archivo, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
            PdfRenderer(descriptor).use { renderer ->
                assertTrue(renderer.pageCount > 0)
                repeat(renderer.pageCount) { indice ->
                    renderizar(renderer, indice).recycle()
                }
            }
        }
    }

    @Test
    fun elIndiceTieneTextoDistribuidoYNoAmontonado() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val archivo = ManualPdfGenerator.obtener(
            context = context,
            secciones = ManualContent.secciones,
            faqs = ManualContent.preguntasFrecuentes,
            versionName = BuildConfig.VERSION_NAME,
        )

        ParcelFileDescriptor.open(archivo, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
            PdfRenderer(descriptor).use { renderer ->
                assertTrue("El PDF debe tener portada e índice", renderer.pageCount >= 2)
                val bitmap = renderizar(renderer, 1)
                try {
                    val bandas = IntArray(8)
                    var total = 0
                    var x = 0
                    while (x < bitmap.width) {
                        var y = 0
                        while (y < bitmap.height) {
                            val color = bitmap.getPixel(x, y)
                            val luminancia = Color.red(color) * 0.299f +
                                Color.green(color) * 0.587f +
                                Color.blue(color) * 0.114f
                            if (luminancia < 180f) {
                                bandas[(y * 8 / bitmap.height).coerceIn(0, 7)]++
                                total++
                            }
                            y += 2
                        }
                        x += 2
                    }
                    assertTrue("El índice no tiene texto visible", total > 500)
                    assertTrue(
                        "El texto del índice sigue amontonado arriba",
                        bandas.count { it > 0 } >= 3,
                    )
                    val arriba = bandas[0] + bandas[1]
                    assertTrue(
                        "Demasiado texto concentrado en la cabecera",
                        arriba < total * 0.7f,
                    )
                } finally {
                    bitmap.recycle()
                }
            }
        }
    }

    private fun renderizar(renderer: PdfRenderer, indice: Int): Bitmap {
        val pagina = renderer.openPage(indice)
        return try {
            val ancho = 1080
            val alto = (ancho.toFloat() * pagina.height / pagina.width).toInt()
            val bitmap = Bitmap.createBitmap(ancho, alto, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(Color.WHITE)
            pagina.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            bitmap
        } finally {
            pagina.close()
        }
    }
}
