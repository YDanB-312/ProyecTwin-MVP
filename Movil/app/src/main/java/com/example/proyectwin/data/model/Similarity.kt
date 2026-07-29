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
    val createdAt: String? = null
) {
    val simStatus: SimilarityStatus get() = SimilarityStatus.fromValue(estado)
    val statusDisplay: String get() = when (simStatus) {
        SimilarityStatus.PENDIENTE -> "Pendiente"
        SimilarityStatus.CONFIRMADO -> "Confirmado"
        SimilarityStatus.RECHAZADO -> "Rechazado"
    }
    val similitudPercent: String get() = "%.1f%%".format(java.util.Locale.US, similitud * 100)
}
