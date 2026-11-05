package com.example.proyectwin.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.proyectwin.BuildConfig
import com.example.proyectwin.ui.components.SenaCard
import com.example.proyectwin.ui.components.SenaChip
import com.example.proyectwin.ui.components.SenaChipRow
import com.example.proyectwin.ui.components.SenaTextField
import com.example.proyectwin.ui.components.SenaTopBar
import com.example.proyectwin.ui.manual.ManualContent
import com.example.proyectwin.ui.manual.ManualFaq
import com.example.proyectwin.ui.manual.ManualFigure
import com.example.proyectwin.ui.manual.ManualRole
import com.example.proyectwin.ui.manual.ManualSection
import com.example.proyectwin.ui.theme.senaColors
import com.example.proyectwin.ui.viewmodel.AuthUiState
import com.example.proyectwin.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

private sealed interface ItemManual {
    data class Parte(val nombre: String) : ItemManual
    data class Seccion(val seccion: ManualSection) : ItemManual
}

/**
 * Manual de usuario integrado. Es una pantalla compartida por los tres roles:
 * no modifica la sesión, no navega fuera de la app y conserva el contenido
 * aunque el usuario no tenga conexión.
 */
@Composable
fun ManualScreen(
    onBack: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val authState by authViewModel.uiState.collectAsState()
    val rolUsuario = (authState as? AuthUiState.LoggedIn)?.user?.role
    val rolInicial = when (rolUsuario) {
        "aprendiz" -> ManualRole.APRENDIZ
        "instructor" -> ManualRole.INSTRUCTOR
        "admin" -> ManualRole.ADMIN
        else -> ManualRole.TODOS
    }

    // null = usar el rol de la sesión; el usuario puede cambiarlo con los chips.
    var filtroManual by rememberSaveable { mutableStateOf<ManualRole?>(null) }
    val filtroRol = filtroManual ?: rolInicial
    var busqueda by rememberSaveable { mutableStateOf("") }
    var expandida by rememberSaveable { mutableStateOf<String?>(null) }
    val listaState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val visibles = ManualContent.secciones.filter { seccion ->
        val porRol = filtroRol == ManualRole.TODOS ||
            seccion.roles.contains(filtroRol) ||
            seccion.roles.contains(ManualRole.TODOS)
        val texto = busqueda.trim()
        val porTexto = texto.isBlank() ||
            seccion.titulo.contains(texto, ignoreCase = true) ||
            seccion.resumen.contains(texto, ignoreCase = true) ||
            seccion.pasos.any { it.contains(texto, ignoreCase = true) }
        porRol && porTexto
    }
    val faqs = ManualContent.preguntasFrecuentes.filter { faq ->
        val porRol = filtroRol == ManualRole.TODOS ||
            faq.roles.contains(filtroRol) ||
            faq.roles.contains(ManualRole.TODOS)
        val texto = busqueda.trim()
        val porTexto = texto.isBlank() ||
            faq.pregunta.contains(texto, ignoreCase = true) ||
            faq.respuesta.contains(texto, ignoreCase = true)
        porRol && porTexto
    }

    // La cabecera ocupa tres elementos del LazyColumn antes de las secciones.
    val primerIndiceSeccion = 3
    val itemsManual = buildList {
        var parteActual: String? = null
        visibles.forEach { seccion ->
            if (seccion.parte != parteActual) {
                add(ItemManual.Parte(seccion.parte))
                parteActual = seccion.parte
            }
            add(ItemManual.Seccion(seccion))
        }
    }
    val indicePorSeccion = itemsManual.withIndex()
        .filter { it.value is ItemManual.Seccion }
        .associate { (indice, item) ->
            (item as ItemManual.Seccion).seccion.id to (primerIndiceSeccion + indice)
        }

    Scaffold(
        topBar = {
            SenaTopBar(
                title = "Manual de usuario",
                onBack = onBack,
                showProfile = false,
                showNotifications = false,
            )
        },
        containerColor = senaColors().background,
    ) { paddingValues ->
        LazyColumn(
            state = listaState,
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                PortadaManual(version = BuildConfig.VERSION_NAME)
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SenaChipRow {
                        SenaChip(
                            text = "Todos",
                            color = senaColors().green,
                            isSelected = filtroRol == ManualRole.TODOS,
                            onClick = { filtroManual = ManualRole.TODOS },
                        )
                        listOf(
                            ManualRole.APRENDIZ,
                            ManualRole.INSTRUCTOR,
                            ManualRole.ADMIN,
                        ).forEach { rol ->
                            SenaChip(
                                text = rol.etiqueta,
                                color = senaColors().green,
                                isSelected = filtroRol == rol,
                                onClick = { filtroManual = rol },
                            )
                        }
                    }
                    SenaTextField(
                        value = busqueda,
                        onValueChange = { busqueda = it },
                        label = "",
                        placeholder = "Buscar en el manual...",
                        leadingIcon = Icons.Default.Search,
                    )
                }
            }

            item {
                IndiceManual(
                    secciones = visibles,
                    onSeleccionar = { seccion ->
                        expandida = seccion.id
                        indicePorSeccion[seccion.id]?.let { indice ->
                            scope.launch {
                                listaState.animateScrollToItem(indice)
                            }
                        }
                    },
                )
            }

            if (visibles.isEmpty()) {
                item {
                    SenaCard {
                        Text(
                            "No hay capítulos que coincidan con la búsqueda para este rol.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = senaColors().textSecondary,
                        )
                    }
                }
            }

            items(itemsManual.size, key = { indice ->
                when (val item = itemsManual[indice]) {
                    is ItemManual.Parte -> "parte:${item.nombre}"
                    is ItemManual.Seccion -> item.seccion.id
                }
            }) { indice ->
                when (val item = itemsManual[indice]) {
                    is ItemManual.Parte -> EncabezadoParte(item.nombre)
                    is ItemManual.Seccion -> {
                        val seccion = item.seccion
                        SeccionManual(
                            seccion = seccion,
                            expandida = expandida == seccion.id,
                            onToggle = {
                                expandida = if (expandida == seccion.id) null else seccion.id
                            },
                        )
                    }
                }
            }

            item {
                PreguntasFrecuentes(faqs = faqs)
            }

            item {
                TextButton(
                    onClick = { scope.launch { listaState.animateScrollToItem(0) } },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Volver al inicio del manual", color = senaColors().green)
                }
            }
        }
    }
}

@Composable
private fun PortadaManual(version: String) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = senaColors().header,
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(18.dp),
                color = senaColors().green.copy(alpha = 0.18f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = senaColors().onHeader,
                        modifier = Modifier.size(30.dp),
                    )
                }
            }
            Text(
                "Manual de usuario",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = senaColors().onHeader,
            )
            Text(
                "Guía paso a paso de ProyecTwin para aprendices, instructores y administradores.",
                style = MaterialTheme.typography.bodyMedium,
                color = senaColors().onHeaderMuted,
            )
            Text(
                "Versión instalada: $version",
                style = MaterialTheme.typography.labelSmall,
                color = senaColors().onHeaderMuted,
            )
        }
    }
}

@Composable
private fun EncabezadoParte(nombre: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            nombre,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = senaColors().text,
        )
        Text(
            "Capítulos de esta parte",
            style = MaterialTheme.typography.labelSmall,
            color = senaColors().textLight,
        )
    }
}

@Composable
private fun IndiceManual(
    secciones: List<ManualSection>,
    onSeleccionar: (ManualSection) -> Unit,
) {
    SenaCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = senaColors().green)
                Spacer(Modifier.width(10.dp))
                Text(
                    "Índice",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            HorizontalDivider(color = senaColors().borderSoft)
            secciones.groupBy { it.parte }.forEach { (parte, capitulos) ->
                Text(
                    parte,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = senaColors().green,
                    modifier = Modifier.padding(top = 8.dp, start = 4.dp),
                )
                capitulos.forEach { seccion ->
                    TextButton(
                        onClick = { onSeleccionar(seccion) },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                    ) {
                        Text(
                            "${seccion.numero}. ${seccion.titulo}",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall,
                            color = senaColors().text,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SeccionManual(
    seccion: ManualSection,
    expandida: Boolean,
    onToggle: () -> Unit,
) {
    SenaCard(onClick = onToggle) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(34.dp),
                    shape = CircleShape,
                    color = senaColors().green.copy(alpha = 0.12f),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            seccion.numero,
                            fontWeight = FontWeight.Black,
                            color = senaColors().green,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        seccion.titulo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = senaColors().text,
                    )
                    Text(
                        "${seccion.parte} · " +
                            seccion.roles.joinToString(" · ") { it.etiqueta },
                        style = MaterialTheme.typography.labelSmall,
                        color = senaColors().textLight,
                    )
                }
                Icon(
                    if (expandida) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expandida) "Contraer capítulo" else "Expandir capítulo",
                    tint = senaColors().textLight,
                )
            }
            Text(
                seccion.resumen,
                style = MaterialTheme.typography.bodySmall,
                color = senaColors().textSecondary,
            )
            AnimatedVisibility(visible = expandida) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    HorizontalDivider(color = senaColors().borderSoft)
                    BloquePasos(seccion.pasos)
                    if (seccion.consejos.isNotEmpty()) {
                        BloqueConsejos(seccion.consejos)
                    }
                    seccion.figuras.forEach { figura ->
                        FiguraManual(figura)
                    }
                    if (seccion.relacionados.isNotEmpty()) {
                        Text(
                            "Ver también: " + seccion.relacionados.joinToString(", "),
                            style = MaterialTheme.typography.labelSmall,
                            color = senaColors().textLight,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BloquePasos(pasos: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Paso a paso", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        pasos.forEachIndexed { indice, paso ->
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    "${indice + 1}.",
                    modifier = Modifier.width(24.dp),
                    color = senaColors().green,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    paso,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = senaColors().textSecondary,
                )
            }
        }
    }
}

@Composable
private fun BloqueConsejos(consejos: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Info, contentDescription = null, tint = senaColors().info, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Consejos", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        }
        consejos.forEach { consejo ->
            Text(
                "· $consejo",
                style = MaterialTheme.typography.bodySmall,
                color = senaColors().textSecondary,
            )
        }
    }
}

@Composable
private fun FiguraManual(figura: ManualFigure) {
    val recurso = figura.recurso
    if (recurso == null) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = senaColors().backgroundElevated,
            border = BorderStroke(1.dp, senaColors().borderSoft),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    "Captura pendiente",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium,
                    color = senaColors().warning,
                )
                Text(
                    figura.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = senaColors().textSecondary,
                )
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Image(
                painter = painterResource(id = recurso),
                contentDescription = figura.descripcion,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .heightIn(max = 520.dp),
                contentScale = ContentScale.FillWidth,
            )
            Text(
                figura.descripcion,
                style = MaterialTheme.typography.labelSmall,
                color = senaColors().textLight,
            )
        }
    }
}

@Composable
private fun PreguntasFrecuentes(faqs: List<ManualFaq>) {
    SenaCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null, tint = senaColors().green)
                Spacer(Modifier.width(10.dp))
                Text(
                    "Preguntas frecuentes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            HorizontalDivider(color = senaColors().borderSoft)
            if (faqs.isEmpty()) {
                Text(
                    "No hay preguntas que coincidan con la búsqueda.",
                    style = MaterialTheme.typography.bodySmall,
                    color = senaColors().textSecondary,
                )
            } else {
                faqs.forEach { faq ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            faq.pregunta,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = senaColors().text,
                        )
                        Text(
                            faq.respuesta,
                            style = MaterialTheme.typography.bodySmall,
                            color = senaColors().textSecondary,
                        )
                    }
                }
            }
        }
    }
}
