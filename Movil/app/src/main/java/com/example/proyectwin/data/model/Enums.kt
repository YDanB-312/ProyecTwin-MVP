package com.example.proyectwin.data.model

enum class ProjectStatus(val value: String) {
    EN_PROGRESO("en_progreso"),
    COMPLETADO("completado"),
    PENDIENTE("pendiente"),
    CANCELADO("cancelado");

    companion object {
        fun fromValue(value: String): ProjectStatus =
            entries.find { it.value == value } ?: PENDIENTE
    }
}

enum class ClassGroupStatus(val value: String) {
    ACTIVO("activo"),
    INACTIVO("inactivo"),
    FINALIZADO("finalizado");

    companion object {
        fun fromValue(value: String): ClassGroupStatus =
            entries.find { it.value == value } ?: ACTIVO
    }
}

enum class BugReportStatus(val value: String) {
    PENDIENTE("pendiente"),
    EN_REVISION("en_revision"),
    RESUELTO("resuelto"),
    CERRADO("cerrado");

    companion object {
        fun fromValue(value: String): BugReportStatus =
            entries.find { it.value == value } ?: PENDIENTE
    }
}

enum class BugReportType(val value: String) {
    FUNCIONAL("funcional"),
    VISUAL("visual"),
    RENDIMIENTO("rendimiento"),
    SEGURIDAD("seguridad"),
    OTRO("otro");

    companion object {
        fun fromValue(value: String): BugReportType =
            entries.find { it.value == value } ?: OTRO
    }
}

enum class SimilarityStatus(val value: String) {
    PENDIENTE("pendiente"),
    CONFIRMADO("confirmado"),
    RECHAZADO("rechazado");

    companion object {
        fun fromValue(value: String): SimilarityStatus =
            entries.find { it.value == value } ?: PENDIENTE
    }
}

enum class NotificationType(val value: String) {
    INFO("info"),
    WARNING("warning"),
    SUCCESS("success"),
    ERROR("error");

    companion object {
        fun fromValue(value: String): NotificationType =
            entries.find { it.value == value } ?: INFO
    }
}

enum class UserRole(val value: String) {
    INSTRUCTOR("instructor"),
    APRENDIZ("aprendiz"),
    ADMINISTRADOR("administrador");

    companion object {
        fun fromValue(value: String): UserRole =
            entries.find { it.value == value } ?: APRENDIZ
    }
}
