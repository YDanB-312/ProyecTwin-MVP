package com.example.proyectwin.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val correo: String,
    val password: String,
    val recordarme: Boolean = false,
)

@Serializable
data class LoginResponse(
    val user: GeneralUserDto,
    val token: String,
    val rol: String,
)

@Serializable
data class RegisterRequest(
    val nombre: String,
    val apellido: String,
    val correo: String,
    val password: String,
    val rol: String,
)

@Serializable
data class ForgotPasswordRequest(val correo: String)

@Serializable
data class ForgotPasswordResponse(
    val message: String = "",
    @SerialName("reset_url") val resetUrl: String? = null,
)

@Serializable
data class ResetPasswordRequest(
    val token: String,
    val correo: String,
    val password: String,
    @SerialName("password_confirmation") val passwordConfirmation: String,
)

@Serializable
data class ChangePasswordRequest(
    @SerialName("password_actual") val passwordActual: String,
    val password: String,
    @SerialName("password_confirmation") val passwordConfirmation: String,
)

@Serializable
data class ChangeEmailRequest(
    val correo: String,
    @SerialName("password_actual") val passwordActual: String,
)

@Serializable
data class ChangeEmailResponse(
    val message: String = "",
    val correo: String = "",
)

@Serializable
data class MessageResponse(val message: String = "")
