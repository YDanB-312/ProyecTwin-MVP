package com.example.proyectwin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectwin.data.api.ApiException
import com.example.proyectwin.data.model.ApprenticeProfile
import com.example.proyectwin.data.model.CredencialesUsuario
import com.example.proyectwin.data.model.Ficha
import com.example.proyectwin.data.model.GeneralUser
import com.example.proyectwin.domain.repository.FichasRepository
import com.example.proyectwin.domain.repository.MembersRepository
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
    /** Nuevas credenciales para mostrar una sola vez. */
    data class Credenciales(val credenciales: CredencialesUsuario) : AdminUserAction()
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
                _fichasDisponibles.value = fichasRepository.listar().getOrDefault(emptyList())
            }
            _uiState.value = AdminUserUiState.Success(
                AdminUserDetail(usuario = usuario, aprendiz = aprendiz, fichaActual = ficha),
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

    fun eliminar() {
        if (_accion.value is AdminUserAction.Loading) return
        viewModelScope.launch {
            _accion.value = AdminUserAction.Loading
            usersRepository.eliminar(idUsuario).fold(
                onSuccess = { _accion.value = AdminUserAction.Deleted },
                onFailure = { e ->
                    _accion.value = AdminUserAction.Error(e.message ?: "No se pudo eliminar el usuario.")
                },
            )
        }
    }

    fun restablecerCredenciales() {
        if (_accion.value is AdminUserAction.Loading) return
        viewModelScope.launch {
            _accion.value = AdminUserAction.Loading
            usersRepository.restablecerCredenciales(idUsuario).fold(
                onSuccess = { _accion.value = AdminUserAction.Credenciales(it) },
                onFailure = { e ->
                    _accion.value = AdminUserAction.Error(e.message ?: "No se pudieron restablecer las credenciales.")
                },
            )
        }
    }

    fun reenviarCredenciales() {
        if (_accion.value is AdminUserAction.Loading) return
        viewModelScope.launch {
            _accion.value = AdminUserAction.Loading
            usersRepository.reenviarCredenciales(idUsuario).fold(
                onSuccess = {
                    _accion.value = AdminUserAction.Message("Credenciales reenviadas al correo.")
                    recargar()
                },
                onFailure = { e ->
                    _accion.value = AdminUserAction.Error(e.message ?: "No se pudieron reenviar las credenciales.")
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
