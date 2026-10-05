package com.example.proyectwin.domain.repository

import com.example.proyectwin.data.model.GeneralUser
import com.example.proyectwin.data.model.Project
import com.example.proyectwin.data.model.ProjectDraft

/** Propuestas de proyecto y su equipo (pivote `apprentice_projects`). */
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

    suspend fun eliminar(id: Int): Result<Unit>

    /** Integrantes de la propuesta (aprendiz + usuario resuelto). */
    suspend fun equipo(idProyecto: Int): Result<List<GeneralUser>>

    suspend fun agregarAlEquipo(idProyecto: Int, idAprendiz: Int): Result<Unit>

    suspend fun quitarDelEquipo(idProyecto: Int, idAprendiz: Int): Result<Unit>
}
