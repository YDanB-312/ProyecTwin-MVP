package com.example.proyectwin.data.repository

import com.example.proyectwin.data.api.safeApiCall
import com.example.proyectwin.data.api.service.AuditApi
import com.example.proyectwin.data.mapper.toDomain
import com.example.proyectwin.data.model.AuditLog
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

    companion object {
        private const val INCLUDE = "user"
    }
}
