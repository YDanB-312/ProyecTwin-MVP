package com.example.proyectwin.data.model

/** Usuario recién creado/restablecido con sus credenciales temporales. */
data class CredencialesUsuario(
    val usuario: GeneralUser,
    val username: String,
    val passwordTemporal: String,
    val enviadas: Boolean,
)
