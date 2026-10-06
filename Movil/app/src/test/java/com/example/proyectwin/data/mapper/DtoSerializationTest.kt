package com.example.proyectwin.data.mapper

import com.example.proyectwin.data.api.dto.GeneralUserDto
import com.example.proyectwin.data.api.dto.NotificationDto
import com.example.proyectwin.data.api.dto.ProjectDto
import com.example.proyectwin.data.api.dto.SimilarityDto
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

/** Contratos de deserialización contra las respuestas reales de Laravel. */
class DtoSerializationTest {

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    @Test
    fun notification_parsesLeidaAsInt() {
        val dto = json.decodeFromString<NotificationDto>(
            """{"id":1,"titulo":"Nuevo","tipo":"sistema","enlace":"proyecto:5","leida":0,"fecha":"2026-10-01"}""",
        )
        assertEquals(0, dto.leida)
        assertFalse(dto.toDomain().leido)
        assertEquals("proyecto", dto.toDomain().enlaceModulo)
        assertEquals(5, dto.toDomain().enlaceId)
    }

    @Test
    fun similarity_parsesVigenteAndNestedProjects() {
        val cuerpo = """
            {
              "id": 9,
              "porcentaje": 42.5,
              "vigente": false,
              "id_proyecto_1": 3,
              "id_proyecto_2": 7,
              "project1": {"id":3,"titulo":"Uno","estado":"aprobado"},
              "project2": {"id":7,"titulo":"Dos","estado":"rechazado"}
            }
        """.trimIndent()
        val dto = json.decodeFromString<SimilarityDto>(cuerpo)
        assertFalse(dto.vigente)
        val dominio = dto.toDomain()
        assertEquals("Histórica", dominio.estadoDisplay)
        assertEquals("Uno", dominio.project1?.title)
        assertEquals("Dos", dominio.project2?.title)
    }

    @Test
    fun similarity_parsesTemaNumericoYTexto() {
        // El motor guardó `detalles.tema` como número en pares antiguos y como
        // null/texto en los nuevos. Antes, el par completo fallaba al
        // deserializar y quedaba oculto en los listados.
        val numerico = """
            {
              "id": 1,
              "porcentaje": 32,
              "detalles": {"palabras":21,"caracteres":43,"tema":40,"cobertura":30,"pasajes":[]},
              "id_proyecto_1": 1,
              "id_proyecto_2": 7
            }
        """.trimIndent()
        val dto = json.decodeFromString<SimilarityDto>(numerico)
        assertEquals("40", dto.toDomain().detalles?.tema)

        val texto = """
            {
              "id": 2,
              "porcentaje": 12,
              "detalles": {"palabras":1,"caracteres":2,"tema":"inventario","cobertura":3},
              "id_proyecto_1": 2,
              "id_proyecto_2": 5
            }
        """.trimIndent()
        assertEquals("inventario", json.decodeFromString<SimilarityDto>(texto).toDomain().detalles?.tema)
    }

    @Test
    fun project_parsesBackendScalars() {
        val cuerpo = """
            {
              "id": 12,
              "titulo": "Propuesta",
              "resumen": "Resumen",
              "palabras_clave": "a,b",
              "area_aplicacion": "Web",
              "objetivo_general": "Objetivo",
              "objetivos_especificos": ["uno","dos"],
              "estado": "pendiente",
              "huella_envio": "abc",
              "id_creador": 4,
              "id_instructor_asignado": 2,
              "id_class_group": 8,
              "classGroup": {"id":8,"codigo":"abc-defg","estado":"activo","program":{"id":1,"nombre":"ADSO"}}
            }
        """.trimIndent()
        val dominio = json.decodeFromString<ProjectDto>(cuerpo).toDomain()
        assertEquals("pendiente", dominio.estado)
        assertEquals("En revisión", dominio.statusDisplay)
        assertEquals("ADSO", dominio.programa)
        assertEquals("activo", dominio.fichaEstado)
        assertEquals(2, dominio.instructorId)
	    assertTrue(dominio.esEditable)
    }

    @Test
    fun generalUser_parsesAdminCredentialFields() {
        val cuerpo = """
            {
              "id": 5,
              "nombre": "Ana",
              "apellido": "Gómez",
              "correo": "ana@sena.edu.co",
              "username": "agomezr_12345",
              "rol": "aprendiz",
              "estado": true,
              "must_change_password": true,
              "tipo_documento": "CC",
              "numero_documento": "1098",
              "credenciales_enviadas_en": "2026-10-01T12:00:00Z"
            }
        """.trimIndent()
        val dominio = json.decodeFromString<GeneralUserDto>(cuerpo).toDomain()
        assertEquals("agomezr_12345", dominio.username)
        assertTrue(dominio.mustChangePassword)
        assertEquals("CC", dominio.tipoDocumento)
        assertEquals("1098", dominio.documentoIdentidad)
        assertNotNull(dominio.credencialesEnviadasEn)
    }
}
