package com.example.proyectwin.data.model

enum class ProjectStatus(val value: String) {
    EN_PROGRESO("en_progreso"),
    COMPLETADO("completado"),
    PENDIENTE("pendiente"),
    CANCELADO("cancelado"),
    APROBADO("aprobado"),
    RECHAZADO("rechazado");

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
    CERRADO("cerrado"),
    RECHAZADO("rechazado");

    companion object {
        fun fromValue(value: String): BugReportStatus =
            entries.find { it.value == value } ?: PENDIENTE
    }
}

enum class BugReportType(val value: String) {
    BUG_UI("bug_ui"),
    ERROR_DATOS("error_datos"),
    RENDIMIENTO("rendimiento"),
    SEGURIDAD("seguridad"),
    OTRO("otro"),
    SISTEMA("sistema"),
    PROYECTO("proyecto"),
    DATOS("datos");

    companion object {
        /** Opciones que ofrece el formulario de reporte, igual que el frontend. */
        val opcionesReporte: List<BugReportType> = listOf(BUG_UI, ERROR_DATOS, RENDIMIENTO, SEGURIDAD, OTRO)

        fun fromValue(value: String): BugReportType =
            entries.find { it.value == value } ?: OTRO
    }
}

enum class SimilarityStatus(val value: String) {
    PENDIENTE("pendiente"),
    REVISADO("revisado"),
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
    ERROR("error"),
    SIMILITUD("similitud"),
    OBSERVACION("observacion"),
    REVISION("revision"),
    MENSAJE("mensaje"),
    SISTEMA("sistema");

    companion object {
        fun fromValue(value: String): NotificationType =
            entries.find { it.value == value } ?: INFO
    }
}

enum class UserRole(val value: String) {
    INSTRUCTOR("instructor"),
    APRENDIZ("aprendiz"),
    ADMINISTRADOR("admin");

    companion object {
        /** Acepta también el valor legado "administrador" de sesiones y mocks antiguos. */
        fun fromValue(value: String): UserRole =
            entries.find { it.value == value }
                ?: if (value == "administrador") ADMINISTRADOR else APRENDIZ
    }
}
