package com.example.proyectwin.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Prepara la foto de perfil igual que el frontend web:
 *
 * - Recorte cuadrado centrado a 256×256 y JPEG calidad 82
 *   (`procesarFoto` de React).
 * - Data URL `data:image/jpeg;base64,...`, que es lo que guarda la API en
 *   `general_users.foto_url` (columna TEXT: 65.535 caracteres máx.).
 *
 * Además incluye un bucle de seguridad por si la imagen comprimida superara el
 * tamaño de la columna (una simple comprobación local; mismo formato final).
 */
object ImagenUtils {

    private const val LADO = 256
    private const val CALIDAD = 82
    private const val MAX_CARACTERES = 60_000
    private const val LADO_DECODIFICACION = 1024

    /** Devuelve `data:image/jpeg;base64,...` listo para `PUT /general-users/{id}`. */
    suspend fun uriAFotoDataUrl(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        val original = decodificar(context, uri) ?: return@withContext null
        var lado = LADO
        var calidad = CALIDAD
        var dataUrl: String? = null

        repeat(5) {
            val cuadrada = recortarCuadrado(original, lado)
            val bytes = comprimirJpeg(cuadrada, calidad)
            if (cuadrada != original) cuadrada.recycle()
            dataUrl = "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
            if (dataUrl!!.length <= MAX_CARACTERES) {
                original.recycle()
                return@withContext dataUrl
            }
            lado = (lado * 0.8f).roundToInt().coerceAtLeast(96)
            calidad = (calidad - 12).coerceAtLeast(50)
        }
        original.recycle()
        dataUrl
    }

    /** Decodifica con muestreo para trabajar en memoria sin cargar el original completo. */
    private fun decodificar(context: Context, uri: Uri): Bitmap? {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sample = 1
        while (max(bounds.outWidth, bounds.outHeight) / (sample * 2) >= LADO_DECODIFICACION) sample *= 2

        val opciones = BitmapFactory.Options().apply { inSampleSize = sample }
        return resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opciones) }
    }

    /** Recorte central cuadrado de [original], escalado a [lado]×[lado]. */
    private fun recortarCuadrado(original: Bitmap, lado: Int): Bitmap {
        val borde = min(original.width, original.height)
        val x = (original.width - borde) / 2
        val y = (original.height - borde) / 2
        val recorte = Bitmap.createBitmap(original, x, y, borde, borde)
        if (borde == lado) return recorte
        val escalado = Bitmap.createScaledBitmap(recorte, lado, lado, true)
        if (escalado != recorte) recorte.recycle()
        return escalado
    }

    private fun comprimirJpeg(bitmap: Bitmap, calidad: Int): ByteArray {
        val salida = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, calidad, salida)
        return salida.toByteArray()
    }
}
