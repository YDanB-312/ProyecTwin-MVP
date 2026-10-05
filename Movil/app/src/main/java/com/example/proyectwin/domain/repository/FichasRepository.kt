package com.example.proyectwin.domain.repository

import com.example.proyectwin.data.model.Ficha

/** Fichas de formación (`class-groups`) y membresía personal del aprendiz. */
interface FichasRepository {

    suspend fun listar(): Result<List<Ficha>>

    suspend fun obtener(id: Int): Result<Ficha>

    suspend fun crear(
        codigo: String,
        nombre: String,
        numero: String?,
        estado: String,
        idPrograma: Int,
        idInstructor: Int,
    ): Result<Ficha>

    suspend fun actualizar(
        id: Int,
        codigo: String,
        nombre: String,
        numero: String?,
        estado: String,
        idPrograma: Int,
        idInstructor: Int,
    ): Result<Ficha>

    suspend fun eliminar(id: Int): Result<Unit>

    /** Previsualiza una ficha por su código (flujo "unirme con código"). */
    suspend fun fichaPorCodigo(codigo: String): Result<Ficha>

    /** Se une a la ficha del código y refresca la sesión local. */
    suspend fun unirmeAFicha(codigo: String): Result<Ficha>

    /** Sale de la ficha actual y limpia la sesión local. */
    suspend fun salirDeFicha(): Result<Unit>
}
