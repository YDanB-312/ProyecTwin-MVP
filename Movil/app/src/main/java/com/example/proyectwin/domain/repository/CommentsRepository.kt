package com.example.proyectwin.domain.repository

import com.example.proyectwin.data.model.Comment

/** Observaciones sobre propuestas, con hilos de respuesta (`comments`). */
interface CommentsRepository {

    suspend fun listar(idProyecto: Int? = null): Result<List<Comment>>

    suspend fun obtener(id: Int): Result<Comment>

    suspend fun crear(idProyecto: Int, texto: String, respuestaA: Int? = null): Result<Comment>

    suspend fun actualizar(id: Int, idProyecto: Int, texto: String, respuestaA: Int?): Result<Comment>

    suspend fun eliminar(id: Int): Result<Unit>
}
