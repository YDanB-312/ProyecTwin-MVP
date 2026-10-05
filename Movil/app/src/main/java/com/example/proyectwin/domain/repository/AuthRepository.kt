package com.example.proyectwin.domain.repository

import com.example.proyectwin.data.model.GeneralUser
import kotlinx.coroutines.flow.Flow

/** Sesión y cuentas: login/registro, perfil propio y credenciales. */
interface AuthRepository {

    val currentUser: Flow<GeneralUser?>
    val isLoggedIn: Flow<Boolean>

    suspend fun login(email: String, password: String): Result<GeneralUser>

    suspend fun register(name: String, email: String, password: String, role: String): Result<GeneralUser>

    /** Cierra sesión en el servidor (best effort) y limpia la sesión local. */
    suspend fun logout(): Result<Unit>

    /** Refresca los datos del usuario del token (incluye ficha/perfil) y la sesión local. */
    suspend fun refreshSession(): Result<GeneralUser>

    suspend fun updateProfile(nombre: String, apellido: String, correo: String): Result<GeneralUser>

    suspend fun updateFoto(fotoUrl: String?): Result<GeneralUser>

    suspend fun changePassword(passwordActual: String, password: String, passwordConfirmation: String): Result<Unit>

    suspend fun changeEmail(correo: String, passwordActual: String): Result<Unit>

    /** Devuelve el mensaje del backend (y la url de restablecimiento si viene). */
    suspend fun forgotPassword(correo: String): Result<String>

    suspend fun resetPassword(token: String, correo: String, password: String, passwordConfirmation: String): Result<Unit>
}
