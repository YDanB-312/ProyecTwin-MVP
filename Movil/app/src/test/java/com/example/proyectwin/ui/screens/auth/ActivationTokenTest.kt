package com.example.proyectwin.ui.screens.auth

import org.junit.Assert.assertEquals
import org.junit.Test

class ActivationTokenTest {

    @Test
    fun `extrae el token del fragmento del enlace`() {
        assertEquals(
            "abc123",
            extraerToken("https://proyectwin.app/activar-cuenta#token=abc123"),
        )
    }

    @Test
    fun `extrae y decodifica el token del query string`() {
        assertEquals(
            "a b+c",
            extraerToken("https://proyectwin.app/activar-cuenta?token=a%20b%2Bc"),
        )
    }

    @Test
    fun `acepta el codigo pegado sin url`() {
        assertEquals("tokenPlano123", extraerToken("tokenPlano123"))
    }
}
