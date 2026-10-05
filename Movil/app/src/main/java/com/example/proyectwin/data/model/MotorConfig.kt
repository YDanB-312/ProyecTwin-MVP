package com.example.proyectwin.data.model

import kotlinx.serialization.Serializable

/** Configuración global del motor de similitud (`config-similitud`). */
@Serializable
data class MotorConfig(
    val id: Int = 0,
    val umbral: Double = 0.0,
    val meses: Int = 0
)

/** Resumen público agregado (`GET /public/resumen`). */
@Serializable
data class ResumenPublico(
    val umbral: Double = 0.0,
    val meses: Int = 0,
    val totalPropuestas: Int = 0,
    val totalProgramas: Int = 0
)

/** Resultado de la demo pública del motor (`POST /public/demo-similitud`). */
@Serializable
data class DemoResultado(
    val umbral: Double = 0.0,
    val meses: Int = 0,
    val total: Int = 0,
    val sobre: Int = 0,
    val coincidencias: List<DemoCoincidencia> = emptyList()
)

@Serializable
data class DemoCoincidencia(
    val id: Int = 0,
    val titulo: String = "",
    val porcentaje: Double = 0.0
)
