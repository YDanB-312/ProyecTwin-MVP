package com.example.proyectwin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectwin.data.model.Ficha
import com.example.proyectwin.data.model.GeneralUser
import com.example.proyectwin.data.model.Project
import com.example.proyectwin.domain.repository.FichasRepository
import com.example.proyectwin.domain.repository.ProjectsRepository
import com.example.proyectwin.domain.repository.UsersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminFichaDetailData(
    val ficha: Ficha,
    val propuestas: List<Project> = emptyList(),
    /** La ficha cargó pero las propuestas fallaron: se avisa sin romper el detalle. */
    val propuestasError: Boolean = false,
)

sealed class AdminFichaDetailUiState {
    data object Loading : AdminFichaDetailUiState()
    data class Success(val data: AdminFichaDetailData) : AdminFichaDetailUiState()
    data class Error(val message: String) : AdminFichaDetailUiState()
}

/**
 * Detalle admin de una ficha: información + roster (viajan con la ficha) y las
 * propuestas de la ficha (`GET /projects?ficha_id=`). El backend manda: un 403
 * en la carga inicial se muestra como error; en el pull-to-refresh se conserva
 * el contenido anterior.
 */
@HiltViewModel
class AdminFichaDetailViewModel @Inject constructor(
    private val fichasRepository: FichasRepository,
    private val projectsRepository: ProjectsRepository,
    private val usersRepository: UsersRepository,
) : ViewModel() {

    private val _candidatos = MutableStateFlow<List<GeneralUser>>(emptyList())
    val candidatos: StateFlow<List<GeneralUser>> = _candidatos.asStateFlow()
    private val _accionError = MutableStateFlow<String?>(null)
    val accionError: StateFlow<String?> = _accionError.asStateFlow()

    private val _uiState = MutableStateFlow<AdminFichaDetailUiState>(AdminFichaDetailUiState.Loading)
    val uiState: StateFlow<AdminFichaDetailUiState> = _uiState.asStateFlow()

    /** Refresco silencioso en curso (pull-to-refresh), sin reemplazar el contenido. */
    private val _refrescando = MutableStateFlow(false)
    val refrescando: StateFlow<Boolean> = _refrescando.asStateFlow()

    private var fichaId: Int = 0

    fun load(id: Int) {
        fichaId = id
        viewModelScope.launch { cargar(id, mostrarCarga = true) }
    }

    fun recargar() {
        if (fichaId != 0) load(fichaId)
    }

    fun cargarCandidatos() {
        viewModelScope.launch {
            usersRepository.listar(role = "aprendiz").onSuccess { usuarios ->
                val enFicha = (_uiState.value as? AdminFichaDetailUiState.Success)
                    ?.data?.ficha?.estudiantes?.map { it.id }?.toSet().orEmpty()
                _candidatos.value = usuarios.filter { it.id !in enFicha }
            }
        }
    }

    fun agregarAprendiz(usuarioId: Int, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _accionError.value = null
            fichasRepository.agregarAprendiz(fichaId, usuarioId).fold(
                onSuccess = {
                    recargar()
                    cargarCandidatos()
                    onDone()
                },
                onFailure = { _accionError.value = it.message ?: "No se pudo agregar el aprendiz." },
            )
        }
    }

    fun crearAprendiz(
        nombre: String,
        apellido: String,
        tipoDocumento: String,
        numeroDocumento: String,
        correo: String,
        onDone: (String?) -> Unit = {},
    ) {
        viewModelScope.launch {
            _accionError.value = null
            fichasRepository.crearAprendizEnFicha(
                fichaId = fichaId,
                nombre = nombre,
                apellido = apellido,
                tipoDocumento = tipoDocumento,
                numeroDocumento = numeroDocumento,
                correo = correo,
            ).fold(
                onSuccess = { activacion ->
                    recargar()
                    cargarCandidatos()
                    onDone(
                        activacion?.mensaje
                            ?: "Aprendiz creado y matriculado en la ficha.",
                    )
                },
                onFailure = { _accionError.value = it.message ?: "No se pudo crear el aprendiz." },
            )
        }
    }

    fun quitarAprendiz(usuarioId: Int, onDone: () -> Unit = {}) {
        val ficha = (_uiState.value as? AdminFichaDetailUiState.Success)?.data?.ficha ?: return
        val programa = ficha.idPrograma ?: return
        val instructor = ficha.instructorId ?: return
        viewModelScope.launch {
            _accionError.value = null
            fichasRepository.actualizar(
                id = ficha.id,
                nombre = ficha.nombre.orEmpty(),
                numero = ficha.numero.orEmpty(),
                estado = ficha.estado,
                idPrograma = programa,
                idInstructor = instructor,
                aprendices = ficha.estudiantes.map { it.id }.filterNot { it == usuarioId },
            ).fold(
                onSuccess = {
                    recargar()
                    cargarCandidatos()
                    onDone()
                },
                onFailure = { _accionError.value = it.message ?: "No se pudo retirar el aprendiz." },
            )
        }
    }

    fun limpiarAccionError() {
        _accionError.value = null
    }

    /** Refresco silencioso (pull-to-refresh): mantiene el contenido visible. */
    fun refrescar() {
        val id = fichaId
        if (id == 0 || _refrescando.value) return
        viewModelScope.launch {
            _refrescando.value = true
            try {
                cargar(id, mostrarCarga = false)
            } finally {
                _refrescando.value = false
            }
        }
    }

    private suspend fun cargar(id: Int, mostrarCarga: Boolean) {
        if (mostrarCarga) _uiState.value = AdminFichaDetailUiState.Loading

        val ficha = fichasRepository.obtener(id).getOrElse { e ->
            if (mostrarCarga && fichaId == id) {
                _uiState.value = AdminFichaDetailUiState.Error(e.message ?: "No se pudo cargar la ficha.")
            }
            return
        }

        // Si el usuario ya navegó a otra ficha, la respuesta tardía se descarta.
        if (fichaId != id) return

        val resultadoPropuestas = projectsRepository.listar(fichaId = id)

        if (fichaId != id) return

        _uiState.value = AdminFichaDetailUiState.Success(
            AdminFichaDetailData(
                ficha = ficha,
                // Filtro defensivo: el backend ya filtra por `ficha_id`, pero una
                // propuesta ajena nunca debe colarse en el detalle de la ficha.
                propuestas = resultadoPropuestas.getOrDefault(emptyList()).filter { it.fichaId == id },
                propuestasError = resultadoPropuestas.isFailure,
            ),
        )
    }
}
