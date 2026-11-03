package com.example.proyectwin.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * GET /app-version/android: versión publicada del APK (consulta pública).
 * Los campos son tolerantes a datos incompletos: la app decide qué hacer sin
 * romperse si el servidor responde algo inesperado.
 */
@Serializable
data class AppVersionDto(
    @SerialName("latest_version") val latestVersion: String? = null,
    @SerialName("latest_version_code") val latestVersionCode: Int? = null,
    @SerialName("minimum_supported_version") val minimumSupportedVersion: String? = null,
    @SerialName("minimum_supported_version_code") val minimumSupportedVersionCode: Int? = null,
    @SerialName("enforce_minimum") val enforceMinimum: Boolean = false,
    @SerialName("download_url") val downloadUrl: String? = null,
    @SerialName("apk_sha256") val apkSha256: String? = null,
    @SerialName("apk_size") val apkSize: Long? = null,
    @SerialName("published_at") val publishedAt: String? = null,
    @SerialName("release_notes") val releaseNotes: String? = null,
)
