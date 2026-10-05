package com.example.proyectwin.data.api.service

import com.example.proyectwin.data.api.dto.ChangeEmailRequest
import com.example.proyectwin.data.api.dto.ChangeEmailResponse
import com.example.proyectwin.data.api.dto.ChangePasswordRequest
import com.example.proyectwin.data.api.dto.ForgotPasswordRequest
import com.example.proyectwin.data.api.dto.ForgotPasswordResponse
import com.example.proyectwin.data.api.dto.GeneralUserDto
import com.example.proyectwin.data.api.dto.LoginRequest
import com.example.proyectwin.data.api.dto.LoginResponse
import com.example.proyectwin.data.api.dto.MessageResponse
import com.example.proyectwin.data.api.dto.RegisterRequest
import com.example.proyectwin.data.api.dto.ResetPasswordRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

/** Sesión y cuentas públicas (auth y registro, bajo `/v1/auth` y general-users). */
interface AuthApi {

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @POST("auth/logout")
    suspend fun logout(): MessageResponse

    @GET("auth/me")
    suspend fun me(): GeneralUserDto

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequest): ForgotPasswordResponse

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body body: ResetPasswordRequest): MessageResponse

    @PUT("auth/password")
    suspend fun changePassword(@Body body: ChangePasswordRequest): MessageResponse

    @PUT("auth/email")
    suspend fun changeEmail(@Body body: ChangeEmailRequest): ChangeEmailResponse

    /** Registro público de cuentas (rol aprendiz/instructor). */
    @POST("general-users")
    suspend fun register(@Body body: RegisterRequest): GeneralUserDto
}
