package com.example.proyectwin.domain.repository

import com.example.proyectwin.data.model.GeneralUser

/** Cuentas de usuario (`general-users`): listado admin y gestión. */
interface UsersRepository {

    suspend fun listar(
        search: String? = null,
        role: String? = null,
        estado: String? = null,
        fichaId: Int? = null,
    ): Result<List<GeneralUser>>

    suspend fun obtener(id: Int): Result<GeneralUser>

    /** Perfil público (solo datos básicos de contacto). */
    suspend fun perfil(id: Int): Result<GeneralUser>

    /** Cuenta del token con ficha/perfil incluidos; refresca la sesión local. */
    suspend fun miCuenta(): Result<GeneralUser>

    suspend fun crear(
        nombre: String,
        apellido: String,
        correo: String,
        password: String,
        rol: String,
    ): Result<GeneralUser>

    /** PUT parcial: [datos] aporta nombre/apellido/correo/rol/estado; [password] solo si cambia. */
    suspend fun actualizar(id: Int, datos: GeneralUser, password: String? = null): Result<GeneralUser>

    suspend fun eliminar(id: Int): Result<Unit>
}
