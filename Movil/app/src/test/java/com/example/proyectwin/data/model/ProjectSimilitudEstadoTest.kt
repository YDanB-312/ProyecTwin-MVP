package com.example.proyectwin.data.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProjectSimilitudEstadoTest {

    private fun proyecto(estado: String?) = Project(
        id = 1,
        title = "Propuesta",
        estado = ProjectStatus.PENDIENTE.value,
        similitudEstado = estado,
    )

    @Test
    fun `un resultado pendiente o con error no permite aprobar`() {
        assertFalse(proyecto("pendiente").puedeAprobarConSimilitud)
        assertFalse(proyecto("en_proceso").puedeAprobarConSimilitud)
        assertFalse(proyecto("error").puedeAprobarConSimilitud)
    }

    @Test
    fun `un resultado actualizado o sin referencias permite aprobar`() {
        assertTrue(proyecto("actualizado").puedeAprobarConSimilitud)
        assertTrue(proyecto("sin_referencias").puedeAprobarConSimilitud)
        assertTrue(proyecto(null).puedeAprobarConSimilitud)
    }
}
