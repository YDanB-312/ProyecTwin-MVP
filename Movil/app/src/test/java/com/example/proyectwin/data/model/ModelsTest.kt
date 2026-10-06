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
        val instructor = GeneralUser(id = 1, name = "Carlos", email = "c@t.com", role = "instructor")
        val aprendiz = GeneralUser(id = 2, name = "Ana", email = "a@t.com", role = "aprendiz")
        val admin = GeneralUser(id = 3, name = "Admin", email = "ad@t.com", role = "admin")
        assertEquals("Instructor", instructor.roleDisplayName)
        assertEquals("Aprendiz", aprendiz.roleDisplayName)
        assertEquals("Administrador", admin.roleDisplayName)
    }

    @Test
    fun ficha_statusDisplay() {
        val activa = Ficha(id = 1, codigo = "abc-defg", programa = "ADSO", estado = "activo")
        val finalizada = Ficha(id = 2, codigo = "hij-klmn", programa = "Web", estado = "finalizado")
        val anulada = Ficha(id = 3, codigo = "", programa = "Web", estado = "anulada")
        assertEquals("Activo", activa.statusDisplay)
        assertEquals("Finalizado", finalizada.statusDisplay)
        assertEquals("Anulada", anulada.statusDisplay)
    }

    @Test
    fun project_statusDisplay() {
        val borrador = Project(id = 1, title = "Test", estado = "borrador")
        val pendiente = Project(id = 2, title = "Test", estado = "pendiente")
        val aprobado = Project(id = 3, title = "Test", estado = "aprobado")
        val rechazado = Project(id = 4, title = "Test", estado = "rechazado")
        assertEquals("Borrador", borrador.statusDisplay)
        assertEquals("En revisión", pendiente.statusDisplay)
        assertEquals("Aprobado", aprobado.statusDisplay)
        assertEquals("Rechazado", rechazado.statusDisplay)
    }

    @Test
    fun project_editabilidad_porEstado() {
        val borrador = Project(id = 1, title = "T", estado = "borrador")
        val pendiente = Project(id = 2, title = "T", estado = "pendiente")
        val rechazado = Project(id = 3, title = "T", estado = "rechazado")
        val aprobado = Project(id = 4, title = "T", estado = "aprobado")
        assertTrue(borrador.esEditable && borrador.puedeEnviar)
        assertTrue(pendiente.esEditable && !pendiente.puedeEnviar)
        assertTrue(rechazado.esEditable && rechazado.puedeEnviar)
        assertFalse(aprobado.esEditable)
        assertFalse(aprobado.puedeEnviar)
    }

    @Test
    fun similarity_similitudPercent_yEstado() {
        val vigente = Similarity(id = 1, projectId1 = 1, projectId2 = 2, similitud = 0.756, vigente = true)
        val historica = Similarity(id = 2, projectId1 = 1, projectId2 = 3, similitud = 0.4, vigente = false)
        assertEquals("75.6%", vigente.similitudPercent)
        assertEquals("Vigente", vigente.estadoDisplay)
        assertEquals("Histórica", historica.estadoDisplay)
    }

    @Test
    fun bugReport_typeDisplay() {
        val funcional = BugReport(id = 1, titulo = "T", descripcion = "D", tipo = "bug_ui")
        val visual = BugReport(id = 2, titulo = "T", descripcion = "D", tipo = "error_datos")
        assertEquals("Bug de UI", funcional.typeDisplay)
        assertEquals("Error de datos", visual.typeDisplay)
    }
}
