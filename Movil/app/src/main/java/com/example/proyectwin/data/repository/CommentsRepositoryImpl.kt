package com.example.proyectwin.data.repository

import com.example.proyectwin.data.api.dto.CommentCreateRequest
import com.example.proyectwin.data.api.dto.CommentUpdateRequest
import com.example.proyectwin.data.api.safeApiCall
import com.example.proyectwin.data.api.service.CommentsApi
import com.example.proyectwin.data.mapper.toDomain
import com.example.proyectwin.data.model.Comment
import com.example.proyectwin.domain.repository.CommentsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentsRepositoryImpl @Inject constructor(
    private val api: CommentsApi,
) : CommentsRepository {

    override suspend fun listar(idProyecto: Int?): Result<List<Comment>> =
        safeApiCall { api.listar(idProyecto, INCLUDE) }.map { lista -> lista.map { it.toDomain() } }

    override suspend fun obtener(id: Int): Result<Comment> =
        safeApiCall { api.obtener(id) }.map { it.toDomain() }

    override suspend fun crear(idProyecto: Int, texto: String, respuestaA: Int?): Result<Comment> =
        safeApiCall {
            api.crear(CommentCreateRequest(texto = texto, idProyecto = idProyecto, respuestaA = respuestaA))
        }.map { it.toDomain() }

    override suspend fun actualizar(
        id: Int,
        idProyecto: Int,
        texto: String,
        respuestaA: Int?,
    ): Result<Comment> = safeApiCall {
        api.actualizar(
            id,
            CommentUpdateRequest(texto = texto, idProyecto = idProyecto, respuestaA = respuestaA),
        )
    }.map { it.toDomain() }

    override suspend fun eliminar(id: Int): Result<Unit> =
        safeApiCall { api.eliminar(id) }.map {}

    companion object {
        private const val INCLUDE = "user"
    }
}
