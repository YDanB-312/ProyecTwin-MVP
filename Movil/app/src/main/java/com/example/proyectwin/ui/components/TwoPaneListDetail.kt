package com.example.proyectwin.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler

/**
 * Contenedor maestro-detalle para tabletas. En modo expandido mantiene la
 * lista a la izquierda y el detalle (o un estado vacío) a la derecha; en
 * compacto deja que cada pantalla gestione su navegación normal.
 */
@Composable
fun TwoPaneListDetail(
    expanded: Boolean,
    selectedId: Int?,
    onSelected: (Int?) -> Unit,
    list: @Composable (onOpen: (Int) -> Unit) -> Unit,
    detail: @Composable (id: Int, onClose: () -> Unit) -> Unit,
) {
    if (!expanded) {
        CompositionLocalProvider(LocalSelectedListItemId provides selectedId) {
            list { id -> onSelected(id) }
        }
        return
    }

    // En tableta, el botón Atrás cierra primero la selección del panel derecho.
    BackHandler(enabled = selectedId != null) { onSelected(null) }

    Row(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.width(360.dp).fillMaxHeight()) {
            CompositionLocalProvider(LocalSelectedListItemId provides selectedId) {
                list { id -> onSelected(id) }
            }
        }
        VerticalDivider()
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            if (selectedId == null) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    SenaEmptyState(
                        title = "Sin selección",
                        message = "Selecciona un elemento de la lista para ver su detalle.",
                        icon = Icons.AutoMirrored.Filled.List,
                    )
                }
            } else {
                detail(selectedId) { onSelected(null) }
            }
        }
    }
}
