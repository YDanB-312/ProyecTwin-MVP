package com.example.proyectwin.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class EnumsTest {

    @Test
    fun projectStatus_fromValue_returnsCorrect() {
        assertEquals(ProjectStatus.EN_PROGRESO, ProjectStatus.fromValue("en_progreso"))
        assertEquals(ProjectStatus.COMPLETADO, ProjectStatus.fromValue("completado"))
        assertEquals(ProjectStatus.PENDIENTE, ProjectStatus.fromValue("pendiente"))
        assertEquals(ProjectStatus.CANCELADO, ProjectStatus.fromValue("cancelado"))
    }

    @Test
    fun projectStatus_fromValue_invalid_returnsDefault() {
        assertEquals(ProjectStatus.PENDIENTE, ProjectStatus.fromValue("invalido"))
    }

    @Test
    fun userRole_fromValue_returnsCorrect() {
        assertEquals(UserRole.INSTRUCTOR, UserRole.fromValue("instructor"))
        assertEquals(UserRole.APRENDIZ, UserRole.fromValue("aprendiz"))
        assertEquals(UserRole.ADMINISTRADOR, UserRole.fromValue("administrador"))
    }

    @Test
    fun notificationType_fromValue_returnsCorrect() {
        assertEquals(NotificationType.INFO, NotificationType.fromValue("info"))
        assertEquals(NotificationType.WARNING, NotificationType.fromValue("warning"))
        assertEquals(NotificationType.SUCCESS, NotificationType.fromValue("success"))
        assertEquals(NotificationType.ERROR, NotificationType.fromValue("error"))
    }

    @Test
    fun bugReportStatus_fromValue_returnsCorrect() {
        assertEquals(BugReportStatus.PENDIENTE, BugReportStatus.fromValue("pendiente"))
        assertEquals(BugReportStatus.EN_REVISION, BugReportStatus.fromValue("en_revision"))
        assertEquals(BugReportStatus.RESUELTO, BugReportStatus.fromValue("resuelto"))
        assertEquals(BugReportStatus.CERRADO, BugReportStatus.fromValue("cerrado"))
    }

    @Test
    fun bugReportType_fromValue_returnsCorrect() {
        assertEquals(BugReportType.FUNCIONAL, BugReportType.fromValue("funcional"))
        assertEquals(BugReportType.VISUAL, BugReportType.fromValue("visual"))
        assertEquals(BugReportType.RENDIMIENTO, BugReportType.fromValue("rendimiento"))
        assertEquals(BugReportType.SEGURIDAD, BugReportType.fromValue("seguridad"))
        assertEquals(BugReportType.OTRO, BugReportType.fromValue("otro"))
    }

    @Test
    fun similarityStatus_fromValue_returnsCorrect() {
        assertEquals(SimilarityStatus.PENDIENTE, SimilarityStatus.fromValue("pendiente"))
        assertEquals(SimilarityStatus.CONFIRMADO, SimilarityStatus.fromValue("confirmado"))
        assertEquals(SimilarityStatus.RECHAZADO, SimilarityStatus.fromValue("rechazado"))
    }

    @Test
    fun classGroupStatus_fromValue_returnsCorrect() {
        assertEquals(ClassGroupStatus.ACTIVO, ClassGroupStatus.fromValue("activo"))
        assertEquals(ClassGroupStatus.INACTIVO, ClassGroupStatus.fromValue("inactivo"))
        assertEquals(ClassGroupStatus.FINALIZADO, ClassGroupStatus.fromValue("finalizado"))
    }
}
