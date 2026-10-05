package com.example.proyectwin.data.repository

import com.example.proyectwin.data.api.TokenProvider
import com.example.proyectwin.data.api.dto.ChangeEmailRequest
import com.example.proyectwin.data.api.dto.ChangePasswordRequest
import com.example.proyectwin.data.api.dto.ForgotPasswordRequest
import com.example.proyectwin.data.api.dto.LoginRequest
import com.example.proyectwin.data.api.dto.RegisterRequest
import com.example.proyectwin.data.api.dto.ResetPasswordRequest
import com.example.proyectwin.data.api.service.AuthApi
import com.example.proyectwin.data.api.service.UsersApi
import com.example.proyectwin.data.api.safeApiCall
import com.example.proyectwin.data.local.SessionManager
import com.example.proyectwin.data.mapper.dividirNombre
import com.example.proyectwin.data.mapper.toDomain
import com.example.proyectwin.data.mapper.toUpdateRequest
import com.example.proyectwin.data.model.GeneralUser
import com.example.proyectwin.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val usersApi: UsersApi,
    private val sessionManager: SessionManager,
    private val tokenProvider: TokenProvider,
) : AuthRepository {

    override val currentUser: Flow<GeneralUser?> = sessionManager.currentUser
    override val isLoggedIn: Flow<Boolean> = sessionManager.isLoggedIn

    override suspend fun login(email: String, password: String): Result<GeneralUser> {
        val credenciales = LoginRequest(correo = email.trim().lowercase(), password = password)
        val respuesta = safeApiCall { api.login(credenciales) }.getOrElse { return Result.failure(it) }

        // Enriquece con ficha/perfil; si falla, la sesión se armа con lo del login.
        val cuenta = runCatching {
            usersApi.obtener(respuesta.user.id, INCLUDE_CUENTA)
        }.getOrNull()?.toDomain() ?: respuesta.user.toDomain()

        val sesion = cuenta.copy(token = respuesta.token)
        sessionManager.saveUser(sesion)
        tokenProvider.update(respuesta.token)
        return Result.success(sesion)
    }

    override suspend fun register(name: String, email: String, password: String, role: String): Result<GeneralUser> {
        val (nombre, apellido) = dividirNombre(name)
        val dto = safeApiCall {
            api.register(
                RegisterRequest(
                    nombre = nombre,
                    apellido = apellido,
                    correo = email.trim().lowercase(),
                    password = password,
                    rol = role,
                ),
            )
        }.getOrElse { return Result.failure(it) }
        // Como el frontend: crea la cuenta sin abrir sesión (luego se loguea).
        return Result.success(dto.toDomain())
    }

    override suspend fun logout(): Result<Unit> {
        runCatching { api.logout() } // best effort: aunque falle, se limpia local
        tokenProvider.update(null)
        return runCatching { sessionManager.clearSession() }
    }

    override suspend fun refreshSession(): Result<GeneralUser> {
        val id = sessionManager.currentUser.first()?.id
            ?: return Result.failure(IllegalStateException("No hay sesión activa."))
        return safeApiCall { usersApi.obtener(id, INCLUDE_CUENTA) }.mapCatching { dto ->
            val cuenta = dto.toDomain().copy(token = sessionManager.getToken())
            sessionManager.saveUser(cuenta)
            cuenta
        }
    }

    override suspend fun updateProfile(nombre: String, apellido: String, correo: String): Result<GeneralUser> {
        val actual = sessionManager.currentUser.first()
            ?: return Result.failure(IllegalStateException("No hay sesión activa."))
        return safeApiCall {
            usersApi.actualizar(actual.id, actual.copy(nombre = nombre, apellido = apellido, email = correo).toUpdateRequest())
        }.mapCatching { dto ->
            val cuenta = dto.toDomain().copy(token = sessionManager.getToken())
            sessionManager.saveUser(cuenta)
            cuenta
        }
    }

    override suspend fun updateFoto(fotoUrl: String?): Result<GeneralUser> {
        val actual = sessionManager.currentUser.first()
            ?: return Result.failure(IllegalStateException("No hay sesión activa."))
        return safeApiCall {
            usersApi.actualizar(actual.id, actual.copy(fotoPerfil = fotoUrl).toUpdateRequest())
        }.mapCatching { dto ->
            val cuenta = dto.toDomain().copy(token = sessionManager.getToken())
            sessionManager.saveUser(cuenta)
            sessionManager.updateFoto(fotoUrl)
            cuenta
        }
    }

    override suspend fun changePassword(
        passwordActual: String,
        password: String,
        passwordConfirmation: String,
    ): Result<Unit> = safeApiCall {
        api.changePassword(
            ChangePasswordRequest(
                passwordActual = passwordActual,
                password = password,
                passwordConfirmation = passwordConfirmation,
            ),
        )
    }.map {}

    override suspend fun changeEmail(correo: String, passwordActual: String): Result<Unit> =
        safeApiCall { api.changeEmail(ChangeEmailRequest(correo = correo, passwordActual = passwordActual)) }
            .mapCatching { respuesta ->
                val actual = sessionManager.currentUser.first()
                if (actual != null) {
                    sessionManager.saveUser(actual.copy(email = respuesta.correo.ifEmpty { correo }))
                }
            }

    override suspend fun forgotPassword(correo: String): Result<String> =
        safeApiCall { api.forgotPassword(ForgotPasswordRequest(correo = correo)) }
            .map { it.message.ifEmpty { it.resetUrl ?: "" } }

    override suspend fun resetPassword(
        token: String,
        correo: String,
        password: String,
        passwordConfirmation: String,
    ): Result<Unit> = safeApiCall {
        api.resetPassword(
            ResetPasswordRequest(
                token = token,
                correo = correo,
                password = password,
                passwordConfirmation = passwordConfirmation,
            ),
        )
    }.map {}

    companion object {
        private const val INCLUDE_CUENTA = "apprentice.classGroup,instructor"
    }
}
