package com.example.proyectwin.data.repository

import com.example.proyectwin.data.api.dto.DemoSimilitudRequest
import com.example.proyectwin.data.api.dto.MotorConfigUpdateRequest
import com.example.proyectwin.data.api.safeApiCall
import com.example.proyectwin.data.api.service.MotorApi
import com.example.proyectwin.data.mapper.toDomain
import com.example.proyectwin.data.model.DemoResultado
import com.example.proyectwin.data.model.MotorConfig
import com.example.proyectwin.data.model.ResumenPublico
import com.example.proyectwin.domain.repository.MotorRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MotorRepositoryImpl @Inject constructor(
    private val api: MotorApi,
) : MotorRepository {

    override suspend fun obtenerConfig(): Result<MotorConfig> =
        safeApiCall { api.obtenerConfig() }.map { it.toDomain() }

    override suspend fun actualizarConfig(umbral: Double, meses: Int): Result<MotorConfig> =
        safeApiCall { api.actualizarConfig(MotorConfigUpdateRequest(umbral = umbral, meses = meses)) }
            .map { it.toDomain() }

    override suspend fun resumen(): Result<ResumenPublico> =
        safeApiCall { api.resumen() }.map { it.toDomain() }

    override suspend fun demo(texto: String): Result<DemoResultado> =
        safeApiCall { api.demo(DemoSimilitudRequest(texto)) }.map { it.toDomain() }
}
