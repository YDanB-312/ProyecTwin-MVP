package com.example.proyectwin.data.local

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File

/** Guarda PDFs descargados de la API en el almacenamiento del dispositivo. */
object DescargasArchivos {

    /**
     * Guarda [bytes] como PDF y devuelve un mensaje con la ubicación.
     * En Android 10+ usa MediaStore (carpeta Descargas, sin permisos); en
     * versiones anteriores usa el directorio externo propio de la app.
     */
    fun guardarPdf(context: Context, bytes: ByteArray, nombre: String): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val valores = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, nombre)
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, valores)
                ?: return "No se pudo crear el archivo de descarga."
            runCatching {
                resolver.openOutputStream(uri)?.use { salida -> salida.write(bytes) }
            }.onFailure {
                resolver.delete(uri, null, null)
                return "No se pudo guardar el PDF."
            }
            valores.clear()
            valores.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(uri, valores, null, null)
            "PDF guardado en Descargas: $nombre"
        } else {
            val directorio = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                ?: context.filesDir
            val archivo = File(directorio, nombre)
            runCatching { archivo.writeBytes(bytes) }
                .fold(
                    onSuccess = { "PDF guardado en ${archivo.absolutePath}" },
                    onFailure = { "No se pudo guardar el PDF." },
                )
        }
    }
}
