package com.example.proyectwin.data.repository

import com.example.proyectwin.data.api.dto.TeamMemberRequest
import com.example.proyectwin.data.api.safeApiCall
import com.example.proyectwin.data.api.service.ProjectsApi
import com.example.proyectwin.data.local.SessionManager
import com.example.proyectwin.data.mapper.toCreateRequest
import com.example.proyectwin.data.mapper.toDomain
import com.example.proyectwin.data.mapper.toTeamMember
import com.example.proyectwin.data.mapper.toUpdateRequest
import com.example.proyectwin.data.model.Project
import com.example.proyectwin.data.model.ProjectDraft
import com.example.proyectwin.data.model.ProjectHistory
import com.example.proyectwin.data.model.TeamMember
import com.example.proyectwin.domain.repository.ProjectsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectsRepositoryImpl @Inject constructor(
    private val api: ProjectsApi,
    private val sessionManager: SessionManager,
) : ProjectsRepository {

    override suspend fun listar(
        search: String?,
        estado: String?,
        fichaId: Int?,
        programa: String?,
    ): Result<List<Project>> =
        safeApiCall { api.listar(search, estado, fichaId, programa, INCLUDE_PROYECTOS) }
            .map { lista -> lista.map { it.toDomain() } }

    override suspend fun obtener(id: Int): Result<Project> =
        safeApiCall { api.obtener(id, INCLUDE_PROYECTOS) }.map { it.toDomain() }

    override suspend fun crear(draft: ProjectDraft): Result<Project> =
        safeApiCall { api.crear(draft.toCreateRequest()) }.map { it.toDomain() }

    override suspend fun actualizar(id: Int, draft: ProjectDraft): Result<Project> =
        safeApiCall { api.actualizar(id, draft.toUpdateRequest()) }.map { it.toDomain() }

    override suspend fun enviar(id: Int): Result<Project> =
        safeApiCall { api.enviar(id) }.map { it.toDomain() }

    override suspend fun historial(id: Int): Result<List<ProjectHistory>> =
        safeApiCall { api.historial(id, "user") }.map { lista -> lista.map { it.toDomain() } }

    override suspend fun eliminar(id: Int): Result<Unit> =
        safeApiCall { api.eliminar(id) }.map {}

    override suspend fun equipo(idProyecto: Int): Result<List<TeamMember>> =
        safeApiCall { api.listarEquipo("apprentice.generalUser") }
            .map { lista ->
                lista.filter { it.idProyecto == idProyecto }
                    .mapNotNull { it.toTeamMember() }
            }

    override suspend fun agregarAlEquipo(idProyecto: Int, idAprendiz: Int): Result<Unit> =
        safeApiCall {
            api.agregarAlEquipo(
                TeamMemberRequest(
                    idAprendiz = idAprendiz,
                    idProyecto = idProyecto
                )
            )
        }.map {}

    override suspend fun quitarDelEquipo(idPivote: Int): Result<Unit> =
        safeApiCall { api.quitarDelEquipo(idPivote) }.map {}

    companion object {
        /** Mismos includes que `INCLUDE_PROYECTOS` del frontend. */
        const val INCLUDE_PROYECTOS =
            "creator,instructor.generalUser,classGroup.program,apprentices.generalUser"
    }
}
