package com.example.proyectwin.data.repository

import com.example.proyectwin.data.api.dto.DeleteUserRequest
import com.example.proyectwin.data.api.dto.UserCreateRequest
import com.example.proyectwin.data.api.dto.UsuarioActivacionDto
import com.example.proyectwin.data.api.safeApiCall
import com.example.proyectwin.data.api.service.UsersApi
import com.example.proyectwin.data.local.SessionManager
import com.example.proyectwin.data.mapper.toDomain
import com.example.proyectwin.data.mapper.toUpdateRequest
import com.example.proyectwin.data.model.ActivacionUsuario
import com.example.proyectwin.data.model.GeneralUser
import com.example.proyectwin.data.model.PaginaUsuarios
import com.example.proyectwin.domain.repository.UsersRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsersRepositoryImpl @Inject constructor(
    private val api: UsersApi,
    private val sessionManager: SessionManager,
) : UsersRepository {

    override suspend fun listar(
        search: String?,
        role: String?,
        estado: String?,
        fichaId: Int?,
    ): Result<List<GeneralUser>> =
        safeApiCall { api.listar(search, role, estado, fichaId, null, INCLUDE_CUENTA) }
            .map { lista -> lista.map { it.toDomain() } }

    override suspend fun listarPaginado(
        page: Int,
        porPagina: Int,
        search: String?,
        role: String?,
        estado: String?,
        fichaId: String?,
        programa: String?,
    ): Result<PaginaUsuarios> =
        safeApiCall {
            api.listarPaginado(
                paginado = 1,
                page = page,
                porPagina = porPagina,
                search = search,
                role = role,
                estado = estado,
                fichaId = fichaId,
                programa = programa,
                included = INCLUDE_CUENTA,
            )
        }.map { dto ->
            PaginaUsuarios(
                items = dto.data.map { it.toDomain() },
                currentPage = dto.currentPage,
                lastPage = dto.lastPage,
                total = dto.total,
            )
        }

    override suspend fun obtener(id: Int): Result<GeneralUser> =
        safeApiCall { api.obtener(id, INCLUDE_CUENTA) }.map { it.toDomain() }

    override suspend fun perfil(id: Int): Result<GeneralUser> =
        safeApiCall { api.perfil(id) }.map { dto ->
            GeneralUser(
                id = dto.id,
                name = "${dto.nombre} ${dto.apellido}".trim().ifEmpty { dto.correo },
                email = dto.correo,
                role = dto.rol,
                fotoPerfil = dto.fotoUrl,
                nombre = dto.nombre,
                apellido = dto.apellido,
            )
        }

    override suspend fun miCuenta(): Result<GeneralUser> {
        val id = sessionManager.currentUser.first()?.id
            ?: return Result.failure(IllegalStateException("No hay sesión activa."))
        return safeApiCall { api.obtener(id, INCLUDE_CUENTA) }.mapCatching { dto ->
            // saveUser conserva el token actual (no lo pisa cuando viene null).
            val cuenta = dto.toDomain()
            sessionManager.saveUser(cuenta)
            cuenta
        }
    }

    override suspend fun crear(
        nombre: String,
        apellido: String,
        tipoDocumento: String,
        numeroDocumento: String,
        correo: String,
        rol: String,
    ): Result<ActivacionUsuario> = safeApiCall {
        api.crear(
            UserCreateRequest(
                nombre = nombre,
                apellido = apellido,
                tipoDocumento = tipoDocumento,
                numeroDocumento = numeroDocumento,
                correo = correo.trim().lowercase(),
                rol = rol,
            ),
        )
    }.map { it.toDomain() }

    override suspend fun actualizar(id: Int, datos: GeneralUser): Result<GeneralUser> =
        safeApiCall { api.actualizar(id, datos.toUpdateRequest()) }.map { it.toDomain() }

    override suspend fun eliminar(id: Int, confirmar: Boolean, hash: String?): Result<Unit> =
        safeApiCall {
            api.eliminar(id, DeleteUserRequest(confirmarEliminacion = confirmar, confirmacionHash = hash))
        }.map {}

    override suspend fun restablecerCredenciales(id: Int): Result<String> =
        safeApiCall { api.restablecerCredenciales(id) }.map { it.message }

    override suspend fun reenviarActivacion(id: Int): Result<String> =
        safeApiCall { api.reenviarActivacion(id) }.map { it.message }

    private fun UsuarioActivacionDto.toDomain(): ActivacionUsuario = ActivacionUsuario(
        usuario = usuario.toDomain(),
        enviada = activacion.enviada,
        correo = activacion.correo.ifBlank { usuario.correo },
        mensaje = activacion.mensaje,
    )

    companion object {
        // `apprentice.classGroup` no está en allowIncluded de general_users y se
        // ignoraba en silencio; la ficha se resuelve por /apprentices.
        private const val INCLUDE_CUENTA = "apprentice,instructor"
    }
}
