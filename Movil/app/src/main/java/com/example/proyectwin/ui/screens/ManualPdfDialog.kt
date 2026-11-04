package com.example.proyectwin.ui.screens

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.proyectwin.ui.components.SenaErrorState
import com.example.proyectwin.ui.components.SenaTopBar
import com.example.proyectwin.ui.manual.ManualContent
import com.example.proyectwin.ui.manual.ManualPdfGenerator
import com.example.proyectwin.ui.theme.senaColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Visor del manual PDF generado desde ManualContent. Se abre como diálogo
 * sobre el login para no perder los campos que el usuario ya escribió.
 */
@Composable
fun ManualPdfDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var archivo by remember { mutableStateOf<File?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(true) }

    fun generar() {
        scope.launch {
            cargando = true
            error = null
            runCatching {
                ManualPdfGenerator.obtener(
                    context = context,
                    secciones = ManualContent.secciones,
                    faqs = ManualContent.preguntasFrecuentes,
                )
            }.onSuccess {
                archivo = it
                cargando = false
            }.onFailure {
                error = it.message ?: "No se pudo generar el manual PDF."
                cargando = false
            }
        }
    }

    LaunchedEffect(Unit) { generar() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = senaColors().background) {
            Scaffold(
                topBar = {
                    SenaTopBar(
                        title = "Manual de usuario (PDF)",
                        onBack = onDismiss,
                        showProfile = false,
                        showNotifications = false,
                    )
                },
                containerColor = senaColors().background,
            ) { padding ->
                when {
                    cargando -> {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(padding),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            CircularProgressIndicator(color = senaColors().green)
                            Text(
                                "Generando manual PDF…",
                                modifier = Modifier.padding(top = 16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = senaColors().textSecondary,
                            )
                        }
                    }

                    error != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(padding),
                            contentAlignment = Alignment.Center,
                        ) {
                            SenaErrorState(
                                message = error.orEmpty(),
                                onRetry = { generar() },
                            )
                        }
                    }

                    archivo != null -> {
                        PdfPages(
                            archivo = archivo!!,
                            modifier = Modifier.fillMaxSize().padding(padding),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PdfPages(archivo: File, modifier: Modifier = Modifier) {
    val estado = remember(archivo) {
        runCatching { PdfRendererState(archivo) }.getOrNull()
    }
    DisposableEffect(estado) {
        onDispose { estado?.close() }
    }

    if (estado == null) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(
                "No se pudo abrir el PDF generado.",
                color = senaColors().danger,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items((0 until estado.pageCount).toList()) { indice ->
            PdfPage(estado = estado, indice = indice)
        }
    }
}

@Composable
private fun PdfPage(estado: PdfRendererState, indice: Int) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(indice) {
        bitmap = withContext(Dispatchers.IO) { estado.render(indice) }
    }
    DisposableEffect(bitmap) {
        onDispose { bitmap?.recycle() }
    }

    val actual = bitmap
    if (actual == null) {
        Box(
            modifier = Modifier.fillMaxWidth().height(420.dp),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = senaColors().green)
        }
    } else {
        Image(
            bitmap = actual.asImageBitmap(),
            contentDescription = "Página ${indice + 1} del manual",
            modifier = Modifier.fillMaxWidth().background(androidx.compose.ui.graphics.Color.White),
            contentScale = ContentScale.FillWidth,
        )
    }
}

private class PdfRendererState(archivo: File) {
    private val descriptor: ParcelFileDescriptor =
        ParcelFileDescriptor.open(archivo, ParcelFileDescriptor.MODE_READ_ONLY)
    private val renderer = PdfRenderer(descriptor)
    private val mutex = Mutex()

    val pageCount: Int = renderer.pageCount

    suspend fun render(indice: Int): Bitmap = mutex.withLock {
        withContext(Dispatchers.IO) {
            val pagina = renderer.openPage(indice)
            val ancho = 1080
            val alto = (ancho.toFloat() * pagina.height / pagina.width).toInt()
            val bitmap = Bitmap.createBitmap(ancho, alto, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(Color.WHITE)
            pagina.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            pagina.close()
            bitmap
        }
    }

    fun close() {
        runCatching { renderer.close() }
        runCatching { descriptor.close() }
    }
}
