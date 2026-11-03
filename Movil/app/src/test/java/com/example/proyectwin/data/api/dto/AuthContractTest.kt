package com.example.proyectwin.data.api.dto

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Contratos que Android comparte con la web y que no deben divergir. */
class AuthContractTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `el login por correo no envia username`() {
        val cuerpo = json.encodeToString(
            LoginRequest(correo = "ana@sena.edu.co", password = "secreta"),
        )

        assertTrue(cuerpo.contains("\"correo\":\"ana@sena.edu.co\""))
        assertFalse(cuerpo.contains("\"username\""))
        assertFalse(cuerpo.contains("\"recordarme\""))
    }

    @Test
    fun `el borrado normal no envia hash nulo`() {
        val cuerpo = json.encodeToString(
            DeleteUserRequest(confirmarEliminacion = false),
        )

        assertTrue(cuerpo.contains("\"confirmar_eliminacion\":false"))
        assertFalse(cuerpo.contains("confirmacion_hash"))
    }

    @Test
    fun `la segunda confirmacion envia hash`() {
        val cuerpo = json.encodeToString(
            DeleteUserRequest(confirmarEliminacion = true, confirmacionHash = "abc"),
        )

        assertTrue(cuerpo.contains("\"confirmar_eliminacion\":true"))
        assertTrue(cuerpo.contains("\"confirmacion_hash\":\"abc\""))
    }
}
