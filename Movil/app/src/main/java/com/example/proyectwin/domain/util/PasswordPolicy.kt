package com.example.proyectwin.domain.util

/**
 * Misma política visible que la web: mínimo 12 caracteres y rechazo local de
 * las contraseñas más comunes. El backend sigue siendo la autoridad final.
 */
object PasswordPolicy {
    const val MIN_LENGTH = 12

    private val COMUNES = setOf(
        "123456789012",
        "1234567890123",
        "12345678901234",
        "password1234",
        "password12345",
        "contrasena123",
        "contraseña123",
        "qwertyuiop12",
        "qwertyuiopas",
        "abcdefghijkl",
        "administrador",
        "administrator",
        "proyectwin123",
        "proyecttwin123",
        "sena12345678",
        "iloveyou1234",
        "welcome12345",
        "letmein12345",
        "monkey123456",
        "dragon123456",
    )

    fun esValida(valor: String): Boolean {
        val limpia = valor.trim().lowercase()
        val compacta = limpia.replace(Regex("[\\s\\-_]+"), "")
        if (limpia.length < MIN_LENGTH) return false
        if (limpia in COMUNES || compacta in COMUNES) return false
        return Regex("^(.)\\1{11,}$").matches(valor) == false
    }
}
