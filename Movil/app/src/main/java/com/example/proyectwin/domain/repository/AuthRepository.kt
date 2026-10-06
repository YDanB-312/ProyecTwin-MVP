package com.example.proyectwin.domain.repository

import com.example.proyectwin.data.model.GeneralUser
import kotlinx.coroutines.flow.Flow

/** Sesión y cuenta propia: login, perfil y credenciales. */
interface AuthRepository {

    val currentUser: Flow<GeneralUser?>
    val isLoggedIn: Flow<Boolean>

    /** Acceso por `username` (el correo es solo para recuperación). */
    suspend fun login(username: String, password: String, recordarme: Boolean = false): Result<GeneralUser>

    /** Cierra sesión en el servidor (best effort) y limpia la sesión local. */
    suspend fun logout(): Result<Unit>

    /**
     * Valida el token contra `GET /auth/me`, refresca la cuenta (incluida la
     * ficha del aprendiz) y la sesión local. Un 401 dispara el cierre global.
     */
    suspend fun refreshSession(): Result<GeneralUser>

    /** Edición del propio perfil (el correo se cambia con [changeEmail]). */
    suspend fun updateProfile(nombre: String, apellido: String): Result<GeneralUser>

    suspend fun updateFoto(fotoUrl: String?): Result<GeneralUser>

    /** Cambio de la contraseña propia; al lograrlo limpia `must_change_password`. */
    suspend fun changePassword(passwordActual: String, password: String, passwordConfirmation: String): Result<Unit>

    suspend fun changeEmail(correo: String, passwordActual: String): Result<Unit>

    /** Devuelve el mensaje del backend (y la url de restablecimiento si viene). */
    suspend fun forgotPassword(correo: String): Result<String>

    suspend fun resetPassword(token: String, correo: String, password: String, passwordConfirmation: String): Result<Unit>
}
