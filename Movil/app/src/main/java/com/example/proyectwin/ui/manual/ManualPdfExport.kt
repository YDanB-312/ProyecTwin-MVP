package com.example.proyectwin.ui.manual

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * Intents y copia segura del manual PDF. Centraliza FileProvider para que el
 * visor, el guardado y el intercambio usen exactamente la misma configuración.
 */
object ManualPdfExport {

    const val MIME_PDF = "application/pdf"

    fun uri(context: Context, archivo: File): Uri =
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            archivo,
        )

    fun nombreSugerido(version: String): String =
        "ProyecTwin-Manual-${version.ifBlank { "usuario" }}.pdf"

    fun intentCompartir(context: Context, archivo: File): Intent {
        val uri = uri(context, archivo)
        return Intent(Intent.ACTION_SEND).apply {
            type = MIME_PDF
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newRawUri("Manual de usuario ProyecTwin", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun intentAbrir(context: Context, archivo: File): Intent =
        intentAbrirUri(uri(context, archivo))

    fun intentAbrirUri(uri: Uri): Intent =
        Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, MIME_PDF)
            clipData = ClipData.newRawUri("Manual de usuario ProyecTwin", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

    /** Copia el PDF a la URI que devuelve el selector SAF. Se llama desde IO. */
    fun copiar(context: Context, archivo: File, destino: Uri): Result<Unit> = runCatching {
        val salida = context.contentResolver.openOutputStream(destino)
            ?: error("No se pudo abrir el destino para guardar el PDF.")
        salida.use { output ->
            archivo.inputStream().use { input ->
                input.copyTo(output)
            }
        }
    }
}
