package com.example.proyectwin.data.model

import kotlinx.serialization.Serializable

/** Red de conocimiento institucional (`knowledge-networks`). */
@Serializable
data class KnowledgeNetwork(
    val id: Int,
    val nombre: String
)

/** Programa de formación (`training-programs`). */
@Serializable
data class TrainingProgram(
    val id: Int,
    val nombre: String,
    val nivel: String? = null,
    val redId: Int? = null,
    val redNombre: String? = null
)
