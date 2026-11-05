package com.example.proyectwin.ui.manual

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.example.proyectwin.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

/**
 * Genera el PDF del manual a partir de la misma fuente que usa la pantalla
 * interactiva. No hay una copia binaria versionada que pueda desincronizarse:
 * el PDF se regenera cuando cambia el contenido, la versión o las capturas.
 */
object ManualPdfGenerator {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 42f
    private const val CONTENT_WIDTH = PAGE_WIDTH - (MARGIN * 2)
    private const val MAX_IMAGE_HEIGHT = 520f

    private val mutex = Mutex()

    fun archivo(context: Context): File =
        File(File(context.cacheDir, "manual"), "manual_usuario_movil.pdf")

    suspend fun obtener(
        context: Context,
        secciones: List<ManualSection>,
        faqs: List<ManualFaq>,
        versionName: String = BuildConfig.VERSION_NAME,
    ): File = mutex.withLock {
        withContext(Dispatchers.IO) {
            val archivo = archivo(context)
            val marcador = File(archivo.parentFile, "manual_usuario_movil.version")
            val huella = huella(secciones, faqs, versionName)
            if (archivo.isFile && archivo.length() > 0 && marcador.isFile &&
                marcador.readText() == huella
            ) {
                return@withContext archivo
            }

            archivo.parentFile?.mkdirs()
            archivo.delete()
            try {
                generar(context, archivo, secciones, faqs, versionName)
                marcador.writeText(huella)
                archivo
            } catch (e: Exception) {
                archivo.delete()
                marcador.delete()
                throw e
            }
        }
    }

    private fun huella(
        secciones: List<ManualSection>,
        faqs: List<ManualFaq>,
        versionName: String,
    ): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val texto = buildString {
            append(versionName)
            secciones.forEach { append(it.toString()) }
            faqs.forEach { append(it.toString()) }
        }
        return digest.digest(texto.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    private fun generar(
        context: Context,
        archivo: File,
        secciones: List<ManualSection>,
        faqs: List<ManualFaq>,
        versionName: String,
    ) {
        val documento = PdfDocument()
        val escritor = EscritorPdf(documento, context)
        try {
            escritor.portada(versionName)
            escritor.indice(secciones)
            var parteActual: String? = null
            secciones.forEach { seccion ->
                if (seccion.parte != parteActual) {
                    escritor.tituloParte(seccion.parte)
                    parteActual = seccion.parte
                }
                escritor.capitulo(seccion)
            }
            escritor.preguntas(faqs)
            escritor.cerrar()
            FileOutputStream(archivo).use { documento.writeTo(it) }
        } finally {
            runCatching { escritor.cerrar() }
            documento.close()
        }
    }

    private class EscritorPdf(
        private val documento: PdfDocument,
        private val context: Context,
    ) {
        private val paintTitulo = TextPaint().apply {
            isAntiAlias = true
            color = Color.rgb(20, 65, 35)
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        private val paintSubtitulo = TextPaint().apply {
            isAntiAlias = true
            color = Color.DKGRAY
            textSize = 12f
        }
        private val paintParte = TextPaint().apply {
            isAntiAlias = true
            color = Color.rgb(20, 110, 55)
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        private val paintCapitulo = TextPaint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        private val paintCuerpo = TextPaint().apply {
            isAntiAlias = true
            color = Color.rgb(55, 55, 55)
            textSize = 12f
        }
        private val paintPie = TextPaint().apply {
            isAntiAlias = true
            color = Color.GRAY
            textSize = 9f
        }
        private val paintLinea = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(210, 220, 210)
            strokeWidth = 1f
        }
        private val paintPlaceholder = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(245, 245, 245)
            style = Paint.Style.FILL
        }
        private val paintPlaceholderBorde = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(200, 200, 200)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        private var pagina: PdfDocument.Page? = null
        private var canvas: Canvas? = null
        private var y = MARGIN
        private var numeroPagina = 0

        init {
            nuevaPagina()
        }

        fun portada(versionName: String) {
            canvas?.drawColor(Color.rgb(238, 246, 238))
            y = 190f
            texto("Manual de usuario", paintTitulo, 8f)
            texto("ProyecTwin", paintParte, 4f)
            texto(
                "Guía de la aplicación Android para aprendices, instructores y administradores.",
                paintSubtitulo,
                12f,
            )
            texto("Versión instalada: $versionName", paintCuerpo, 4f)
            texto(
                "Este PDF se genera desde el mismo contenido del manual integrado en la app.",
                paintCuerpo,
                0f,
            )
            nuevaPagina()
        }

        fun indice(secciones: List<ManualSection>) {
            texto("Índice", paintParte, 10f)
            var parte: String? = null
            secciones.forEach { seccion ->
                if (seccion.parte != parte) {
                    texto(seccion.parte, paintCapitulo, 4f)
                    parte = seccion.parte
                }
                texto("${seccion.numero}. ${seccion.titulo}", paintCuerpo, 2f)
            }
        }

        fun tituloParte(nombre: String) {
            asegurar(46f)
            canvas?.drawLine(
                MARGIN,
                y,
                PAGE_WIDTH - MARGIN,
                y,
                paintLinea,
            )
            y += 10f
            texto(nombre, paintParte, 10f)
        }

        fun capitulo(seccion: ManualSection) {
            texto("${seccion.numero}. ${seccion.titulo}", paintCapitulo, 4f)
            texto(seccion.resumen, paintCuerpo, 8f)
            texto("Paso a paso", paintCapitulo, 4f)
            seccion.pasos.forEachIndexed { indice, paso ->
                texto("${indice + 1}. $paso", paintCuerpo, 2f)
            }
            if (seccion.consejos.isNotEmpty()) {
                texto("Consejos", paintCapitulo, 4f)
                seccion.consejos.forEach { consejo ->
                    texto("· $consejo", paintCuerpo, 2f)
                }
            }
            seccion.figuras.forEach { figura ->
                imagen(figura)
            }
            if (seccion.relacionados.isNotEmpty()) {
                texto(
                    "Ver también: " + seccion.relacionados.joinToString(", "),
                    paintPie,
                    8f,
                )
            }
            y += 8f
        }

        fun preguntas(faqs: List<ManualFaq>) {
            if (faqs.isEmpty()) return
            texto("Preguntas frecuentes", paintParte, 8f)
            faqs.forEach { faq ->
                texto(faq.pregunta, paintCapitulo, 2f)
                texto(faq.respuesta, paintCuerpo, 8f)
            }
        }

        fun cerrar() {
            val actual = pagina ?: return
            dibujarPie(actual.canvas)
            documento.finishPage(actual)
            pagina = null
            canvas = null
        }

        private fun nuevaPagina() {
            val actual = pagina
            if (actual != null) {
                dibujarPie(actual.canvas)
                documento.finishPage(actual)
            }
            val info = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, ++numeroPagina).create()
            pagina = documento.startPage(info)
            canvas = pagina?.canvas
            y = MARGIN
        }

        private fun dibujarPie(canvas: Canvas) {
            canvas.drawText(
                "ProyecTwin · Manual de usuario · Página $numeroPagina",
                MARGIN,
                PAGE_HEIGHT - 18f,
                paintPie,
            )
        }

        private fun asegurar(alto: Float) {
            if (y + alto > PAGE_HEIGHT - MARGIN - 20f) {
                nuevaPagina()
            }
        }

        private fun texto(texto: String, paint: TextPaint, espacioDespues: Float) {
            if (texto.isBlank()) return
            val layout = StaticLayout.Builder.obtain(
                texto,
                0,
                texto.length,
                paint,
                CONTENT_WIDTH.toInt(),
            )
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1.22f)
                .setIncludePad(false)
                .build()
            asegurar(layout.height + espacioDespues)
            layout.draw(canvas ?: return)
            y += layout.height + espacioDespues
        }

        private fun imagen(figura: ManualFigure) {
            val recurso = figura.recurso
            if (recurso == null) {
                asegurar(64f)
                val rect = android.graphics.RectF(
                    MARGIN,
                    y,
                    PAGE_WIDTH - MARGIN,
                    y + 56f,
                )
                canvas?.drawRoundRect(rect, 6f, 6f, paintPlaceholder)
                canvas?.drawRoundRect(rect, 6f, 6f, paintPlaceholderBorde)
                y += 8f
                texto("Captura pendiente: ${figura.descripcion}", paintPie, 8f)
                return
            }

            val original = BitmapFactory.decodeResource(context.resources, recurso)
                ?: error("No se pudo cargar la captura del manual (recurso $recurso).")
            // Una sola escala para ancho y alto: nunca deformar la captura.
            val escala = minOf(
                CONTENT_WIDTH / original.width.toFloat(),
                MAX_IMAGE_HEIGHT / original.height.toFloat(),
            )
            val ancho = (original.width * escala).toInt().coerceAtLeast(1)
            val alto = (original.height * escala).toInt().coerceAtLeast(1)
            val escalada = Bitmap.createScaledBitmap(original, ancho, alto, true)
            asegurar(alto + 30f)
            val x = MARGIN + (CONTENT_WIDTH - ancho) / 2f
            canvas?.drawBitmap(escalada, x, y, null)
            y += alto + 6f
            texto(figura.descripcion, paintPie, 10f)
            if (escalada !== original) escalada.recycle()
            original.recycle()
        }
    }
}
