package com.example.proyectwin.data.model

/** Integrante real del equipo: fila del pivote `apprentice_projects` + usuario. */
data class TeamMember(
    val idPivote: Int,
    val idAprendiz: Int,
    val usuario: GeneralUser,
)
