package com.example.proyectwin.data.mock

import com.example.proyectwin.data.model.BugReport
import com.example.proyectwin.data.model.BugReportType
import com.example.proyectwin.data.model.Ficha
import com.example.proyectwin.data.model.GeneralUser
import com.example.proyectwin.data.model.Notification
import com.example.proyectwin.data.model.NotificationType
import com.example.proyectwin.data.model.Project
import com.example.proyectwin.data.model.ProjectStatus
import com.example.proyectwin.data.model.Similarity
import com.example.proyectwin.data.model.UserRole

object MockDataProvider {

    // Users
    val users = mutableListOf(
        GeneralUser(
            id = 1, name = "Carlos Instructor", email = "instructor@test.com",
            role = UserRole.INSTRUCTOR.value, token = "mock-token-instructor",
            telefono = "3001234567", documentoIdentidad = "1234567890"
        ),
        GeneralUser(
            id = 2, name = "Ana Aprendiz", email = "aprendiz@test.com",
            role = UserRole.APRENDIZ.value, token = "mock-token-aprendiz",
            telefono = "3007654321", documentoIdentidad = "0987654321"
        ),
        GeneralUser(
            id = 3, name = "Admin Principal", email = "admin@test.com",
            role = UserRole.ADMINISTRADOR.value, token = "mock-token-admin",
            telefono = "3005555555", documentoIdentidad = "1112233445"
        )
    )

    fun findUserByEmail(email: String): GeneralUser? = users.find { it.email == email }
    fun findUserById(id: Int): GeneralUser? = users.find { it.id == id }

    fun createUser(name: String, email: String, role: String, fichaId: Int? = null): GeneralUser {
        val nextId = (users.maxOfOrNull { it.id } ?: 0) + 1
        val user = GeneralUser(
            id = nextId,
            name = name,
            email = email,
            role = role,
            token = "mock-token-$nextId",
            fichaId = fichaId
        )
        users.add(user)
        return user
    }

    fun deleteUser(id: Int) {
        users.removeAll { it.id == id }
    }

    private val inactiveUserIds = mutableSetOf<Int>()

    fun deactivateUser(id: Int) {
        inactiveUserIds.add(id)
    }

    fun isUserActive(id: Int): Boolean = id !in inactiveUserIds

    private var nextFichaId: Int = 13

    // Fichas
    private val fichaList = mutableListOf(
        Ficha(
            id = 1, codigo = "FT-2692701", programa = "Análisis y Desarrollo de Software",
            instructorId = 1, instructorName = "Carlos Instructor", estado = "activo",
            estudiantes = listOf(users[1])
        ),
        Ficha(
            id = 2, codigo = "FT-2771109", programa = "Desarrollo Web",
            instructorId = 1, instructorName = "Carlos Instructor", estado = "activo"
        ),
        Ficha(
            id = 3, codigo = "FT-2823412", programa = "Machine Learning",
            instructorId = 1, instructorName = "Carlos Instructor", estado = "activo"
        ),
        Ficha(id = 4, codigo = "FT-2874441", programa = "Seguridad Informática", estado = "inactivo"),
        Ficha(id = 5, codigo = "FT-2902005", programa = "Data Science", estado = "finalizado"),
        Ficha(id = 6, codigo = "FT-2993476", programa = "Inteligencia Artificial", estado = "activo"),
        Ficha(id = 7, codigo = "FT-3039845", programa = "DevOps", estado = "activo"),
        Ficha(id = 8, codigo = "FT-3067030", programa = "Cloud Computing", estado = "inactivo"),
        Ficha(id = 9, codigo = "FT-3114059", programa = "Ciberseguridad", estado = "activo"),
        Ficha(id = 10, codigo = "FT-3184978", programa = "IoT", estado = "activo"),
        Ficha(id = 11, codigo = "FT-3258694", programa = "Big Data", estado = "activo"),
        Ficha(id = 12, codigo = "FT-3309264", programa = "Realidad Virtual", estado = "activo")
    )

    fun getAllFichas(): List<Ficha> = fichaList
    fun findFichaById(id: Int): Ficha? = fichaList.find { it.id == id }
    fun findFichaByCodigo(codigo: String): Ficha? = fichaList.find { it.codigo == codigo }
    fun getActiveFichas(): List<Ficha> = fichaList.filter { it.estado == "activo" }

    fun createFicha(codigo: String, programa: String, instructorName: String): Ficha {
        val ficha = Ficha(
            id = nextFichaId++,
            codigo = codigo,
            programa = programa,
            instructorName = instructorName,
            estado = "activo"
        )
        fichaList.add(ficha)
        return ficha
    }

    fun joinFicha(codigo: String, estudiante: GeneralUser): Boolean {
        val index = fichaList.indexOfFirst { it.codigo == codigo }
        if (index == -1) return false
        val ficha = fichaList[index]
        if (ficha.estudiantes.any { it.id == estudiante.id }) return true
        fichaList[index] = ficha.copy(estudiantes = ficha.estudiantes + estudiante)
        return true
    }

    // Projects
    private var nextProjectId: Int = 6
    private val projectList = mutableListOf(
        Project(
            id = 1, title = "Sistema de Ventas", description = "App de punto de venta",
            estado = ProjectStatus.EN_PROGRESO.value, studentId = 2, instructorId = 1, fichaId = 1,
            studentName = "Ana Aprendiz", instructorName = "Carlos Instructor"
        ),
        Project(
            id = 2, title = "Plataforma E-learning", description = "Plataforma educativa",
            estado = ProjectStatus.COMPLETADO.value, studentId = 2, instructorId = 1, fichaId = 1,
            studentName = "Ana Aprendiz", instructorName = "Carlos Instructor"
        ),
        Project(
            id = 3, title = "App de Inventarios", description = "Control de inventarios",
            estado = ProjectStatus.PENDIENTE.value, studentId = 2, instructorId = 1, fichaId = 1,
            studentName = "Ana Aprendiz", instructorName = "Carlos Instructor"
        ),
        Project(
            id = 4, title = "Gestor de Proyectos", description = "Herramienta de gestión",
            estado = ProjectStatus.EN_PROGRESO.value, studentId = 2, instructorId = 1, fichaId = 2,
            studentName = "Ana Aprendiz", instructorName = "Carlos Instructor"
        ),
        Project(
            id = 5, title = "Dashboard Analytics", description = "Visualización de datos",
            estado = ProjectStatus.CANCELADO.value, studentId = 2, instructorId = 1, fichaId = 1,
            studentName = "Ana Aprendiz", instructorName = "Carlos Instructor"
        )
    )

    fun getAllProjects(): List<Project> = projectList
    fun findProjectById(id: Int): Project? = projectList.find { it.id == id }
    fun getProjectsByStudent(studentId: Int): List<Project> = projectList.filter { it.studentId == studentId }
    fun getProjectsByInstructor(instructorId: Int): List<Project> = projectList.filter { it.instructorId == instructorId }
    fun getProjectsByFicha(fichaId: Int): List<Project> = projectList.filter { it.fichaId == fichaId }

    fun createProject(
        title: String,
        description: String,
        studentId: Int,
        instructorId: Int,
        fichaId: Int,
        studentName: String,
        instructorName: String
    ): Project {
        val project = Project(
            id = nextProjectId++,
            title = title,
            description = description,
            estado = ProjectStatus.PENDIENTE.value,
            studentId = studentId,
            instructorId = instructorId,
            fichaId = fichaId,
            studentName = studentName,
            instructorName = instructorName
        )
        projectList.add(project)
        return project
    }

    fun updateProjectEstado(id: Int, estado: String) {
        val index = projectList.indexOfFirst { it.id == id }
        if (index != -1) {
            projectList[index] = projectList[index].copy(estado = estado)
        }
    }

    fun updateProject(id: Int, title: String, description: String) {
        val index = projectList.indexOfFirst { it.id == id }
        if (index != -1) {
            projectList[index] = projectList[index].copy(title = title, description = description)
        }
    }

    // BugReports
    private var nextBugReportId: Int = 4
    private val bugReportList = mutableListOf(
        BugReport(
            id = 1, titulo = "Error al guardar", descripcion = "No guarda los cambios",
            tipo = BugReportType.FUNCIONAL.value, estado = "pendiente",
            projectId = 1, reporterId = 2, reporterName = "Ana Aprendiz"
        ),
        BugReport(
            id = 2, titulo = "Botón roto", descripcion = "El botón de submit no responde",
            tipo = BugReportType.VISUAL.value, estado = "en_revision",
            projectId = 1, reporterId = 2, reporterName = "Ana Aprendiz"
        ),
        BugReport(
            id = 3, titulo = "App lenta", descripcion = "Tarda mucho en cargar",
            tipo = BugReportType.RENDIMIENTO.value, estado = "resuelto",
            projectId = 2, reporterId = 2, reporterName = "Ana Aprendiz"
        )
    )

    fun getAllBugReports(): List<BugReport> = bugReportList
    fun getBugReportsByProject(projectId: Int): List<BugReport> = bugReportList.filter { it.projectId == projectId }
    fun getBugReportsByReporter(reporterId: Int): List<BugReport> = bugReportList.filter { it.reporterId == reporterId }

    fun updateBugReportEstado(id: Int, estado: String) {
        val index = bugReportList.indexOfFirst { it.id == id }
        if (index != -1) {
            bugReportList[index] = bugReportList[index].copy(estado = estado)
        }
    }

    fun createBugReport(
        titulo: String,
        descripcion: String,
        tipo: String,
        projectId: Int,
        reporterId: Int,
        reporterName: String
    ): BugReport {
        val report = BugReport(
            id = nextBugReportId++,
            titulo = titulo,
            descripcion = descripcion,
            tipo = tipo,
            estado = "pendiente",
            projectId = projectId,
            reporterId = reporterId,
            reporterName = reporterName
        )
        bugReportList.add(report)
        return report
    }

    // Similarities
    private val similarityList = mutableListOf(
        Similarity(
            id = 1, projectId1 = 1, projectId2 = 4,
            project1Title = "Sistema de Ventas", project2Title = "Gestor de Proyectos",
            project1Student = "Ana Aprendiz", project2Student = "Ana Aprendiz",
            similitud = 0.75, estado = "pendiente"
        ),
        Similarity(
            id = 2, projectId1 = 1, projectId2 = 3,
            project1Title = "Sistema de Ventas", project2Title = "App de Inventarios",
            project1Student = "Ana Aprendiz", project2Student = "Ana Aprendiz",
            similitud = 0.45, estado = "confirmado"
        )
    )

    fun getAllSimilarities(): List<Similarity> = similarityList
    fun getSimilaritiesByProject(projectId: Int): List<Similarity> =
        similarityList.filter { it.projectId1 == projectId || it.projectId2 == projectId }

    fun updateSimilarityEstado(id: Int, estado: String) {
        val index = similarityList.indexOfFirst { it.id == id }
        if (index != -1) {
            similarityList[index] = similarityList[index].copy(estado = estado)
        }
    }

    // Notifications
    private val notificationList = mutableListOf(
        Notification(id = 1, mensaje = "Proyecto 'Sistema de Ventas' ha sido actualizado", tipo = NotificationType.INFO.value, userId = 1, projectId = 1, leido = false),
        Notification(id = 2, mensaje = "Nuevo reporte de bug en 'Sistema de Ventas'", tipo = NotificationType.WARNING.value, userId = 1, projectId = 1, leido = false),
        Notification(id = 3, mensaje = "Similitud detectada entre proyectos", tipo = NotificationType.WARNING.value, userId = 1, leido = false),
        Notification(id = 4, mensaje = "Proyecto 'Plataforma E-learning' completado", tipo = NotificationType.SUCCESS.value, userId = 1, projectId = 2, leido = true),
        Notification(id = 5, mensaje = "Bienvenido a ProyecTwin", tipo = NotificationType.INFO.value, userId = 2, leido = false),
        Notification(id = 6, mensaje = "Tu proyecto ha sido revisado", tipo = NotificationType.INFO.value, userId = 2, leido = true)
    )

    fun getNotificationsByUser(userId: Int): List<Notification> = notificationList.filter { it.userId == userId }
    fun getUnreadNotificationsByUser(userId: Int): List<Notification> = getNotificationsByUser(userId).filter { !it.leido }
    fun markNotificationAsRead(notificationId: Int) {
        val index = notificationList.indexOfFirst { it.id == notificationId }
        if (index != -1) {
            notificationList[index] = notificationList[index].copy(leido = true)
        }
    }
}
