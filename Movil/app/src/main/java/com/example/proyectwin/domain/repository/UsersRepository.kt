package com.example.proyectwin.domain.repository

import com.example.proyectwin.data.model.ActivacionUsuario
import com.example.proyectwin.data.model.GeneralUser
import com.example.proyectwin.data.model.PaginaUsuarios

/** Cuentas de usuario (`general-users`): listado admin, gestión y credenciales. */
interface UsersRepository {

    suspend fun listar(
        search: String? = null,
        role: String? = null,
        estado: String? = null,
        fichaId: Int? = null,
    ): Result<List<GeneralUser>>

    /**
     * Listado paginado del servidor (panel admin). `fichaId` acepta el id de la
     * ficha como texto o `sin` (usuarios sin ficha); `null` = sin filtrar.
     */
    suspend fun listarPaginado(
        page: Int,
        porPagina: Int,
        search: String? = null,
        role: String? = null,
        estado: String? = null,
        fichaId: String? = null,
        programa: String? = null,
    ): Result<PaginaUsuarios>

    suspend fun obtener(id: Int): Result<GeneralUser>

    /** Perfil público (solo datos básicos de contacto). */
    suspend fun perfil(id: Int): Result<GeneralUser>

    /** Cuenta del token con ficha/perfil incluidos; refresca la sesión local. */
    suspend fun miCuenta(): Result<GeneralUser>

    /** Alta (admin): cuenta pendiente + enlace de activación por correo. */
    suspend fun crear(
        nombre: String,
        apellido: String,
        tipoDocumento: String,
        numeroDocumento: String,
        correo: String,
        rol: String,
    ): Result<ActivacionUsuario>

    /** PUT parcial: [datos] aporta nombre/apellido/correo/rol/estado. */
    suspend fun actualizar(id: Int, datos: GeneralUser): Result<GeneralUser>

    /**
     * Borrado normal ([confirmar] = false). Con historial, el backend responde
     * 409 con `requiere_confirmacion`, `impacto` y `confirmacion_hash`.
     */
    suspend fun eliminar(id: Int, confirmar: Boolean = false, hash: String? = null): Result<Unit>

    /** Envía un enlace de restablecimiento (o reenvía activación si está pendiente). */
    suspend fun restablecerCredenciales(id: Int): Result<String>

    /** Reenvía el enlace de activación vigente. */
    suspend fun reenviarActivacion(id: Int): Result<String>
}
