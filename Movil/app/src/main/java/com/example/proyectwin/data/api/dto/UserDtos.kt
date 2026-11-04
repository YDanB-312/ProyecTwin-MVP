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
    @SerialName("must_change_password") val mustChangePassword: Boolean = false,
    @SerialName("email_verified_at") val emailVerifiedAt: String? = null,
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
 * POST /general-users (solo admin): el backend exige documento, genera el
 * identificador interno y deja la cuenta pendiente de activación por correo.
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
    @SerialName("tipo_documento") val tipoDocumento: String? = null,
    @SerialName("numero_documento") val numeroDocumento: String? = null,
    // Sin valor por defecto: `null` debe viajar para retirar una foto. En el
    // PUT parcial los campos opcionales nulos se omiten, pero este se envía
    // siempre con el valor actual o con null cuando el admin la retira.
    @SerialName("foto_url") val fotoUrl: String?,
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

/** Resultado del envío del enlace de activación. */
@Serializable
data class ActivacionDto(
    val enviada: Boolean = false,
    val correo: String = "",
    val mensaje: String = "",
)

/** Respuesta `{usuario, activacion}` del alta administrativa. */
@Serializable
data class UsuarioActivacionDto(
    val usuario: GeneralUserDto,
    val activacion: ActivacionDto = ActivacionDto(),
)

/** Respuesta del reenvío de activación (incluye `activacion`). */
@Serializable
data class ReenviarActivacionResponse(
    val message: String = "",
    val activacion: ActivacionDto? = null,
)

/**
 * Cuerpo del borrado. `confirmar_eliminacion` solo se envía en la segunda
 * confirmación; el backend responde 409 con impacto y hash si hay historial.
 */
@Serializable
data class DeleteUserRequest(
    // Sin valor por defecto: `true` también debe viajar (confirmar=true es una
    // decisión explícita, no el estado inicial). El hash nulo se omite.
    @SerialName("confirmar_eliminacion") val confirmarEliminacion: Boolean,
    @SerialName("confirmacion_hash") val confirmacionHash: String? = null,
)
