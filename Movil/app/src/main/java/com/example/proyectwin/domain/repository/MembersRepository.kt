package com.example.proyectwin.domain.repository

import com.example.proyectwin.data.model.ApprenticeProfile
import com.example.proyectwin.data.model.InstructorProfile

/** Perfiles de instructor/aprendiz (`instructors`, `apprentices`). */
interface MembersRepository {

    suspend fun instructores(): Result<List<InstructorProfile>>

    suspend fun instructorPorUsuario(idUsuario: Int): Result<InstructorProfile?>

    suspend fun aprendizPorUsuario(idUsuario: Int): Result<ApprenticeProfile?>

    /**
     * Aprendices visibles para el usuario (ficha/alcance resuelto por el
     * backend, igual que React). Endpoint accesible a aprendiz/instructor/admin;
     * `/general-users` NO sirve: es solo de admin.
     */
    suspend fun listarAprendices(): Result<List<ApprenticeProfile>>

    suspend fun crearAprendiz(
        codigo: String,
        idUsuario: Int,
        idClassGroup: Int?,
        idPrograma: Int? = null,
    ): Result<ApprenticeProfile>

    suspend fun actualizarAprendiz(
        id: Int,
        codigo: String,
        idUsuario: Int,
        idClassGroup: Int?,
        idPrograma: Int? = null,
    ): Result<ApprenticeProfile>

    suspend fun eliminarAprendiz(id: Int): Result<Unit>
}
