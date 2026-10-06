package com.example.proyectwin.domain.usecase

import com.example.proyectwin.data.model.ClassGroupStatus
import com.example.proyectwin.data.model.Project
import com.example.proyectwin.data.model.ProjectDraft
import com.example.proyectwin.domain.repository.FichasRepository
import com.example.proyectwin.domain.repository.ProjectsRepository
import com.example.proyectwin.domain.repository.UsersRepository
import javax.inject.Inject

/** Propuesta creada + integrantes que no se pudieron agregar (para reintentar). */
data class PropuestaCreada(
    val proyecto: Project,
    val miembrosFallidos: List<Int> = emptyList(),
)

/**
 * Crea una propuesta y su equipo, igual que el frontend: POST /projects y luego
 * POST /apprentice-projects por cada integrante. Si un miembro falla, la
 * propuesta ya existe: se devuelve con la lista de fallidos para que la UI
 * avise y el usuario los agregue desde el detalle (no se duplica el borrador).
 */
class CreateProjectWithTeamUseCase @Inject constructor(
    private val projects: ProjectsRepository,
    private val users: UsersRepository,
    private val fichas: FichasRepository,
) {

    suspend operator fun invoke(draft: ProjectDraft, miembros: List<Int> = emptyList()): Result<PropuestaCreada> {
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

        val fallidos = miembros.filter { idAprendiz ->
            projects.agregarAlEquipo(proyecto.id, idAprendiz).isFailure
        }
        return Result.success(PropuestaCreada(proyecto = proyecto, miembrosFallidos = fallidos))
    }
}
