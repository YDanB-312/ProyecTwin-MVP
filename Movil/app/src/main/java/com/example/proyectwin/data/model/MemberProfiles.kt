package com.example.proyectwin.data.model

/** Perfil de instructor (`instructors`) con su usuario resuelto. */
data class InstructorProfile(
    val id: Int,
    val idUsuario: Int,
    val fechaIngreso: String? = null,
    val nombre: String = "",
    val correo: String = "",
    val fotoUrl: String? = null,
)

/** Perfil de aprendiz (`apprentices`) con su usuario resuelto. */
data class ApprenticeProfile(
    val id: Int,
    val codigo: String = "",
    val idUsuario: Int,
    val idClassGroup: Int? = null,
    val idPrograma: Int? = null,
    val nombre: String = "",
    val correo: String = "",
    val fotoUrl: String? = null,
)
