package com.example.proyectwin.domain.usecase

import com.example.proyectwin.data.mapper.toDraft
import com.example.proyectwin.data.model.ClassGroupStatus
import com.example.proyectwin.data.model.Project
import com.example.proyectwin.data.model.ProjectDraft
import com.example.proyectwin.domain.repository.FichasRepository
import com.example.proyectwin.domain.repository.ProjectsRepository
import com.example.proyectwin.domain.repository.UsersRepository
import javax.inject.Inject

/**
 * Crea una propuesta y su equipo en un flujo atómico de UI, igual que el
 * frontend: POST /projects y luego POST /apprentice-projects por cada
 * integrante. Si un miembro falla se reporta el error (la propuesta ya existe).
 */
class CreateProjectWithTeamUseCase @Inject constructor(
    private val projects: ProjectsRepository,
    private val users: UsersRepository,
    private val fichas: FichasRepository,
) {

    suspend operator fun invoke(draft: ProjectDraft, miembros: List<Int> = emptyList()): Result<Project> {
        var preparado = draft

        // Sin ficha destino (caso aprendiz): toma la suya y valida que esté activa.
        if (preparado.idClassGroup == null) {
            val cuenta = users.miCuenta().getOrElse { return Result.failure(it) }
            val fichaId = cuenta.fichaId
                ?: return Result.failure(
                    IllegalStateException("Únete a una ficha con tu código antes de crear una propuesta."),
                )
            val ficha = fichas.obtener(fichaId).getOrElse { return Result.failure(it) }
            if (ficha.estado != ClassGroupStatus.ACTIVO.value) {
                return Result.failure(
                    IllegalStateException("Tu ficha no está activa para recibir propuestas."),
                )
            }
            preparado = preparado.copy(
                idClassGroup = fichaId,
                idInstructorAsignado = ficha.instructorId,
            )
        }

        if (preparado.idCreador == 0) {
            val cuenta = users.miCuenta().getOrElse { return Result.failure(it) }
            preparado = preparado.copy(idCreador = cuenta.id)
        }

        val proyecto = projects.crear(preparado).getOrElse { return Result.failure(it) }

        val resultados = miembros.map { idAprendiz ->
            projects.agregarAlEquipo(proyecto.id, idAprendiz)
        }
        val fallo = resultados.firstOrNull { it.isFailure }
        if (fallo != null) {
            return Result.failure(
                fallo.exceptionOrNull() ?: IllegalStateException("No se pudo completar el equipo de la propuesta."),
            )
        }
        return Result.success(proyecto)
    }
}
