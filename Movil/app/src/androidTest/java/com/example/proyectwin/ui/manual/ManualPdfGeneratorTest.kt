package com.example.proyectwin.ui.manual

import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ManualPdfGeneratorTest {

    @Test
    fun generaPdfDesdeElManualEIncluyeLasCapturasDisponibles() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val archivo = ManualPdfGenerator.obtener(
            context = context,
            secciones = ManualContent.secciones,
            faqs = ManualContent.preguntasFrecuentes,
            versionName = "test",
        )

        assertTrue("El PDF no se generó", archivo.isFile)
        assertTrue("El PDF está vacío", archivo.length() > 0)

        ParcelFileDescriptor.open(archivo, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
            PdfRenderer(descriptor).use { renderer ->
                assertTrue("El PDF no contiene páginas", renderer.pageCount > 0)
            }
        }
    }
}
