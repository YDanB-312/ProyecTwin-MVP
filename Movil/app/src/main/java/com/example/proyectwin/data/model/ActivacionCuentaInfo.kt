package com.example.proyectwin.data.model

/** Datos enmascarados que el backend devuelve al validar un enlace de activación. */
data class ActivacionCuentaInfo(
    val nombre: String,
    val correo: String,
)
