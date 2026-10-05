package com.example.proyectwin.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Desglose del motor de similitud (columna json `detalles`). */
@Serializable
data class DetallesSimilitud(
    val palabras: Int = 0,
    val caracteres: Int = 0,
    val tema: String? = null,
    val cobertura: Int = 0,
    val terminos: List<String> = emptyList(),
    val pasajes: List<PasajeSimilitud> = emptyList(),
)

@Serializable
data class PasajeSimilitud(
    val a: String = "",
    val b: String = "",
    val score: Double = 0.0,
)

/** Par de propuestas comparadas por el motor (`similarities`). */
@Serializable
data class SimilarityDto(
    val id: Int = 0,
    val porcentaje: Double = 0.0,
    val detalles: DetallesSimilitud? = null,
    val fecha: String? = null,
    @SerialName("id_proyecto_1") val idProyecto1: Int = 0,
    @SerialName("id_proyecto_2") val idProyecto2: Int = 0,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    val project1: ProjectDto? = null,
    val project2: ProjectDto? = null,
)

@Serializable
data class DetectRequest(@SerialName("id_proyecto") val idProyecto: Int)

@Serializable
data class DetectResponse(val detectadas: Int = 0)

@Serializable
data class RecalculateResponse(
    val eliminadas: Int = 0,
    val creadas: Int = 0,
)
