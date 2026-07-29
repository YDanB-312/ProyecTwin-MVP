package com.example.proyectwin.data.model

import kotlinx.serialization.Serializable

@Serializable
data class BugReport(
    val id: Int,
    val titulo: String,
    val descripcion: String,
    val tipo: String = BugReportType.OTRO.value,
    val estado: String = BugReportStatus.PENDIENTE.value,
    val projectId: Int? = null,
    val reporterId: Int? = null,
    val reporterName: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    val bugType: BugReportType get() = BugReportType.fromValue(tipo)
    val bugStatus: BugReportStatus get() = BugReportStatus.fromValue(estado)
    val typeDisplay: String get() = when (bugType) {
        BugReportType.FUNCIONAL -> "Funcional"
        BugReportType.VISUAL -> "Visual"
        BugReportType.RENDIMIENTO -> "Rendimiento"
        BugReportType.SEGURIDAD -> "Seguridad"
        BugReportType.OTRO -> "Otro"
    }
    val statusDisplay: String get() = when (bugStatus) {
        BugReportStatus.PENDIENTE -> "Pendiente"
        BugReportStatus.EN_REVISION -> "En Revisión"
        BugReportStatus.RESUELTO -> "Resuelto"
        BugReportStatus.CERRADO -> "Cerrado"
    }
}
