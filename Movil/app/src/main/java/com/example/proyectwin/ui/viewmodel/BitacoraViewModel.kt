package com.example.proyectwin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectwin.data.model.AuditLog
import com.example.proyectwin.data.model.GeneralUser
import com.example.proyectwin.domain.repository.AuditRepository
import com.example.proyectwin.domain.repository.UsersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Registros por página de la bitácora (igual que la web: `PAGINA_TABLA`). */
const val REGISTROS_POR_PAGINA = 8

/** Filtros aplicados de la bitácora (null = sin filtrar; "Todos" no viaja). */
data class BitacoraFiltros(
    val accion: String? = null,
    val entidad: String? = null,
    val idUsuario: Int? = null,
    val desde: String? = null,
    val hasta: String? = null,
    val buscar: String? = null,
    val rol: String? = null,
    val orden: String? = null,
) {
    val hayFiltros: Boolean
        get() = accion != null || entidad != null || idUsuario != null || desde != null ||
            hasta != null || !buscar.isNullOrBlank() || rol != null || orden != null
}

sealed class BitacoraUiState {
    data object Loading : BitacoraUiState()

    data class Success(
        val registros: List<AuditLog>,
        val filtros: BitacoraFiltros = BitacoraFiltros(),
        val pagina: Int = 1,
        val totalPaginas: Int = 1,
        val totalRegistros: Int = 0,
    ) : BitacoraUiState() {
        /** El servidor ya entregó solo la página solicitada. */
        val visibles: List<AuditLog> get() = registros
    }

    data class Error(val message: String) : BitacoraUiState()
}

/** Bitácora inmutable (`GET /audit-logs?paginado=1`) con filtros del servidor. */
@HiltViewModel
class BitacoraViewModel @Inject constructor(
    private val auditRepository: AuditRepository,
    private val usersRepository: UsersRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<BitacoraUiState>(BitacoraUiState.Loading)
    val uiState: StateFlow<BitacoraUiState> = _uiState.asStateFlow()

    private val _usuarios = MutableStateFlow<List<GeneralUser>>(emptyList())
    val usuarios: StateFlow<List<GeneralUser>> = _usuarios.asStateFlow()

    private var filtros = BitacoraFiltros()
    private var paginaActual = 1
    private var trabajoCarga: Job? = null
    private var versionSolicitud = 0

    init {
        cargar()
        cargarUsuarios()
    }

    fun load(
        accion: String? = null,
        entidad: String? = null,
        idUsuario: Int? = null,
        desde: String? = null,
        hasta: String? = null,
        buscar: String? = null,
        rol: String? = null,
        orden: String? = null,
    ) {
        filtros = BitacoraFiltros(
            accion = accion,
            entidad = entidad,
            idUsuario = idUsuario,
            desde = desde,
            hasta = hasta,
            buscar = buscar?.trim()?.ifBlank { null },
            rol = rol,
            orden = orden,
        )
        paginaActual = 1
        cargar()
    }

    fun limpiar() {
        filtros = BitacoraFiltros()
        paginaActual = 1
        cargar()
    }

    fun recargar() = cargar()

    fun irAPagina(pagina: Int) {
        val destino = pagina.coerceAtLeast(1)
        if (destino != paginaActual) {
            paginaActual = destino
            cargar()
        }
    }

    fun paginaSiguiente() {
        val actual = _uiState.value as? BitacoraUiState.Success ?: return
        if (paginaActual < actual.totalPaginas) irAPagina(paginaActual + 1)
    }

    fun paginaAnterior() {
        if (paginaActual > 1) irAPagina(paginaActual - 1)
    }

    private fun cargar() {
        val f = filtros
        val pagina = paginaActual
        val version = ++versionSolicitud
        trabajoCarga?.cancel()
        trabajoCarga = viewModelScope.launch {
            _uiState.value = BitacoraUiState.Loading
            auditRepository.listarPaginado(
                page = pagina,
                porPagina = REGISTROS_POR_PAGINA,
                buscar = f.buscar,
                accion = f.accion,
                entidad = f.entidad,
                idUsuario = f.idUsuario,
                rol = f.rol,
                desde = f.desde,
                hasta = f.hasta,
                orden = f.orden,
            ).fold(
                onSuccess = { resultado ->
                    if (version != versionSolicitud) return@launch
                    // Si el servidor corrige la página (p. ej. borrado), se refleja.
                    paginaActual = resultado.pagina
                    _uiState.value = BitacoraUiState.Success(
                        registros = resultado.registros,
                        filtros = f,
                        pagina = resultado.pagina,
                        totalPaginas = resultado.totalPaginas.coerceAtLeast(1),
                        totalRegistros = resultado.totalRegistros,
                    )
                },
                onFailure = { e ->
                    if (version == versionSolicitud) {
                        _uiState.value = BitacoraUiState.Error(
                            e.message ?: "No se pudo cargar la bitácora.",
                        )
                    }
                },
            )
        }
    }

    private fun cargarUsuarios() {
        viewModelScope.launch {
            usersRepository.listar().onSuccess { _usuarios.value = it }
        }
    }
}
