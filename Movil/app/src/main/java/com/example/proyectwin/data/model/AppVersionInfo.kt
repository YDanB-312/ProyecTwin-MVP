package com.example.proyectwin.data.model

/** Versión publicada del APK Android (respuesta pública del backend). */
data class AppVersionInfo(
    val versionPublicada: String,
    val versionMinima: String,
    /** Política de actualización obligatoria definida por el servidor. */
    val obligatoria: Boolean,
    val urlDescarga: String?,
    val notas: String?,
    /** Identificador monótono usado como criterio principal de actualización. */
    val versionCode: Int? = null,
    val versionMinimaCode: Int? = null,
    /** Integridad del APK publicada por el backend (hex SHA-256). */
    val sha256: String? = null,
    val tamanoBytes: Long? = null,
    val fechaPublicacion: String? = null,
)
