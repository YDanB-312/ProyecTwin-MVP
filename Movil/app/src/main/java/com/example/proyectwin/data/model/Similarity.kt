package com.example.proyectwin.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Similarity(
    val id: Int,
    val projectId1: Int,
    val projectId2: Int,
    val project1Title: String? = null,
    val project2Title: String? = null,
    val project1Student: String? = null,
    val project2Student: String? = null,
    val similitud: Double = 0.0,
    val estado: String = SimilarityStatus.PENDIENTE.value,
    val createdAt: String? = null,
    val fecha: String? = null,
    val detalles: DetalleSimilitud? = null
) {
    val simStatus: SimilarityStatus get() = SimilarityStatus.fromValue(estado)
    val statusDisplay: String get() = when (simStatus) {
        SimilarityStatus.PENDIENTE -> "Pendiente"
        SimilarityStatus.REVISADO -> "Revisado"
        SimilarityStatus.CONFIRMADO -> "Confirmado"
        SimilarityStatus.RECHAZADO -> "Rechazado"
    }
    val similitudPercent: String get() = "%.1f%%".format(java.util.Locale.US, similitud * 100)
}

/** Desglose del motor de similitud (columna json `detalles`). */
@Serializable
data class DetalleSimilitud(
    val palabras: Int = 0,
    val caracteres: Int = 0,
    val tema: String? = null,
    val cobertura: Int = 0,
    val terminos: List<String> = emptyList(),
    val pasajes: List<PasajeSimilitud> = emptyList()
)

@Serializable
data class PasajeSimilitud(
    val a: String = "",
    val b: String = "",
    val score: Double = 0.0
)

/** Resultado de `POST /similarities/recalculate` (admin). */
@Serializable
data class RecalculoSimilitudes(
    val eliminadas: Int = 0,
    val creadas: Int = 0
)
