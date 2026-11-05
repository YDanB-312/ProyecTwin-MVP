package com.example.proyectwin.ui.screens

import android.content.ActivityNotFoundException
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.proyectwin.BuildConfig
import com.example.proyectwin.ui.components.SenaErrorState
import com.example.proyectwin.ui.components.SenaSettingsItem
import com.example.proyectwin.ui.components.SenaTopBar
import com.example.proyectwin.ui.manual.ManualContent
import com.example.proyectwin.ui.manual.ManualPdfExport
import com.example.proyectwin.ui.manual.ManualPdfGenerator
import com.example.proyectwin.ui.theme.senaColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.Executors

/**
 * Visor del manual PDF generado desde ManualContent. Se abre como diálogo
 * sobre la pantalla actual para no perder el estado del usuario.
 */
@Composable
fun ManualPdfDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var archivo by remember { mutableStateOf<File?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(true) }
    var guardando by remember { mutableStateOf(false) }

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

    fun avisar(mensaje: String) {
        scope.launch { snackbarHostState.showSnackbar(mensaje) }
    }

    fun abrirUri(uri: Uri) {
        try {
            context.startActivity(ManualPdfExport.intentAbrirUri(uri))
        } catch (_: ActivityNotFoundException) {
            avisar("No hay una aplicación para abrir el PDF.")
        } catch (_: Exception) {
            avisar("No se pudo abrir el PDF.")
        }
    }

    fun compartir() {
        val actual = archivo ?: return
        try {
            context.startActivity(
                android.content.Intent.createChooser(
                    ManualPdfExport.intentCompartir(context, actual),
                    "Compartir manual",
                ),
            )
        } catch (_: ActivityNotFoundException) {
            avisar("No hay una aplicación para compartir el PDF.")
        } catch (_: Exception) {
            avisar("No se pudo compartir el PDF.")
        }
    }

    val guardarLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(ManualPdfExport.MIME_PDF),
    ) { uri ->
        val actual = archivo
        if (uri == null || actual == null) return@rememberLauncherForActivityResult
        guardando = true
        scope.launch {
            val resultado = withContext(Dispatchers.IO) {
                ManualPdfExport.copiar(context, actual, uri)
            }
            guardando = false
            resultado.onSuccess {
                val respuesta = snackbarHostState.showSnackbar(
                    message = "Manual guardado en el dispositivo.",
                    actionLabel = "Abrir",
                    withDismissAction = true,
                )
                if (respuesta == SnackbarResult.ActionPerformed) abrirUri(uri)
            }.onFailure {
                snackbarHostState.showSnackbar(
                    it.message ?: "No se pudo guardar el PDF.",
                )
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
                        actions = {
                            IconButton(
                                onClick = { compartir() },
                                enabled = archivo != null && !cargando && !guardando,
                            ) {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = "Compartir manual",
                                    tint = senaColors().onHeader,
                                )
                            }
                            IconButton(
                                onClick = {
                                    guardarLauncher.launch(
                                        ManualPdfExport.nombreSugerido(BuildConfig.VERSION_NAME),
                                    )
                                },
                                enabled = archivo != null && !cargando && !guardando,
                            ) {
                                if (guardando) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = senaColors().onHeader,
                                        strokeWidth = 2.dp,
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.SaveAlt,
                                        contentDescription = "Guardar manual",
                                        tint = senaColors().onHeader,
                                    )
                                }
                            }
                            IconButton(
                                onClick = { archivo?.let { abrirUri(ManualPdfExport.uri(context, it)) } },
                                enabled = archivo != null && !cargando && !guardando,
                            ) {
                                Icon(
                                    Icons.Default.OpenInNew,
                                    contentDescription = "Abrir manual",
                                    tint = senaColors().onHeader,
                                )
                            }
                        },
                    )
                },
                snackbarHost = { SnackbarHost(snackbarHostState) },
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

/**
 * Entrada reutilizable para los tres perfiles. Mantiene la visibilidad del
 * diálogo y evita duplicar la lógica de exportación en cada pantalla.
 */
@Composable
fun ManualPdfProfileEntry() {
    var mostrar by rememberSaveable { mutableStateOf(false) }
    SenaSettingsItem(
        icon = Icons.Default.PictureAsPdf,
        title = "Manual de usuario (PDF)",
        description = "Guarda, abre o comparte la guía de ProyecTwin",
        onClick = { mostrar = true },
    )
    if (mostrar) {
        ManualPdfDialog(onDismiss = { mostrar = false })
    }
}

@Composable
private fun PdfPages(archivo: File, modifier: Modifier = Modifier) {
    var estado by remember(archivo) { mutableStateOf<PdfRendererState?>(null) }
    var fallo by remember(archivo) { mutableStateOf(false) }

    LaunchedEffect(archivo) {
        val abierto = withContext(Dispatchers.IO) {
            runCatching { PdfRendererState(archivo) }.getOrNull()
        }
        estado = abierto
        fallo = abierto == null
    }
    val estadoActual = estado
    DisposableEffect(estadoActual) {
        onDispose { estadoActual?.close() }
    }

    when {
        fallo -> {
            Box(modifier = modifier, contentAlignment = Alignment.Center) {
                Text(
                    "No se pudo abrir el PDF generado.",
                    color = senaColors().danger,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        estado == null -> {
            Box(modifier = modifier, contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = senaColors().green)
            }
        }
        else -> {
            val actual = estado!!
            LazyColumn(
                modifier = modifier,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items((0 until actual.pageCount).toList()) { indice ->
                    PdfPage(estado = actual, indice = indice)
                }
            }
        }
    }
}

@Composable
private fun PdfPage(estado: PdfRendererState, indice: Int) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var error by remember { mutableStateOf(false) }
    LaunchedEffect(indice) {
        val intento = withContext(Dispatchers.IO) {
            runCatching { estado.render(indice) }
        }
        intento.onFailure {
            Log.e("ManualPdf", "No se pudo renderizar la página $indice", it)
        }
        bitmap = intento.getOrNull()
        error = bitmap == null
    }
    val actual = bitmap
    DisposableEffect(actual) {
        onDispose { actual?.recycle() }
    }

    if (actual == null) {
        Box(
            modifier = Modifier.fillMaxWidth().height(420.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (error) {
                Text(
                    "No se pudo mostrar esta página.",
                    color = senaColors().danger,
                    style = MaterialTheme.typography.bodySmall,
                )
            } else {
                CircularProgressIndicator(color = senaColors().green)
            }
        }
    } else {
        Image(
            bitmap = actual.asImageBitmap(),
            contentDescription = "Página ${indice + 1} del manual",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(actual.width.toFloat() / actual.height.toFloat())
                .background(androidx.compose.ui.graphics.Color.White),
            contentScale = ContentScale.Fit,
        )
    }
}

private class PdfRendererState(archivo: File) {
    // PdfRenderer no es thread-safe y puede requerir el mismo hilo para abrir,
    // renderizar y cerrar. Se usa un dispatcher de un solo hilo.
    private val executor = Executors.newSingleThreadExecutor()
    private val dispatcher = executor.asCoroutineDispatcher()
    private val descriptor: ParcelFileDescriptor = runBlocking(dispatcher) {
        ParcelFileDescriptor.open(archivo, ParcelFileDescriptor.MODE_READ_ONLY)
    }
    private val renderer: PdfRenderer = runBlocking(dispatcher) { PdfRenderer(descriptor) }
    private val mutex = Mutex()

    val pageCount: Int = runBlocking(dispatcher) { renderer.pageCount }

    suspend fun render(indice: Int): Bitmap = mutex.withLock {
        withContext(dispatcher) {
            val pagina = renderer.openPage(indice)
            try {
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

    fun close() {
        runBlocking(dispatcher) {
            runCatching { renderer.close() }
            runCatching { descriptor.close() }
        }
        dispatcher.close()
        executor.shutdown()
    }
}
