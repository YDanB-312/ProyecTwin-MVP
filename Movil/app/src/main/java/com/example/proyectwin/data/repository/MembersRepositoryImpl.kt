package com.example.proyectwin.data.repository

import com.example.proyectwin.data.api.dto.ApprenticeCreateRequest
import com.example.proyectwin.data.api.dto.ApprenticeUpdateRequest
import com.example.proyectwin.data.api.safeApiCall
import com.example.proyectwin.data.api.service.ApprenticesApi
import com.example.proyectwin.data.api.service.InstructorsApi
import com.example.proyectwin.data.mapper.toProfile
import com.example.proyectwin.data.model.ApprenticeProfile
import com.example.proyectwin.data.model.InstructorProfile
import com.example.proyectwin.domain.repository.MembersRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MembersRepositoryImpl @Inject constructor(
    private val instructorsApi: InstructorsApi,
    private val apprenticesApi: ApprenticesApi,
) : MembersRepository {

    override suspend fun instructores(): Result<List<InstructorProfile>> =
        safeApiCall { instructorsApi.listar("generalUser") }
            .map { lista -> lista.map { it.toProfile() } }

    override suspend fun instructorPorUsuario(idUsuario: Int): Result<InstructorProfile?> =
        instructores().map { lista -> lista.firstOrNull { it.idUsuario == idUsuario } }

    override suspend fun aprendizPorUsuario(idUsuario: Int): Result<ApprenticeProfile?> =
        listarAprendices().map { lista -> lista.firstOrNull { it.idUsuario == idUsuario } }

    override suspend fun listarAprendices(): Result<List<ApprenticeProfile>> =
        safeApiCall { apprenticesApi.listar("generalUser,classGroup") }
            .map { lista -> lista.map { it.toProfile() } }

    override suspend fun crearAprendiz(
        codigo: String,
        idUsuario: Int,
        idClassGroup: Int?,
        idPrograma: Int?,
    ): Result<ApprenticeProfile> = safeApiCall {
        apprenticesApi.crear(
            ApprenticeCreateRequest(
                codigo = codigo,
                idUsuario = idUsuario,
                idClassGroup = idClassGroup,
                idPrograma = idPrograma,
            ),
        )
    }.map { it.toProfile() }

    override suspend fun actualizarAprendiz(
        id: Int,
        codigo: String,
        idUsuario: Int,
        idClassGroup: Int?,
        idPrograma: Int?,
    ): Result<ApprenticeProfile> = safeApiCall {
        apprenticesApi.actualizar(
            id,
            ApprenticeUpdateRequest(
                codigo = codigo,
                idUsuario = idUsuario,
                idClassGroup = idClassGroup,
                idPrograma = idPrograma,
            ),
        )
    }.map { it.toProfile() }

    override suspend fun eliminarAprendiz(id: Int): Result<Unit> =
        safeApiCall { apprenticesApi.eliminar(id) }.map {}
}
