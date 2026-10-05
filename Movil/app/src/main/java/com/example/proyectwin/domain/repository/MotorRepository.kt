package com.example.proyectwin.domain.repository

import com.example.proyectwin.data.model.DemoResultado
import com.example.proyectwin.data.model.MotorConfig
import com.example.proyectwin.data.model.ResumenPublico

/** Motor de similitud: configuración global y endpoints públicos de demo. */
interface MotorRepository {

    suspend fun obtenerConfig(): Result<MotorConfig>

    suspend fun actualizarConfig(umbral: Double, meses: Int): Result<MotorConfig>

    suspend fun resumen(): Result<ResumenPublico>

    suspend fun demo(texto: String): Result<DemoResultado>
}
