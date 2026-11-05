package com.example.proyectwin.ui.manual

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
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
            versionName = "test",
        )

        ParcelFileDescriptor.open(archivo, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
            PdfRenderer(descriptor).use { renderer ->
                assertTrue(renderer.pageCount > 0)
                repeat(renderer.pageCount) { indice ->
                    val pagina = renderer.openPage(indice)
                    try {
                        val ancho = 1080
                        val alto = (ancho.toFloat() * pagina.height / pagina.width).toInt()
                        val bitmap = Bitmap.createBitmap(ancho, alto, Bitmap.Config.ARGB_8888)
                        bitmap.eraseColor(Color.WHITE)
                        pagina.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        bitmap.recycle()
                    } finally {
                        pagina.close()
                    }
                }
            }
        }
    }
}
