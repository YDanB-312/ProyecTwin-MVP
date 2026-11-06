package com.example.proyectwin.ui.manual

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import com.example.proyectwin.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import kotlin.math.roundToInt

/**
 * Genera el PDF del manual a partir de la misma fuente que usa la pantalla
 * interactiva. No hay una copia binaria versionada que pueda desincronizarse:
 * el PDF se regenera cuando cambia el contenido, la versión o el diseño.
 *
 * El texto se dibuja como contornos vectoriales. Android PdfRenderer no pinta
 * las fuentes Type3 que produce PdfDocument con texto normal; los contornos
 * garantizan que títulos y párrafos se vean en todos los visores.
 */
object ManualPdfGenerator {

    private const val LAYOUT_VERSION = 11

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN_LEFT = 48f
    private const val MARGIN_RIGHT = 48f
    private const val CONTENT_WIDTH = PAGE_WIDTH - MARGIN_LEFT - MARGIN_RIGHT
    private const val TOP = 66f
    private const val BOTTOM = 56f
    private const val CONTENT_BOTTOM = PAGE_HEIGHT - BOTTOM

    private val INK = Color.rgb(24, 32, 26)
    private val MUTED = Color.rgb(96, 112, 102)
    private val GREEN_DARK = Color.rgb(16, 72, 38)
    private val GREEN = Color.rgb(39, 128, 63)
    private val GREEN_SOFT = Color.rgb(233, 245, 236)
    private val GREEN_LINE = Color.rgb(196, 221, 202)
    private val SURFACE = Color.rgb(247, 250, 247)
    private val WHITE = Color.WHITE

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
            append("layout:").append(LAYOUT_VERSION).append('\n')
            append(versionName).append('\n')
            secciones.forEach { append(it.toString()).append('\n') }
            faqs.forEach { append(it.toString()).append('\n') }
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
        val primeraParte = secciones.firstOrNull()?.parte
        val paginas = calcularPaginas(context, secciones, faqs, primeraParte)

        val documento = PdfDocument()
        val escritor = EscritorPdf(
            documento = documento,
            context = context,
            simular = false,
            paginaInicial = 0,
            primeraParte = primeraParte,
        )
        try {
            escritor.portada(versionName)
            escritor.indice(secciones, paginas)
            escribirContenido(escritor, secciones, faqs)
            escritor.cerrar()
            FileOutputStream(archivo).use { documento.writeTo(it) }
        } finally {
            runCatching { escritor.cerrar() }
            documento.close()
        }
    }

    private fun calcularPaginas(
        context: Context,
        secciones: List<ManualSection>,
        faqs: List<ManualFaq>,
        primeraParte: String?,
    ): Map<String, Int> {
        val medidor = EscritorPdf(
            documento = null,
            context = context,
            simular = true,
            paginaInicial = 2, // portada + índice
            primeraParte = primeraParte,
        )
        medidor.nuevaPagina(parte = primeraParte)
        escribirContenido(medidor, secciones, faqs)
        medidor.cerrar()
        return medidor.inicioSeccion
    }

    private fun escribirContenido(
        escritor: EscritorPdf,
        secciones: List<ManualSection>,
        faqs: List<ManualFaq>,
    ) {
        var parteActual: String? = null
        secciones.forEach { seccion ->
            if (seccion.parte != parteActual) {
                escritor.tituloParte(seccion.parte)
                parteActual = seccion.parte
            }
            escritor.capitulo(seccion)
        }
        escritor.preguntas(faqs)
    }

    private class EscritorPdf(
        private val documento: PdfDocument?,
        private val context: Context,
        private val simular: Boolean,
        paginaInicial: Int,
        primeraParte: String?,
    ) {
        private val typefaceTitulo: Typeface =
            Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        private val paintPortadaTitulo = textPaint(33f, WHITE, true, typefaceTitulo)
        private val paintPortadaSub = textPaint(13f, Color.argb(230, 255, 255, 255), false)
        private val paintPortadaEtiqueta = textPaint(9f, Color.argb(200, 255, 255, 255), true)
        private val paintTitulo = textPaint(21f, INK, true, typefaceTitulo)
        private val paintParte = textPaint(11f, WHITE, true, typefaceTitulo)
        private val paintNumero = textPaint(19f, GREEN, true, typefaceTitulo)
        private val paintSubtitulo = textPaint(10.5f, GREEN, true)
        private val paintCuerpo = textPaint(10.2f, INK, false)
        private val paintPaso = textPaint(10f, INK, false)
        private val paintPie = textPaint(8f, MUTED, false)
        private val paintCaption = textPaint(8.5f, MUTED, false)
        private val paintToc = textPaint(9f, INK, false)
        private val paintTocParte = textPaint(9f, GREEN_DARK, true, typefaceTitulo)
        private val paintBlanco = textPaint(9f, WHITE, true)

        private val paintLinea = Paint().apply {
            isAntiAlias = true
            color = GREEN_LINE
            strokeWidth = 1f
        }
        private val paintBanda = Paint().apply {
            isAntiAlias = true
            color = GREEN_DARK
            style = Paint.Style.FILL
        }
        private val paintVerde = Paint().apply {
            isAntiAlias = true
            color = GREEN
            style = Paint.Style.FILL
        }
        private val paintVerdeSuave = Paint().apply {
            isAntiAlias = true
            color = GREEN_SOFT
            style = Paint.Style.FILL
        }
        private val paintSuperficie = Paint().apply {
            isAntiAlias = true
            color = SURFACE
            style = Paint.Style.FILL
        }
        private val paintPlaceholderBorde = Paint().apply {
            isAntiAlias = true
            color = GREEN_LINE
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        private val paintCirculo = Paint().apply {
            isAntiAlias = true
            color = GREEN
            style = Paint.Style.FILL
        }
        private val paintCubierta = Paint().apply {
            isAntiAlias = true
            color = Color.argb(18, 255, 255, 255)
            style = Paint.Style.FILL
        }

        private var pagina: PdfDocument.Page? = null
        private var canvas: Canvas? = null
        private var y = TOP
        private var numeroPagina = paginaInicial
        private var parteActual: String? = primeraParte
        private var paginaConPie = false
        val inicioSeccion = linkedMapOf<String, Int>()

        init {
            if (!simular) {
                nuevaPagina(conCabecera = false, conPie = false, parte = null)
            }
        }

        fun portada(versionName: String) {
            val c = canvas ?: return
            c.drawColor(GREEN_DARK)
            c.drawCircle(PAGE_WIDTH - 40f, 80f, 130f, paintCubierta)
            c.drawCircle(20f, PAGE_HEIGHT - 120f, 170f, paintCubierta)

            lineaSimple("SENA · PROYECTWIN", paintPortadaEtiqueta, MARGIN_LEFT, 120f)
            y = 150f
            texto(
                "Manual de usuario",
                paintPortadaTitulo,
                x = MARGIN_LEFT,
                ancho = CONTENT_WIDTH - 20f,
                conEspacio = false,
            )
            y += 12f
            c.drawRect(MARGIN_LEFT, y, MARGIN_LEFT + 58f, y + 3f, paintVerde)
            y += 24f
            texto(
                "Guía de la aplicación Android para aprendices, instructores y administradores.",
                paintPortadaSub,
                x = MARGIN_LEFT,
                ancho = CONTENT_WIDTH - 70f,
                conEspacio = false,
            )
            y += 18f
            texto(
                "Versión instalada: $versionName",
                paintPortadaSub,
                x = MARGIN_LEFT,
                ancho = CONTENT_WIDTH,
                conEspacio = false,
            )

            // Tarjeta inferior con el alcance del documento.
            val card = RectF(MARGIN_LEFT, PAGE_HEIGHT - 190f, PAGE_WIDTH - MARGIN_RIGHT, PAGE_HEIGHT - 74f)
            c.drawRoundRect(card, 14f, 14f, paintSuperficie)
            val anterior = y
            y = card.top + 26f
            lineaSimple("CONTENIDO", paintSubtitulo, MARGIN_LEFT + 22f, y)
            y += 20f
            texto(
                "Acceso, navegación, propuestas, fichas, similitudes, administración, perfiles y actualizaciones.",
                paintCuerpo,
                x = MARGIN_LEFT + 22f,
                ancho = card.width() - 44f,
                conEspacio = false,
            )
            y = anterior

            nuevaPagina(conCabecera = false, conPie = true, parte = null)
        }

        fun indice(
            secciones: List<ManualSection>,
            paginasPorSeccion: Map<String, Int>,
        ) {
            val c = canvas ?: return
            lineaSimple("Contenido", paintTitulo, MARGIN_LEFT, TOP + 20f)
            val filas = mutableListOf<FilaToc>()
            var parte: String? = null
            secciones.forEach { seccion ->
                if (seccion.parte != parte) {
                    filas += FilaToc.Parte(seccion.parte)
                    parte = seccion.parte
                }
                filas += FilaToc.Seccion(seccion, paginasPorSeccion[seccion.id] ?: 0)
            }

            val columnas = 2
            val anchoColumna = (CONTENT_WIDTH - 26f) / columnas
            val altoLinea = 15.2f
            val filasPorColumna = (filas.size + columnas - 1) / columnas
            var indiceFila = 0
            for (columna in 0 until columnas) {
                val x = MARGIN_LEFT + columna * (anchoColumna + 26f)
                var yToc = TOP + 58f
                val limite = minOf(filas.size, indiceFila + filasPorColumna)
                while (indiceFila < limite) {
                    when (val fila = filas[indiceFila]) {
                        is FilaToc.Parte -> {
                            yToc += 4f
                            lineaSimple(fila.nombre.uppercase(), paintTocParte, x, yToc)
                            yToc += altoLinea
                        }
                        is FilaToc.Seccion -> {
                            val numero = fila.seccion.numero
                            val titulo = "${fila.seccion.numero}. ${fila.seccion.titulo}"
                            val anchoNumero = 24f
                            val texto = TextUtils.ellipsize(
                                titulo,
                                paintToc,
                                anchoColumna - anchoNumero,
                                TextUtils.TruncateAt.END,
                            ).toString()
                            lineaSimple(texto, paintToc, x, yToc)
                            lineaSimple(
                                fila.pagina.toString(),
                                paintToc,
                                x + anchoColumna,
                                yToc,
                                alinearDerecha = true,
                            )
                            val finTexto = x + paintToc.measureText(texto) + 4f
                            val inicioNumero = x + anchoColumna - paintToc.measureText(fila.pagina.toString()) - 6f
                            if (inicioNumero > finTexto) {
                                c.drawLine(finTexto, yToc - 2f, inicioNumero, yToc - 2f, paintLinea)
                            }
                            yToc += altoLinea
                        }
                    }
                    indiceFila++
                }
                if (columna == 0) {
                    c.drawLine(
                        MARGIN_LEFT + anchoColumna + 13f,
                        TOP + 58f,
                        MARGIN_LEFT + anchoColumna + 13f,
                        CONTENT_BOTTOM - 20f,
                        paintLinea,
                    )
                }
            }

            nuevaPagina(conCabecera = true, conPie = true, parte = parteActual)
        }

        fun tituloParte(nombre: String) {
            if (simular) {
                asegurar(520f)
                parteActual = nombre
                y += 54f
                return
            }
            asegurar(520f)
            val c = canvas ?: return
            c.drawRoundRect(
                RectF(MARGIN_LEFT, y, PAGE_WIDTH - MARGIN_RIGHT, y + 34f),
                8f,
                8f,
                paintBanda,
            )
            lineaSimple(nombre.uppercase(), paintParte, MARGIN_LEFT + 14f, y + 22f)
            y += 54f
            parteActual = nombre
        }

        fun capitulo(seccion: ManualSection) {
            asegurar(if (seccion.figuras.isNotEmpty()) 300f else 180f)
            inicioSeccion[seccion.id] = numeroPagina
            val inicio = y
            canvas?.drawRoundRect(
                RectF(MARGIN_LEFT, inicio, MARGIN_LEFT + 46f, inicio + 46f),
                10f,
                10f,
                paintVerdeSuave,
            )
            if (canvas != null) {
                lineaSimple(
                    seccion.numero,
                    paintNumero,
                    MARGIN_LEFT + 23f,
                    inicio + 30f,
                    alinearDerecha = true,
                )
            }

            y = inicio + 4f
            texto(
                seccion.titulo,
                paintTitulo,
                x = MARGIN_LEFT + 58f,
                ancho = CONTENT_WIDTH - 58f,
                conEspacio = false,
            )
            y = maxOf(y, inicio + 50f) + 4f
            canvas?.drawLine(MARGIN_LEFT, y, PAGE_WIDTH - MARGIN_RIGHT, y, paintLinea)
            y += 16f

            texto(seccion.resumen, paintCuerpo, conEspacio = true)
            figuras(seccion.figuras)
            texto("Paso a paso", paintSubtitulo, conEspacio = true)

            seccion.pasos.forEachIndexed { indice, paso ->
                asegurar(42f)
                val cy = y + 6f
                val pasoCanvas = canvas
                if (pasoCanvas != null) {
                    pasoCanvas.drawCircle(MARGIN_LEFT + 8f, cy, 7f, paintCirculo)
                    lineaSimple(
                        (indice + 1).toString(),
                        paintBlanco,
                        MARGIN_LEFT + 8f,
                        cy + 3f,
                        alinearDerecha = true,
                    )
                }
                texto(
                    paso,
                    paintPaso,
                    x = MARGIN_LEFT + 24f,
                    ancho = CONTENT_WIDTH - 24f,
                    conEspacio = true,
                )
            }

            if (seccion.consejos.isNotEmpty()) {
                consejos(seccion.consejos)
            }

            if (seccion.relacionados.isNotEmpty()) {
                asegurar(24f)
                texto(
                    "Ver también: " + seccion.relacionados.joinToString(", "),
                    paintPie,
                    conEspacio = true,
                )
            }
            y += 10f
        }

        private fun consejos(consejos: List<String>) {
            asegurar(80f)
            val textoLayouts = consejos.map { consejo ->
                layoutTexto("· $consejo", paintPaso, CONTENT_WIDTH - 34f)
            }
            val altoTexto = textoLayouts.sumOf { it.height.toDouble() }.toFloat() +
                (textoLayouts.size - 1) * 3f
            val altoCaja = altoTexto + 44f
            if (y + altoCaja > CONTENT_BOTTOM) {
                nuevaPagina(parte = parteActual)
            }
            val c2 = canvas
            val top = y
            c2?.drawRoundRect(
                RectF(MARGIN_LEFT, top, PAGE_WIDTH - MARGIN_RIGHT, top + altoCaja),
                10f,
                10f,
                paintVerdeSuave,
            )
            c2?.drawRect(MARGIN_LEFT, top, MARGIN_LEFT + 3f, top + altoCaja, paintVerde)
            y = top + 18f
            if (c2 != null) {
                lineaSimple("Consejos", paintSubtitulo, MARGIN_LEFT + 18f, y)
            }
            y += 14f
            textoLayouts.forEach { layout ->
                if (!simular) {
                    dibujarLayout(layout, MARGIN_LEFT + 18f, y, paintPaso)
                }
                y += layout.height + 3f
            }
            y = top + altoCaja + 14f
        }

        fun preguntas(faqs: List<ManualFaq>) {
            if (faqs.isEmpty()) return
            asegurar(80f)
            texto("Preguntas frecuentes", paintTitulo, conEspacio = true)
            faqs.forEach { faq ->
                asegurar(46f)
                texto(faq.pregunta, paintSubtitulo, conEspacio = true)
                texto(faq.respuesta, paintCuerpo, conEspacio = true)
            }
        }

        fun cerrar() {
            if (simular) {
                pagina = null
                canvas = null
                return
            }
            val actual = pagina ?: return
            if (paginaConPie) dibujarPie(actual.canvas)
            documento?.finishPage(actual)
            pagina = null
            canvas = null
        }

        fun nuevaPagina(
            conCabecera: Boolean = true,
            conPie: Boolean = true,
            parte: String? = parteActual,
        ) {
            if (!simular) {
                val actual = pagina
                if (actual != null) {
                    if (paginaConPie) dibujarPie(actual.canvas)
                    documento?.finishPage(actual)
                }
                numeroPagina++
                val info = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, numeroPagina).create()
                pagina = documento?.startPage(info)
                canvas = pagina?.canvas
                if (conCabecera) dibujarCabecera(parte)
            } else {
                numeroPagina++
            }
            parteActual = parte
            paginaConPie = conPie
            y = TOP
        }

        private fun dibujarCabecera(parte: String?) {
            val c = canvas ?: return
            c.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 5f, paintVerde)
            lineaSimple("PROYECTWIN", paintPie, MARGIN_LEFT, 32f)
            lineaSimple(
                (parte ?: "Manual de usuario").uppercase(),
                paintPie,
                PAGE_WIDTH - MARGIN_RIGHT,
                32f,
                alinearDerecha = true,
            )
            c.drawLine(MARGIN_LEFT, 42f, PAGE_WIDTH - MARGIN_RIGHT, 42f, paintLinea)
        }

        private fun dibujarPie(c: Canvas) {
            c.drawLine(
                MARGIN_LEFT,
                PAGE_HEIGHT - 34f,
                PAGE_WIDTH - MARGIN_RIGHT,
                PAGE_HEIGHT - 34f,
                paintLinea,
            )
            lineaSimple("ProyecTwin · Manual de usuario", paintPie, MARGIN_LEFT, PAGE_HEIGHT - 18f)
            lineaSimple(
                "Página $numeroPagina",
                paintPie,
                PAGE_WIDTH - MARGIN_RIGHT,
                PAGE_HEIGHT - 18f,
                alinearDerecha = true,
            )
        }

        private fun asegurar(alto: Float) {
            if (y + alto > CONTENT_BOTTOM) {
                nuevaPagina(parte = parteActual)
            }
        }

        private fun texto(
            texto: String,
            paint: TextPaint,
            x: Float = MARGIN_LEFT,
            ancho: Float = CONTENT_WIDTH,
            conEspacio: Boolean = false,
        ) {            if (texto.isBlank()) return
            val layout = layoutTexto(texto, paint, ancho)
            var primeraLinea = 0
            while (primeraLinea < layout.lineCount) {
                if (y >= CONTENT_BOTTOM - 10f) {
                    nuevaPagina(parte = parteActual)
                }
                val disponible = CONTENT_BOTTOM - y
                var ultimaLinea = primeraLinea
                while (ultimaLinea < layout.lineCount) {
                    val alto = layout.getLineBottom(ultimaLinea) - layout.getLineTop(primeraLinea)
                    if (alto > disponible) break
                    ultimaLinea++
                }
                if (ultimaLinea == primeraLinea) {
                    nuevaPagina(parte = parteActual)
                    continue
                }
                if (!simular) {
                    dibujarLayout(
                        layout,
                        x,
                        y - layout.getLineTop(primeraLinea),
                        paint,
                        primeraLinea,
                        ultimaLinea,
                    )
                }
                y += layout.getLineBottom(ultimaLinea - 1) - layout.getLineTop(primeraLinea)
                primeraLinea = ultimaLinea
                if (primeraLinea < layout.lineCount) {
                    nuevaPagina(parte = parteActual)
                }
            }
            if (conEspacio) y += 9f
        }

        private fun layoutTexto(texto: String, paint: TextPaint, ancho: Float): StaticLayout =
            StaticLayout.Builder.obtain(texto, 0, texto.length, paint, ancho.roundToInt())
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1.28f)
                .setIncludePad(false)
                .build()

        private fun dibujarLayout(
            layout: StaticLayout,
            x: Float,
            top: Float,
            paint: TextPaint,
            desde: Int = 0,
            hasta: Int = layout.lineCount,
        ) {
            val c = canvas ?: return
            val relleno = Paint(paint).apply {
                style = Paint.Style.FILL
                strokeWidth = 0f
            }
            for (linea in desde until hasta) {
                val inicio = layout.getLineStart(linea)
                val fin = layout.getLineEnd(linea)
                if (fin <= inicio) continue
                val contenido = layout.text.subSequence(inicio, fin).toString().trimEnd('\n')
                if (contenido.isBlank()) continue
                val path = Path()
                paint.getTextPath(
                    contenido,
                    0,
                    contenido.length,
                    x + layout.getLineLeft(linea),
                    top + layout.getLineBaseline(linea),
                    path,
                )
                c.drawPath(path, relleno)
            }
        }

        private fun lineaSimple(
            texto: String,
            paint: TextPaint,
            x: Float,
            baseline: Float,
            alinearDerecha: Boolean = false,
        ) {
            if (texto.isBlank()) return
            val c = canvas ?: return
            val ancho = paint.measureText(texto)
            val origen = if (alinearDerecha) x - ancho else x
            val path = Path()
            paint.getTextPath(texto, 0, texto.length, origen, baseline, path)
            val relleno = Paint(paint).apply { style = Paint.Style.FILL }
            c.drawPath(path, relleno)
        }

        private fun figuras(figuras: List<ManualFigure>) {
            var indice = 0
            while (indice < figuras.size) {
                val primera = figuras[indice]
                val segunda = figuras.getOrNull(indice + 1)
                if (segunda != null && primera.recurso != null && segunda.recurso != null) {
                    parDeImagenes(primera, segunda)
                    indice += 2
                } else {
                    imagen(primera)
                    indice++
                }
            }
        }

        private fun parDeImagenes(primera: ManualFigure, segunda: ManualFigure) {
            val original1 = BitmapFactory.decodeResource(context.resources, primera.recurso!!)
                ?: error("No se pudo cargar la captura del manual (recurso ${primera.recurso}).")
            val original2 = BitmapFactory.decodeResource(context.resources, segunda.recurso!!)
                ?: error("No se pudo cargar la captura del manual (recurso ${segunda.recurso}).")
            val maxAncho = 232f
            val maxAlto = 400f
            val escala1 = minOf(maxAncho / original1.width.toFloat(), maxAlto / original1.height.toFloat())
            val escala2 = minOf(maxAncho / original2.width.toFloat(), maxAlto / original2.height.toFloat())
            val ancho1 = (original1.width * escala1).toInt().coerceAtLeast(1)
            val alto1 = (original1.height * escala1).toInt().coerceAtLeast(1)
            val ancho2 = (original2.width * escala2).toInt().coerceAtLeast(1)
            val alto2 = (original2.height * escala2).toInt().coerceAtLeast(1)
            val escalada1 = Bitmap.createScaledBitmap(original1, ancho1, alto1, true)
            val escalada2 = Bitmap.createScaledBitmap(original2, ancho2, alto2, true)

            val tarjeta = 238f
            val separacion = 18f
            val x1 = MARGIN_LEFT
            val x2 = MARGIN_LEFT + tarjeta + separacion
            val caption1 = layoutTexto(primera.descripcion, paintCaption, tarjeta - 10f)
            val caption2 = layoutTexto(segunda.descripcion, paintCaption, tarjeta - 10f)
            val altoImagen = maxOf(alto1, alto2).toFloat()
            val altoCaption = maxOf(caption1.height, caption2.height).toFloat()
            val altoBloque = altoImagen + 10f + altoCaption + 18f
            if (y + altoBloque > CONTENT_BOTTOM) {
                nuevaPagina(parte = parteActual)
            }
            val c = canvas
            if (c != null) {
                dibujarTarjeta(c, x1, y, tarjeta, escalada1, ancho1, alto1)
                dibujarTarjeta(c, x2, y, tarjeta, escalada2, ancho2, alto2)
                c.save()
                c.translate(x1 + 5f, y + altoImagen + 10f)
                dibujarLayout(caption1, 0f, 0f, paintCaption)
                c.restore()
                c.save()
                c.translate(x2 + 5f, y + altoImagen + 10f)
                dibujarLayout(caption2, 0f, 0f, paintCaption)
                c.restore()
            }
            y += altoBloque
            if (escalada1 !== original1) escalada1.recycle()
            if (escalada2 !== original2) escalada2.recycle()
            original1.recycle()
            original2.recycle()
        }

        private fun dibujarTarjeta(
            c: Canvas,
            x: Float,
            top: Float,
            anchoTarjeta: Float,
            bitmap: Bitmap,
            anchoImagen: Int,
            altoImagen: Int,
        ) {
            c.drawRoundRect(
                RectF(x, top, x + anchoTarjeta, top + altoImagen + 6f),
                8f,
                8f,
                paintSuperficie,
            )
            val ix = x + (anchoTarjeta - anchoImagen) / 2f
            val clip = Path().apply {
                addRoundRect(
                    RectF(ix, top, ix + anchoImagen, top + altoImagen),
                    6f,
                    6f,
                    Path.Direction.CW,
                )
            }
            c.save()
            c.clipPath(clip)
            c.drawBitmap(bitmap, ix, top, null)
            c.restore()
            c.drawRoundRect(
                RectF(ix, top, ix + anchoImagen, top + altoImagen),
                6f,
                6f,
                paintPlaceholderBorde,
            )
        }

        private fun imagen(figura: ManualFigure) {
            val recurso = figura.recurso
            if (recurso == null) {
                asegurar(72f)
                val c = canvas
                val rect = RectF(MARGIN_LEFT, y, PAGE_WIDTH - MARGIN_RIGHT, y + 58f)
                c?.drawRoundRect(rect, 8f, 8f, paintVerdeSuave)
                c?.drawRoundRect(rect, 8f, 8f, paintPlaceholderBorde)
                y += 70f
                texto("Captura pendiente: ${figura.descripcion}", paintCaption, conEspacio = true)
                return
            }

            val original = BitmapFactory.decodeResource(context.resources, recurso)
                ?: error("No se pudo cargar la captura del manual (recurso $recurso).")
            val escala = minOf(
                CONTENT_WIDTH / original.width.toFloat(),
                430f / original.height.toFloat(),
            )
            val ancho = (original.width * escala).toInt().coerceAtLeast(1)
            val alto = (original.height * escala).toInt().coerceAtLeast(1)
            val escalada = Bitmap.createScaledBitmap(original, ancho, alto, true)

            val captionLayout = layoutTexto(
                figura.descripcion,
                paintCaption,
                CONTENT_WIDTH - 20f,
            )
            val altoBloque = alto + 8f + captionLayout.height + 10f
            if (y + altoBloque > CONTENT_BOTTOM) {
                nuevaPagina(parte = parteActual)
            }
            val c = canvas
            if (c != null) {
                val x = MARGIN_LEFT + (CONTENT_WIDTH - ancho) / 2f
                val rect = RectF(x - 6f, y - 4f, x + ancho + 6f, y + alto + 6f)
                c.drawRoundRect(rect, 8f, 8f, paintSuperficie)
                val clip = Path().apply {
                    addRoundRect(
                        RectF(x.toFloat(), y.toFloat(), (x + ancho).toFloat(), (y + alto).toFloat()),
                        6f,
                        6f,
                        Path.Direction.CW,
                    )
                }
                c.save()
                c.clipPath(clip)
                c.drawBitmap(escalada, x, y, null)
                c.restore()
                c.drawRoundRect(
                    RectF(x.toFloat(), y.toFloat(), (x + ancho).toFloat(), (y + alto).toFloat()),
                    6f,
                    6f,
                    paintPlaceholderBorde,
                )
                c.save()
                c.translate(MARGIN_LEFT + 10f, y + alto + 8f)
                dibujarLayout(captionLayout, 0f, 0f, paintCaption)
                c.restore()
            }
            y += alto + 8f + captionLayout.height + 10f
            if (escalada !== original) escalada.recycle()
            original.recycle()
        }

        private fun textPaint(
            tamano: Float,
            color: Int,
            negrita: Boolean,
            typeface: Typeface? = null,
        ): TextPaint = TextPaint().apply {
            isAntiAlias = true
            textSize = tamano
            this.color = color
            when {
                typeface != null -> this.typeface = typeface
                negrita -> this.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
        }

        private sealed interface FilaToc {
            data class Parte(val nombre: String) : FilaToc
            data class Seccion(val seccion: ManualSection, val pagina: Int) : FilaToc
        }
    }
}
