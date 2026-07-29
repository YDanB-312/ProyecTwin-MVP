package com.example.proyectwin.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ProjectResponse(
    val id: Int,
    val title: String,
    val description: String? = null,
    val status: String,
    val student_id: Int? = null,
    val instructor_id: Int? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)
