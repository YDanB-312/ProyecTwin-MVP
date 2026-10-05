package com.example.proyectwin.domain.usecase

import com.example.proyectwin.domain.repository.SimilaritiesRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * Ejecuta el motor de similitud sobre una propuesta con reintentos
 * (como el análisis automático tras crear una propuesta en el frontend).
 */
class DetectSimilarityWithRetryUseCase @Inject constructor(
    private val similarities: SimilaritiesRepository,
) {

    suspend operator fun invoke(idProyecto: Int, intentos: Int = 3): Result<Int> {
        var ultimo: Result<Int> = Result.failure(IllegalStateException("No se pudo ejecutar el análisis."))
        repeat(intentos.coerceAtLeast(1)) { indice ->
            if (indice > 0) delay(500L * indice)
            ultimo = similarities.detectar(idProyecto)
            if (ultimo.isSuccess) return ultimo
        }
        return ultimo
    }
}
