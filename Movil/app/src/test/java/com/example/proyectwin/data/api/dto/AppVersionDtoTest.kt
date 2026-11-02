package com.example.proyectwin.data.api.dto

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Contrato JSON de GET /v1/app-version/android: parsea la respuesta completa
 * del backend y tolera respuestas incompletas o con campos extra.
 */
class AppVersionDtoTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `parsea la respuesta completa del backend`() {
        val payload = """
            {
              "latest_version": "1.4.0",
              "latest_version_code": 5,
              "minimum_supported_version": "1.3.0",
              "minimum_supported_version_code": 4,
              "enforce_minimum": false,
              "download_url": "https://proyectwin-mvp-production.up.railway.app/apk/proyecttwin.apk",
              "apk_sha256": "f462fd283b14d9c7308095131318db363d9f15f0a667dfb5c65982d183b7a386",
              "apk_size": 14920772,
              "published_at": "2026-11-02T19:00:00Z",
              "release_notes": "Mejoras de estabilidad"
            }
        """.trimIndent()

        val dto = json.decodeFromString<AppVersionDto>(payload)

        assertEquals("1.4.0", dto.latestVersion)
        assertEquals(5, dto.latestVersionCode)
        assertEquals("1.3.0", dto.minimumSupportedVersion)
        assertEquals(4, dto.minimumSupportedVersionCode)
        assertFalse(dto.enforceMinimum)
        assertEquals(
            "https://proyectwin-mvp-production.up.railway.app/apk/proyecttwin.apk",
            dto.downloadUrl,
        )
        assertEquals(
            "f462fd283b14d9c7308095131318db363d9f15f0a667dfb5c65982d183b7a386",
            dto.apkSha256,
        )
        assertEquals(14920772L, dto.apkSize)
        assertEquals("2026-11-02T19:00:00Z", dto.publishedAt)
        assertEquals("Mejoras de estabilidad", dto.releaseNotes)
    }

    @Test
    fun `respuesta vacia o incompleta no rompe el parseo`() {
        val dto = json.decodeFromString<AppVersionDto>("{}")

        assertNull(dto.latestVersion)
        assertNull(dto.downloadUrl)
        assertFalse(dto.enforceMinimum)
    }

    @Test
    fun `ignora campos desconocidos del servidor`() {
        val payload = """
            { "latest_version": "2.0.0", "campo_nuevo": { "x": 1 } }
        """.trimIndent()

        val dto = json.decodeFromString<AppVersionDto>(payload)

        assertEquals("2.0.0", dto.latestVersion)
        assertTrue(dto.enforceMinimum.not()) // informativo: sigue en false por defecto
    }
}
