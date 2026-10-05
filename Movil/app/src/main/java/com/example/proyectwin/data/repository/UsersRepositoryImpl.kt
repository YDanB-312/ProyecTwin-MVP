package com.example.proyectwin.data.repository

import com.example.proyectwin.data.api.dto.UserCreateRequest
import com.example.proyectwin.data.api.safeApiCall
import com.example.proyectwin.data.api.service.UsersApi
import com.example.proyectwin.data.local.SessionManager
import com.example.proyectwin.data.mapper.toDomain
import com.example.proyectwin.data.mapper.toUpdateRequest
import com.example.proyectwin.data.model.GeneralUser
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
        correo: String,
        password: String,
        rol: String,
    ): Result<GeneralUser> = safeApiCall {
        api.crear(
            UserCreateRequest(
                nombre = nombre,
                apellido = apellido,
                correo = correo.trim().lowercase(),
                password = password,
                rol = rol,
            ),
        )
    }.map { it.toDomain() }

    override suspend fun actualizar(id: Int, datos: GeneralUser, password: String?): Result<GeneralUser> =
        safeApiCall { api.actualizar(id, datos.toUpdateRequest(password)) }.map { it.toDomain() }

    override suspend fun eliminar(id: Int): Result<Unit> =
        safeApiCall { api.eliminar(id) }.map {}

    companion object {
        private const val INCLUDE_CUENTA = "apprentice.classGroup,instructor"
    }
}
