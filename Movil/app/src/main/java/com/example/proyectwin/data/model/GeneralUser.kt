package com.example.proyectwin.data.model

import kotlinx.serialization.Serializable

@Serializable
data class GeneralUser(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val token: String? = null,
    val fotoPerfil: String? = null,
    val telefono: String? = null,
    val fichaId: Int? = null,
    val documentoIdentidad: String? = null
) {
    val userRole: UserRole get() = UserRole.fromValue(role)
    val roleDisplayName: String get() = when (userRole) {
        UserRole.INSTRUCTOR -> "Instructor"
        UserRole.APRENDIZ -> "Aprendiz"
        UserRole.ADMINISTRADOR -> "Administrador"
    }
    val initials: String get() =
        name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
}
