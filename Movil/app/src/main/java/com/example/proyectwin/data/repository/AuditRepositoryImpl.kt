package com.example.proyectwin.data.repository

import com.example.proyectwin.data.api.safeApiCall
import com.example.proyectwin.data.api.service.AuditApi
import com.example.proyectwin.data.mapper.toDomain
import com.example.proyectwin.data.model.AuditLog
import com.example.proyectwin.data.model.PaginaAuditoria
import com.example.proyectwin.domain.repository.AuditRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuditRepositoryImpl @Inject constructor(
    private val api: AuditApi,
) : AuditRepository {

    override suspend fun listar(
        accion: String?,
        entidad: String?,
        idUsuario: Int?,
        desde: String?,
        hasta: String?,
    ): Result<List<AuditLog>> =
        safeApiCall { api.listar(accion, entidad, idUsuario, desde, hasta, INCLUDE) }
            .map { lista -> lista.map { it.toDomain() } }

    override suspend fun listarPaginado(
        page: Int,
        porPagina: Int,
        buscar: String?,
        accion: String?,
        entidad: String?,
        idUsuario: Int?,
        rol: String?,
        desde: String?,
        hasta: String?,
        orden: String?,
    ): Result<PaginaAuditoria> = safeApiCall {
        api.listarPaginado(
            page = page,
            porPagina = porPagina,
            buscar = buscar,
            accion = accion,
            entidad = entidad,
            idUsuario = idUsuario,
            rol = rol,
            desde = desde,
            hasta = hasta,
            orden = orden,
            included = INCLUDE,
        )
    }.map { dto ->
        PaginaAuditoria(
            registros = dto.data.map { it.toDomain() },
            pagina = dto.currentPage,
            totalPaginas = dto.lastPage,
            totalRegistros = dto.total,
        )
    }

    companion object {
        private const val INCLUDE = "user"
    }
}
