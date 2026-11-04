package com.example.proyectwin.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * GET /dashboard-metrics?periodo=3m|30d|7d (solo autenticado).
 * El backend decide el rol con el token; el cliente no lo envía.
 */
@Serializable
data class DashboardMetricsDto(
    val rol: String = "",
    val periodo: String = "",
    val usuarios: UsuariosMetricasDto? = null,
    val fichas: FichasMetricasDto? = null,
    val aprendices: ConteoMetricaDto? = null,
    val propuestas: PropuestasMetricasDto? = null,
    val similitudesPropuestas: List<SimilitudPropuestaDto> = emptyList(),
    val evolucion: List<EvolucionMetricaDto> = emptyList(),
    val actividad: List<ActividadMetricaDto> = emptyList(),
)

@Serializable
data class FichasMetricasDto(
    val total: Int = 0,
    val detalle: List<FichaMetricaDto> = emptyList(),
)

@Serializable
data class FichaMetricaDto(
    val id: Int = 0,
    val codigo: String = "",
    val nombre: String = "",
    val aprendices: Int = 0,
    val propuestas: Int = 0,
    val borradores: Int = 0,
    val pendientes: Int = 0,
    val aprobadas: Int = 0,
    val rechazadas: Int = 0,
    val revisables: Int = 0,
    val revisadas: Int = 0,
    val cobertura: Int? = null,
)

@Serializable
data class SimilitudPropuestaDto(
    val id: Int = 0,
    val titulo: String = "",
    val max: Int = 0,
    val count: Int = 0,
)

@Serializable
data class UsuariosMetricasDto(
    val total: Int = 0,
    val aprendices: Int = 0,
    val instructores: Int = 0,
    val admins: Int = 0,
)

@Serializable
data class ConteoMetricaDto(val total: Int = 0)

@Serializable
data class PropuestasMetricasDto(
    val total: Int = 0,
    val borrador: Int = 0,
    val pendiente: Int = 0,
    val aprobado: Int = 0,
    val rechazado: Int = 0,
)

@Serializable
data class EvolucionMetricaDto(
    val clave: String = "",
    val label: String = "",
    val fecha: String = "",
    val total: Int = 0,
    val propuestas: List<String> = emptyList(),
    val restantes: Int = 0,
)

@Serializable
data class ActividadMetricaDto(
    val id: Int = 0,
    val titulo: String = "",
    val meta: String = "",
    val usuario: String = "",
    @SerialName("proyecto_id") val proyectoId: Int? = null,
    val fecha: String? = null,
)
