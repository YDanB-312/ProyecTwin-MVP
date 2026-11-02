package com.example.proyectwin.domain.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordPolicyTest {

    @Test
    fun `acepta frases de al menos doce caracteres`() {
        assertTrue(PasswordPolicy.esValida("clave segura 2026"))
        assertTrue(PasswordPolicy.esValida("otraClaveDistinta"))
    }

    @Test
    fun `rechaza menos de doce caracteres`() {
        assertFalse(PasswordPolicy.esValida("corta123"))
    }

    @Test
    fun `rechaza claves comunes y repeticiones`() {
        assertFalse(PasswordPolicy.esValida("password1234"))
        assertFalse(PasswordPolicy.esValida("aaaaaaaaaaaa"))
    }
}
