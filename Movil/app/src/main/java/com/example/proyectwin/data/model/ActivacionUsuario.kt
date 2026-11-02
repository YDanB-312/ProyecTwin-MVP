package com.example.proyectwin.data.model

/** Resultado del alta: la cuenta queda pendiente hasta que el usuario activa. */
data class ActivacionUsuario(
    val usuario: GeneralUser,
    val enviada: Boolean,
    val correo: String,
    val mensaje: String,
)
