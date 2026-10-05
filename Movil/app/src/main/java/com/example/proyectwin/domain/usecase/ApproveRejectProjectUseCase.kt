package com.example.proyectwin.domain.usecase

import com.example.proyectwin.data.mapper.toDraft
import com.example.proyectwin.data.model.Project
import com.example.proyectwin.data.model.ProjectStatus
import com.example.proyectwin.domain.repository.ProjectsRepository
import javax.inject.Inject

/** Aprueba o rechaza una propuesta (PUT /projects con el nuevo estado). */
class ApproveRejectProjectUseCase @Inject constructor(
    private val projects: ProjectsRepository,
) {

    suspend operator fun invoke(projectId: Int, aprobar: Boolean): Result<Project> {
        val proyecto = projects.obtener(projectId).getOrElse { return Result.failure(it) }
        val nuevoEstado = if (aprobar) ProjectStatus.APROBADO.value else ProjectStatus.RECHAZADO.value
        return projects.actualizar(projectId, proyecto.toDraft().copy(estado = nuevoEstado))
    }
}
