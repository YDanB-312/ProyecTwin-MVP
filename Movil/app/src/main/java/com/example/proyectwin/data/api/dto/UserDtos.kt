package com.example.proyectwin.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Cuenta de usuario tal como la devuelve Laravel (`general_users`).
 * Relaciones incluidas con `?included=` (apprentice/instructor) según aplique.
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
    @SerialName("tipo_documento") val tipoDocumento: String? = null,
    @SerialName("numero_documento") val numeroDocumento: String? = null,
    @SerialName("credenciales_enviadas_en") val credencialesEnviadasEn: String? = null,
    @SerialName("credenciales_error") val credencialesError: String? = null,
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

/**
 * POST /general-users (solo admin): el backend exige documento y genera
 * username + contraseña temporal; no se envía contraseña desde el cliente.
 */
@Serializable
data class UserCreateRequest(
    val nombre: String,
    val apellido: String,
    @SerialName("tipo_documento") val tipoDocumento: String,
    @SerialName("numero_documento") val numeroDocumento: String,
    val correo: String,
    val rol: String,
    val estado: Boolean = true,
)

/**
 * PUT /general-users/{id} (admin): actualiza cuenta completa. El backend
 * ignora `password` por esta vía y cambia rol/estado solo para admin.
 */
@Serializable
data class UserUpdateRequest(
    val nombre: String,
    val apellido: String,
    val correo: String,
    val rol: String,
    val estado: Boolean,
    @SerialName("foto_url") val fotoUrl: String? = null,
)

/** Edición del propio perfil: solo nombre/apellido (el correo va por /auth/email). */
@Serializable
data class ProfileUpdateRequest(
    val nombre: String,
    val apellido: String,
)

/** Subida/quita de la foto propia. [fotoUrl] sin valor por defecto para que
 * un `null` explícito viaje y el backend borre la foto. */
@Serializable
data class FotoUpdateRequest(
    @SerialName("foto_url") val fotoUrl: String?,
)

/** Credenciales temporales devueltas por alta/restablecimiento. */
@Serializable
data class CredencialesDto(
    val username: String = "",
    @SerialName("password_temporal") val passwordTemporal: String = "",
    val enviadas: Boolean = false,
)

/** Respuesta `{usuario, credenciales}` de alta y restablecimiento. */
@Serializable
data class UsuarioCredencialesDto(
    val usuario: GeneralUserDto,
    val credenciales: CredencialesDto,
)

/** Cuerpo del PDF por lote: `{"ids":[...]}`. */
@Serializable
data class CredencialesRequest(val ids: List<Int>)
