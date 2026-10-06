package com.example.proyectwin.domain.repository

import com.example.proyectwin.data.model.RecalculoSimilitudes
import com.example.proyectwin.data.model.Similarity

/** Pares del motor de originalidad (`similarities`). */
interface SimilaritiesRepository {

    suspend fun listar(
        proyectoId: Int? = null,
        /** `true` = evidencia histórica del proyecto (`vigente=false`). */
        historial: Boolean? = null,
        relatedTo: String? = null,
        search: String? = null,
        fichaId: Int? = null,
        programa: String? = null,
    ): Result<List<Similarity>>

    suspend fun obtener(id: Int): Result<Similarity>

    /** Recalculación global (solo admin). */
    suspend fun recalcular(): Result<RecalculoSimilitudes>

    suspend fun eliminar(id: Int): Result<Unit>
}
