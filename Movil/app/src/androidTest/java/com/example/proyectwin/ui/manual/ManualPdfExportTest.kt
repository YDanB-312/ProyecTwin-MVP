package com.example.proyectwin.ui.manual

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ManualPdfExportTest {

    @Test
    fun fileProviderExponeElManualYLoSharesConPermisoDeLectura() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val archivo = ManualPdfGenerator.obtener(
            context = context,
            secciones = ManualContent.secciones,
            faqs = ManualContent.preguntasFrecuentes,
            versionName = "test",
        )

        val uri = ManualPdfExport.uri(context, archivo)
        assertEquals("content", uri.scheme)
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        assertNotNull("FileProvider no devolvió contenido", bytes)
        assertTrue("El PDF compartido está vacío", bytes!!.isNotEmpty())

        val intent = ManualPdfExport.intentCompartir(context, archivo)
        assertEquals(Intent.ACTION_SEND, intent.action)
        assertEquals(ManualPdfExport.MIME_PDF, intent.type)
        assertTrue(
            "Falta el permiso temporal de lectura",
            intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0,
        )
        assertNotNull(intent.clipData)
    }
}
