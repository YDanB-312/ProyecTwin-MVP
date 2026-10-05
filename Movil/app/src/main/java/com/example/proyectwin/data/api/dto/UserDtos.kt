package com.example.proyectwin.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Cuenta de usuario tal como la devuelve Laravel (`general_users`).
 * Relaciones incluidas con `?included=` (aprendice/instructor) según aplique.
 */
@Serializable
data class GeneralUserDto(
    val id: Int = 0,
    val nombre: String = "",
    val apellido: String = "",
    val correo: String = "",
    val username: String = "",
    @SerialName("must_change_password") val mustChangePassword: Boolean = false,
    @SerialName("foto_url") val fotoUrl: String? = null,
    val rol: String = "aprendiz",
    val estado: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    val apprentice: ApprenticeDto? = null,
    val instructor: InstructorDto? = null,
)

/** Perfil público (`GET /general-users/{id}/perfil`): solo datos de contacto. */
@Serializable
data class PerfilDto(
    val id: Int = 0,
    val nombre: String = "",
    val apellido: String = "",
    val correo: String = "",
    @SerialName("foto_url") val fotoUrl: String? = null,
    val rol: String = "aprendiz",
)

/** Creación de cuenta (registro público y alta desde el panel de admin). */
@Serializable
data class UserCreateRequest(
    val nombre: String,
    val apellido: String,
    val correo: String,
    val password: String,
    val rol: String,
    @SerialName("foto_url") val fotoUrl: String? = null,
    val estado: Boolean? = null,
)

/**
 * PUT /general-users: envía siempre los escalares que exige el backend
 * (equivalente a `payloadCuenta()` del frontend). [password] y [fotoUrl] solo
 * viajan cuando cambian: con `encodeDefaults=false` no se serializan y así no
 * se pisa la foto ni se re-hashea la contraseña en cada guardado.
 */
@Serializable
data class UserUpdateRequest(
    val nombre: String,
    val apellido: String,
    val correo: String,
    val rol: String,
    val estado: Boolean,
    val password: String? = null,
    @SerialName("foto_url") val fotoUrl: String? = null,
)
