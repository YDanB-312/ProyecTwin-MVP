package com.example.proyectwin.data.api

import com.example.proyectwin.data.model.GeneralUser
import com.example.proyectwin.data.model.ProjectResponse
import retrofit2.http.*

interface ApiService {
    @GET("projects")
    suspend fun getProjects(): List<ProjectResponse>

    @GET("projects/{id}")
    suspend fun getProject(@Path("id") id: Int): ProjectResponse

    @POST("login")
    suspend fun login(@Body credentials: Map<String, String>): GeneralUser
}
