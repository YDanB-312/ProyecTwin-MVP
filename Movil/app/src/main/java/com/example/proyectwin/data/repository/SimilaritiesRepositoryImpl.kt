package com.example.proyectwin.data.repository

import com.example.proyectwin.data.api.dto.DetectRequest
import com.example.proyectwin.data.api.safeApiCall
import com.example.proyectwin.data.api.service.SimilaritiesApi
import com.example.proyectwin.data.mapper.toDomain
import com.example.proyectwin.data.model.RecalculoSimilitudes
import com.example.proyectwin.data.model.Similarity
import com.example.proyectwin.domain.repository.SimilaritiesRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SimilaritiesRepositoryImpl @Inject constructor(
    private val api: SimilaritiesApi,
) : SimilaritiesRepository {

    override suspend fun listar(
        proyectoId: Int?,
        relatedTo: String?,
        search: String?,
        fichaId: Int?,
        programa: String?,
    ): Result<List<Similarity>> =
        safeApiCall { api.listar(proyectoId, relatedTo, search, fichaId, programa, INCLUDE) }
            .map { lista -> lista.map { it.toDomain() } }

    override suspend fun obtener(id: Int): Result<Similarity> =
        safeApiCall { api.obtener(id, INCLUDE) }.map { it.toDomain() }

    override suspend fun detectar(idProyecto: Int): Result<Int> =
        safeApiCall { api.detectar(DetectRequest(idProyecto)) }.map { it.detectadas }

    override suspend fun recalcular(): Result<RecalculoSimilitudes> =
        safeApiCall { api.recalcular(emptyMap()) }
            .map { RecalculoSimilitudes(eliminadas = it.eliminadas, creadas = it.creadas) }

    override suspend fun eliminar(id: Int): Result<Unit> =
        safeApiCall { api.eliminar(id) }.map {}

    companion object {
        /** Mismas relaciones que resuelve el frontend para título y autoría. */
        private const val INCLUDE =
            "project1.classGroup.program,project2.classGroup.program," +
                "project1.creator,project2.creator," +
                "project1.apprentices.generalUser,project2.apprentices.generalUser"
    }
}
