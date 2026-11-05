package com.example.proyectwin.data.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** La UI de "Mis proyectos" debe excluir propuestas de otros aprendices de la ficha. */
class ProjectPertenenciaTest {

    private val usuario = GeneralUser(
        id = 7,
        name = "Ana Aprendiz",
        email = "ana@example.com",
        role = "aprendiz",
    )

    @Test
    fun `el creador y los integrantes del equipo son propietarios`() {
        val creada = Project(id = 1, title = "Uno", studentId = usuario.id)
        val enEquipo = Project(
            id = 2,
            title = "Dos",
            studentId = 99,
            equipo = listOf(usuario),
        )

        assertTrue(creada.perteneceA(usuario.id))
        assertTrue(enEquipo.perteneceA(usuario.id))
    }

    @Test
    fun `una propuesta de otro aprendiz de la ficha no es propia`() {
        val ajena = Project(id = 3, title = "Tres", studentId = 99)

        assertFalse(ajena.perteneceA(usuario.id))
        assertFalse(ajena.perteneceA(null))
    }
}
