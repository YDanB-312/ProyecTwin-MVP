package com.example.proyectwin.data.model

import org.junit.Assert.*
import org.junit.Test

class ModelsTest {

    @Test
    fun generalUser_initials() {
        val user = GeneralUser(id = 1, name = "Ana Maria", email = "ana@test.com", role = "aprendiz")
        assertEquals("AM", user.initials)
    }

    @Test
    fun generalUser_singleName_initials() {
        val user = GeneralUser(id = 1, name = "Carlos", email = "carlos@test.com", role = "instructor")
        assertEquals("C", user.initials)
    }

    @Test
    fun generalUser_roleDisplay() {
        val instructor = GeneralUser(id = 1, name = "Carlos", email="c@t.com", role="instructor")
        val aprendiz = GeneralUser(id = 2, name = "Ana", email="a@t.com", role="aprendiz")
        val admin = GeneralUser(id = 3, name = "Admin", email="ad@t.com", role="administrador")
        assertEquals("Instructor", instructor.roleDisplayName)
        assertEquals("Aprendiz", aprendiz.roleDisplayName)
        assertEquals("Administrador", admin.roleDisplayName)
    }

    @Test
    fun ficha_codigoValido() {
        assertTrue(Ficha.esCodigoValido("FT-2692701"))
        assertTrue(Ficha.esCodigoValido("FT-3309264"))
        assertFalse(Ficha.esCodigoValido("FT-0000000"))
        assertFalse(Ficha.esCodigoValido(""))
        assertFalse(Ficha.esCodigoValido("invalido"))
    }

    @Test
    fun ficha_generarCodigo_format() {
        val codigo = Ficha.generarCodigo()
        assertTrue(codigo.matches(Regex("^FT-\\d{6}$")))
    }

    @Test
    fun ficha_statusDisplay() {
        val activa = Ficha(id = 1, codigo = "FT-2692701", programa = "ADSO", estado = "activo")
        val inactiva = Ficha(id = 2, codigo = "FT-2771109", programa = "Web", estado = "inactivo")
        assertEquals("Activo", activa.statusDisplay)
        assertEquals("Inactivo", inactiva.statusDisplay)
    }

    @Test
    fun project_statusDisplay() {
        val enProgreso = Project(id = 1, title = "Test", estado = "en_progreso")
        val completado = Project(id = 2, title = "Test", estado = "completado")
        assertEquals("En Progreso", enProgreso.statusDisplay)
        assertEquals("Completado", completado.statusDisplay)
    }

    @Test
    fun similarity_similitudPercent() {
        val sim = Similarity(id = 1, projectId1 = 1, projectId2 = 2, similitud = 0.756)
        assertEquals("75.6%", sim.similitudPercent)
    }

    @Test
    fun bugReport_typeDisplay() {
        val funcional = BugReport(id = 1, titulo = "T", descripcion = "D", tipo = "funcional")
        val visual = BugReport(id = 2, titulo = "T", descripcion = "D", tipo = "visual")
        assertEquals("Funcional", funcional.typeDisplay)
        assertEquals("Visual", visual.typeDisplay)
    }
}
