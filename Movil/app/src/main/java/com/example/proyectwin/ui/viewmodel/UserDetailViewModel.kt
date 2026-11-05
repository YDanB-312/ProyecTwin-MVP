package com.example.proyectwin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectwin.data.api.ApiException
import com.example.proyectwin.data.model.ApprenticeProfile
import com.example.proyectwin.data.model.Ficha
import com.example.proyectwin.data.model.GeneralUser
import com.example.proyectwin.data.model.Project
import com.example.proyectwin.domain.repository.FichasRepository
import com.example.proyectwin.domain.repository.MembersRepository
import com.example.proyectwin.domain.repository.ProjectsRepository
import com.example.proyectwin.domain.repository.UsersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminUserDetail(
    val usuario: GeneralUser,
    val aprendiz: ApprenticeProfile? = null,
    val fichaActual: Ficha? = null,
    val propuestas: List<Project> = emptyList(),
)

sealed class AdminUserUiState {
    data object Loading : AdminUserUiState()
    data class Success(val data: AdminUserDetail) : AdminUserUiState()
    data class Error(val message: String) : AdminUserUiState()
}

sealed class AdminUserAction {
    data object Idle : AdminUserAction()
    data object Loading : AdminUserAction()
    data object Saved : AdminUserAction()
    data object Deleted : AdminUserAction()
    /**
     * El backend bloqueó el borrado (409): hay historial relacionado y se debe
     * elegir entre desactivar o confirmar la eliminación en cascada.
     */
    data class DecisionEliminacion(
        val message: String,
        val impacto: Map<String, Int>,
        val hash: String?,
        val puedeDesactivar: Boolean,
    ) : AdminUserAction()
    data class Message(val text: String) : AdminUserAction()
    data class Error(
        val message: String,
        val fieldErrors: Map<String, String> = emptyMap(),
    ) : AdminUserAction()
}

/** Detalle y gestión de una cuenta desde el panel admin. */
@HiltViewModel
class UserDetailViewModel @Inject constructor(
    private val usersRepository: UsersRepository,
    private val membersRepository: MembersRepository,
    private val fichasRepository: FichasRepository,
    private val projectsRepository: ProjectsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdminUserUiState>(AdminUserUiState.Loading)
    val uiState: StateFlow<AdminUserUiState> = _uiState.asStateFlow()

    private val _accion = MutableStateFlow<AdminUserAction>(AdminUserAction.Idle)
    val accion: StateFlow<AdminUserAction> = _accion.asStateFlow()

    /** Fichas disponibles para trasladar a un aprendiz. */
    private val _fichasDisponibles = MutableStateFlow<List<Ficha>>(emptyList())
    val fichasDisponibles: StateFlow<List<Ficha>> = _fichasDisponibles.asStateFlow()

    private var idUsuario: Int = 0

    fun resetAccion() {
        _accion.value = AdminUserAction.Idle
    }

    fun load(id: Int) {
        idUsuario = id
        viewModelScope.launch {
            _uiState.value = AdminUserUiState.Loading
            val usuario = usersRepository.obtener(id).getOrElse { e ->
                _uiState.value = AdminUserUiState.Error(e.message ?: "No se pudo cargar el usuario.")
                return@launch
            }
            val aprendiz = membersRepository.aprendizPorUsuario(id).getOrNull()
            val ficha = aprendiz?.idClassGroup?.let { fichasRepository.obtener(it).getOrNull() }
            if (aprendiz != null && _fichasDisponibles.value.isEmpty()) {
                // Solo fichas activas: mover a una finalizada/anulada deja al
                // aprendiz en un estado que el backend rechaza o considera histórico.
                _fichasDisponibles.value = fichasRepository.listar()
                    .getOrDefault(emptyList())
                    .filter {
                        it.estado == "activo" || it.id == aprendiz.idClassGroup
                    }
            }
            val propuestas = projectsRepository.listar()
                .getOrDefault(emptyList())
                .filter { proyecto ->
                    proyecto.studentId == id || proyecto.equipo.any { it.id == id }
                }
            _uiState.value = AdminUserUiState.Success(
                AdminUserDetail(
                    usuario = usuario,
                    aprendiz = aprendiz,
                    fichaActual = ficha,
                    propuestas = propuestas,
                ),
            )
        }
    }

    fun recargar() {
        if (idUsuario != 0) load(idUsuario)
    }

    /** Guarda nombre/apellido/correo/rol/estado (el backend valida reglas). */
    fun guardar(datos: GeneralUser) {
        ejecutar(recargarTrasExito = true) { usersRepository.actualizar(idUsuario, datos) }
    }

    /** Activa o suspende la cuenta (`estado`). */
    fun cambiarEstado(activo: Boolean) {
        val actual = (_uiState.value as? AdminUserUiState.Success)?.data ?: return
        guardar(actual.usuario.copy(estado = activo))
    }

    fun eliminar(confirmar: Boolean = false, hash: String? = null) {
        if (_accion.value is AdminUserAction.Loading) return
        viewModelScope.launch {
            _accion.value = AdminUserAction.Loading
            usersRepository.eliminar(idUsuario, confirmar = confirmar, hash = hash).fold(
                onSuccess = { _accion.value = AdminUserAction.Deleted },
                onFailure = { e ->
                    val http = e as? ApiException.Http
                    if (http != null && http.status == 409 && http.requiereConfirmacion) {
                        _accion.value = AdminUserAction.DecisionEliminacion(
                            message = http.message ?: "El usuario tiene historial relacionado.",
                            impacto = http.impacto,
                            hash = http.confirmacionHash,
                            puedeDesactivar = http.puedeDesactivar,
                        )
                    } else {
                        _accion.value = AdminUserAction.Error(
                            e.message ?: "No se pudo eliminar el usuario.",
                        )
                    }
                },
            )
        }
    }

    /** Restablece la clave (o reenvía activación si aún no está verificada). */
    fun restablecerCredenciales() {
        if (_accion.value is AdminUserAction.Loading) return
        viewModelScope.launch {
            _accion.value = AdminUserAction.Loading
            usersRepository.restablecerCredenciales(idUsuario).fold(
                onSuccess = { mensaje ->
                    _accion.value = AdminUserAction.Message(mensaje)
                    recargar()
                },
                onFailure = { e ->
                    _accion.value = AdminUserAction.Error(
                        e.message ?: "No se pudo enviar el enlace de restablecimiento.",
                    )
                },
            )
        }
    }

    fun reenviarActivacion() {
        if (_accion.value is AdminUserAction.Loading) return
        viewModelScope.launch {
            _accion.value = AdminUserAction.Loading
            usersRepository.reenviarActivacion(idUsuario).fold(
                onSuccess = { mensaje ->
                    _accion.value = AdminUserAction.Message(mensaje)
                    recargar()
                },
                onFailure = { e ->
                    _accion.value = AdminUserAction.Error(
                        e.message ?: "No se pudo reenviar la activación.",
                    )
                },
            )
        }
    }

    /** Mueve al aprendiz a otra ficha (o lo desvincula con null). */
    fun moverAprendiz(nuevaFichaId: Int?) {
        val actual = (_uiState.value as? AdminUserUiState.Success)?.data ?: return
        val aprendiz = actual.aprendiz ?: return
        ejecutar(recargarTrasExito = true) {
            membersRepository.actualizarAprendiz(
                id = aprendiz.id,
                codigo = aprendiz.codigo,
                idUsuario = aprendiz.idUsuario,
                idClassGroup = nuevaFichaId,
                idPrograma = null,
            )
        }
    }

    private fun ejecutar(recargarTrasExito: Boolean, bloque: suspend () -> Result<*>) {
        if (_accion.value is AdminUserAction.Loading) return // doble toque
        viewModelScope.launch {
            _accion.value = AdminUserAction.Loading
            bloque().fold(
                onSuccess = {
                    _accion.value = AdminUserAction.Saved
                    if (recargarTrasExito) recargar()
                },
                onFailure = { e ->
                    _accion.value = AdminUserAction.Error(
                        message = e.message ?: "No se pudo completar la acción.",
                        fieldErrors = (e as? ApiException.Http)?.fieldErrors.orEmpty(),
                    )
                },
            )
        }
    }
}
