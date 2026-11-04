package com.example.proyectwin.ui.manual

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ManualContentTest {

    @Test
    fun `los identificadores de capitulo son unicos`() {
        val ids = ManualContent.secciones.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `los enlaces relacionados apuntan a capitulos existentes`() {
        val ids = ManualContent.secciones.map { it.id }.toSet()
        ManualContent.secciones.forEach { seccion ->
            seccion.relacionados.forEach { relacionado ->
                assertTrue(
                    "El capítulo ${seccion.id} referencia $relacionado",
                    relacionado in ids,
                )
            }
        }
    }

    @Test
    fun `cada rol tiene contenido suficiente`() {
        listOf(
            ManualRole.APRENDIZ,
            ManualRole.INSTRUCTOR,
            ManualRole.ADMIN,
        ).forEach { rol ->
            val propias = ManualContent.secciones.count { it.roles.contains(rol) }
            assertTrue("Faltan capítulos para $rol", propias >= 5)
        }
    }

    @Test
    fun `todos los capitulos tienen contenido basico`() {
        ManualContent.secciones.forEach { seccion ->
            assertTrue("Título vacío en ${seccion.id}", seccion.titulo.isNotBlank())
            assertTrue("Resumen vacío en ${seccion.id}", seccion.resumen.isNotBlank())
            assertTrue("Parte vacía en ${seccion.id}", seccion.parte.isNotBlank())
            assertFalse("Pasos vacíos en ${seccion.id}", seccion.pasos.isEmpty())
            assertTrue("Número vacío en ${seccion.id}", seccion.numero.isNotBlank())
        }
    }

    @Test
    fun `los numeros son unicos y consecutivos`() {
        val numeros = ManualContent.secciones.map { it.numero.toInt() }
        assertEquals((1..ManualContent.secciones.size).toList(), numeros)
    }

    @Test
    fun `las capturas declaradas son recursos validos`() {
        val figuras = ManualContent.secciones.flatMap { it.figuras }
        assertTrue(figuras.isNotEmpty())
        assertTrue(figuras.any { it.recurso != null })
        assertTrue(figuras.filter { it.recurso != null }.all { (it.recurso ?: 0) > 0 })
    }

    @Test
    fun `las preguntas frecuentes estan completas`() {
        assertFalse(ManualContent.preguntasFrecuentes.isEmpty())
        ManualContent.preguntasFrecuentes.forEach { faq ->
            assertTrue(faq.pregunta.isNotBlank())
            assertTrue(faq.respuesta.isNotBlank())
            assertFalse(faq.roles.isEmpty())
        }
    }
}
