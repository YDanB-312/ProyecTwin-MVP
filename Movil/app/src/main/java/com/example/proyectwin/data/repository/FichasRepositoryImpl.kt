package com.example.proyectwin.data.repository

import com.example.proyectwin.data.api.dto.ClassGroupCreateRequest
import com.example.proyectwin.data.api.dto.ClassGroupUpdateRequest
import com.example.proyectwin.data.api.dto.JoinFichaRequest
import com.example.proyectwin.data.api.safeApiCall
import com.example.proyectwin.data.api.service.ApprenticesApi
import com.example.proyectwin.data.api.service.ClassGroupsApi
import com.example.proyectwin.data.local.SessionManager
import com.example.proyectwin.data.mapper.toDomain
import com.example.proyectwin.data.model.Ficha
import com.example.proyectwin.domain.repository.FichasRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FichasRepositoryImpl @Inject constructor(
    private val api: ClassGroupsApi,
    private val apprenticesApi: ApprenticesApi,
    private val sessionManager: SessionManager,
) : FichasRepository {

    override suspend fun listar(): Result<List<Ficha>> =
        safeApiCall { api.listar(INCLUDE_LISTA) }.map { lista -> lista.map { it.toDomain() } }

    override suspend fun obtener(id: Int): Result<Ficha> =
        safeApiCall { api.obtener(id, INCLUDE_DETALLE) }.map { it.toDomain() }

    override suspend fun crear(
        codigo: String,
        nombre: String,
        numero: String?,
        estado: String,
        idPrograma: Int,
        idInstructor: Int,
    ): Result<Ficha> = safeApiCall {
        api.crear(
            ClassGroupCreateRequest(
                codigo = codigo,
                numero = numero,
                nombre = nombre,
                estado = estado,
                idPrograma = idPrograma,
                idInstructor = idInstructor,
            ),
        )
    }.map { it.toDomain() }

    override suspend fun actualizar(
        id: Int,
        codigo: String,
        nombre: String,
        numero: String?,
        estado: String,
        idPrograma: Int,
        idInstructor: Int,
    ): Result<Ficha> = safeApiCall {
        api.actualizar(
            id,
            ClassGroupUpdateRequest(
                codigo = codigo,
                numero = numero,
                nombre = nombre,
                estado = estado,
                idPrograma = idPrograma,
                idInstructor = idInstructor,
            ),
        )
    }.map { it.toDomain() }

    override suspend fun eliminar(id: Int): Result<Unit> =
        safeApiCall { api.eliminar(id) }.map {}

    override suspend fun fichaPorCodigo(codigo: String): Result<Ficha> =
        safeApiCall { apprenticesApi.fichaPorCodigo(normalizar(codigo)) }.map { it.toDomain() }

    override suspend fun unirmeAFicha(codigo: String): Result<Ficha> {
        val codigoNorm = normalizar(codigo)
        val aprendiz = safeApiCall { apprenticesApi.unirmeAFicha(JoinFichaRequest(codigoNorm)) }
            .getOrElse { return Result.failure(it) }
        val fichaId = aprendiz.idClassGroup
            ?: return Result.failure(IllegalStateException("La ficha no indicó su identificador."))
        sessionManager.joinFicha(fichaId)

        // Devuelve la ficha completa; si la lectura posterior falla, al menos
        // queda la mínima con el id para que la UI continue.
        val ficha: Ficha = safeApiCall { apprenticesApi.fichaPorCodigo(codigoNorm) }
            .map { it.toDomain() }
            .getOrElse { Ficha(id = fichaId, codigo = codigoNorm, programa = "") }
        return Result.success(ficha)
    }

    override suspend fun salirDeFicha(): Result<Unit> =
        safeApiCall { apprenticesApi.salirDeFicha() }.map { sessionManager.clearFicha() }

    private fun normalizar(codigo: String): String = codigo.trim().lowercase()

    companion object {
        private const val INCLUDE_LISTA = "program,instructor.generalUser"
        private const val INCLUDE_DETALLE = "program,instructor.generalUser,apprentices.generalUser"
    }
}
