package com.example.proyectwin.domain.repository

import com.example.proyectwin.data.model.BugReport

/** Reportes de falla (`bug-reports`). */
interface BugReportsRepository {

    suspend fun listar(): Result<List<BugReport>>

    suspend fun obtener(id: Int): Result<BugReport>

    /** Alta de reporte: el backend lo firma con el usuario del token. */
    suspend fun crear(
        titulo: String?,
        descripcion: String,
        tipo: String,
        numeroFicha: String? = null,
        motivo: String? = null,
    ): Result<BugReport>

    /** PUT completo (cambio de estado desde admin, u otra edición). */
    suspend fun actualizar(id: Int, reporte: BugReport): Result<BugReport>

    suspend fun eliminar(id: Int): Result<Unit>
}
