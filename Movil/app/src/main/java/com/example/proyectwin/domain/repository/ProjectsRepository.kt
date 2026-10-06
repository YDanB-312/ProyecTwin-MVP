package com.example.proyectwin.domain.repository

import com.example.proyectwin.data.model.Project
import com.example.proyectwin.data.model.ProjectDraft
import com.example.proyectwin.data.model.ProjectHistory
import com.example.proyectwin.data.model.TeamMember

/** Propuestas de proyecto, su equipo (pivote `apprentice_projects`) e historial. */
interface ProjectsRepository {

    suspend fun listar(
        search: String? = null,
        estado: String? = null,
        fichaId: Int? = null,
        programa: String? = null,
    ): Result<List<Project>>

    suspend fun obtener(id: Int): Result<Project>

    suspend fun crear(draft: ProjectDraft): Result<Project>

    suspend fun actualizar(id: Int, draft: ProjectDraft): Result<Project>

    /** Envío/reenvío explícito; el backend valida y ejecuta el motor. */
    suspend fun enviar(id: Int): Result<Project>

    suspend fun historial(id: Int): Result<List<ProjectHistory>>

    suspend fun eliminar(id: Int): Result<Unit>

    /** Integrantes de la propuesta con el id de su fila pivote. */
    suspend fun equipo(idProyecto: Int): Result<List<TeamMember>>

    suspend fun agregarAlEquipo(idProyecto: Int, idAprendiz: Int): Result<Unit>

    /** Retira al integrante; [idPivote] es el id de `apprentice-projects`. */
    suspend fun quitarDelEquipo(idPivote: Int): Result<Unit>
}
