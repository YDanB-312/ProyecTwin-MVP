package com.example.proyectwin.data.mapper

import com.example.proyectwin.data.api.dto.ApprenticeDto
import com.example.proyectwin.data.api.dto.AuditLogDto
import com.example.proyectwin.data.api.dto.BugReportCreateRequest
import com.example.proyectwin.data.api.dto.BugReportDto
import com.example.proyectwin.data.api.dto.BugReportUpdateRequest
import com.example.proyectwin.data.api.dto.ClassGroupDto
import com.example.proyectwin.data.api.dto.CommentDto
import com.example.proyectwin.data.api.dto.DetallesSimilitud
import com.example.proyectwin.data.api.dto.GeneralUserDto
import com.example.proyectwin.data.api.dto.InstructorDto
import com.example.proyectwin.data.api.dto.KnowledgeNetworkDto
import com.example.proyectwin.data.api.dto.MotorConfigDto
import com.example.proyectwin.data.api.dto.NotificationDto
import com.example.proyectwin.data.api.dto.NotificationRequest
import com.example.proyectwin.data.api.dto.PasajeSimilitud
import com.example.proyectwin.data.api.dto.ProjectCreateRequest
import com.example.proyectwin.data.api.dto.ProjectDto
import com.example.proyectwin.data.api.dto.ProjectUpdateRequest
import com.example.proyectwin.data.api.dto.PublicResumenDto
import com.example.proyectwin.data.api.dto.SimilarityDto
import com.example.proyectwin.data.api.dto.TrainingProgramDto
import com.example.proyectwin.data.api.dto.UserUpdateRequest
import com.example.proyectwin.data.model.AuditLog
import com.example.proyectwin.data.model.BugReport
import com.example.proyectwin.data.model.Comment
import com.example.proyectwin.data.model.DetalleSimilitud
import com.example.proyectwin.data.model.DemoCoincidencia
import com.example.proyectwin.data.model.DemoResultado
import com.example.proyectwin.data.model.Ficha
import com.example.proyectwin.data.model.GeneralUser
import com.example.proyectwin.data.model.KnowledgeNetwork
import com.example.proyectwin.data.model.MotorConfig
import com.example.proyectwin.data.model.Notification
import com.example.proyectwin.data.model.Project
import com.example.proyectwin.data.model.ProjectDraft
import com.example.proyectwin.data.model.ProjectStatus
import com.example.proyectwin.data.model.ResumenPublico
import com.example.proyectwin.data.model.Similarity
import com.example.proyectwin.data.model.TrainingProgram
import com.example.proyectwin.data.model.UserRole
import java.time.LocalDate

/** Fecha de hoy en formato `yyyy-MM-dd` (lo exigen `fecha` en reportes/notificaciones). */
fun hoy(): String = LocalDate.now().toString()

// ------------------------------------------------------------ Usuarios

fun GeneralUserDto.nombreCompleto(): String = "$nombre $apellido".trim()

fun GeneralUserDto.toDomain(): GeneralUser = GeneralUser(
    id = id,
    name = nombreCompleto().ifEmpty { correo },
    email = correo,
    role = rol,
    fotoPerfil = fotoUrl,
    fichaId = apprentice?.idClassGroup,
    nombre = nombre,
    apellido = apellido,
    estado = estado,
)

/** Usuario anidado en otra relación: conserva el vínculo con su ficha si lo trae. */
fun GeneralUserDto.toDomain(fichaId: Int?): GeneralUser = toDomain().copy(
    fichaId = fichaId ?: apprentice?.idClassGroup,
)

fun GeneralUser.toUpdateRequest(password: String? = null): UserUpdateRequest = UserUpdateRequest(
    nombre = nombre ?: name.substringBefore(' ').ifEmpty { name },
    apellido = apellido ?: name.substringAfter(' ', "").ifEmpty { nombre ?: name },
    correo = email,
    rol = role,
    estado = estado,
    password = password,
    fotoUrl = fotoPerfil,
)

/** Divide un nombre completo en (nombre, apellido) como fallback de cuentas antiguas. */
fun dividirNombre(name: String): Pair<String, String> {
    val limpio = name.trim()
    val nombre = limpio.substringBefore(' ').ifEmpty { limpio }
    val apellido = limpio.substringAfter(' ', "").trim().ifEmpty { nombre }
    return nombre to apellido
}

fun InstructorDto.nombreUsuario(): String? = generalUser?.nombreCompleto()

fun ApprenticeDto.toGeneralUser(): GeneralUser? =
    generalUser?.toDomain(idClassGroup)

// ------------------------------------------------------------ Fichas

fun ClassGroupDto.toDomain(): Ficha = Ficha(
    id = id,
    codigo = codigo,
    programa = program?.nombre ?: "",
    estado = estado,
    instructorId = idInstructor.takeIf { it != 0 },
    instructorName = instructor?.nombreUsuario(),
    createdAt = createdAt,
    estudiantes = apprentices?.mapNotNull { it.toGeneralUser() } ?: emptyList(),
    nombre = nombre,
    numero = numero,
    idPrograma = idPrograma.takeIf { it != 0 },
)

// ------------------------------------------------------------ Catálogos

fun KnowledgeNetworkDto.toDomain(): KnowledgeNetwork = KnowledgeNetwork(
    id = id,
    nombre = nombre,
)

fun TrainingProgramDto.toDomain(): TrainingProgram = TrainingProgram(
    id = id,
    nombre = nombre,
    nivel = nivel,
    numTrimestres = numTrimestres,
    redId = knowledgeNetworkId.takeIf { it != 0 },
    redNombre = knowledgeNetwork?.nombre,
)

// ------------------------------------------------------------ Propuestas

fun ProjectDto.toDomain(): Project = Project(
    id = id,
    title = titulo,
    description = resumen,
    estado = estado,
    studentId = idCreador.takeIf { it != 0 },
    instructorId = idInstructorAsignado,
    fichaId = idClassGroup,
    createdAt = createdAt,
    updatedAt = updatedAt,
    studentName = creator?.nombreCompleto() ?: apprentices?.firstNotNullOfOrNull { it.toGeneralUser()?.name },
    instructorName = instructor?.nombreUsuario(),
    palabrasClave = palabrasClave,
    areaAplicacion = areaAplicacion,
    objetivoGeneral = objetivoGeneral,
    objetivosEspecificos = objetivosEspecificos ?: emptyList(),
    programa = classGroup?.program?.nombre,
    equipo = apprentices?.mapNotNull { it.toGeneralUser() } ?: emptyList(),
)

fun Project.toDraft(): ProjectDraft = ProjectDraft(
    titulo = title,
    resumen = description,
    palabrasClave = palabrasClave,
    areaAplicacion = areaAplicacion,
    objetivoGeneral = objetivoGeneral,
    objetivosEspecificos = objetivosEspecificos,
    estado = estado,
    idCreador = studentId ?: 0,
    idInstructorAsignado = instructorId,
    idClassGroup = fichaId,
)

fun ProjectDraft.toCreateRequest(): ProjectCreateRequest = ProjectCreateRequest(
    titulo = titulo,
    resumen = resumen,
    palabrasClave = palabrasClave,
    areaAplicacion = areaAplicacion,
    objetivoGeneral = objetivoGeneral,
    objetivosEspecificos = objetivosEspecificos,
    estado = estado,
    idCreador = idCreador,
    idInstructorAsignado = idInstructorAsignado,
    idClassGroup = idClassGroup,
)

fun ProjectDraft.toUpdateRequest(): ProjectUpdateRequest = ProjectUpdateRequest(
    titulo = titulo,
    resumen = resumen,
    palabrasClave = palabrasClave,
    areaAplicacion = areaAplicacion,
    objetivoGeneral = objetivoGeneral,
    objetivosEspecificos = objetivosEspecificos,
    estado = estado,
    idCreador = idCreador,
    idInstructorAsignado = idInstructorAsignado,
    idClassGroup = idClassGroup,
)

// ------------------------------------------------------------ Similitudes

fun DetallesSimilitud.toDomain(): DetalleSimilitud = DetalleSimilitud(
    palabras = palabras,
    caracteres = caracteres,
    tema = tema,
    cobertura = cobertura,
    terminos = terminos,
    pasajes = pasajes.map { it.toDomain() },
)

fun PasajeSimilitud.toDomain(): com.example.proyectwin.data.model.PasajeSimilitud =
    com.example.proyectwin.data.model.PasajeSimilitud(a = a, b = b, score = score)

fun SimilarityDto.toDomain(): Similarity {
    val creador1 = project1?.creator?.nombreCompleto()
    val creador2 = project2?.creator?.nombreCompleto()
    return Similarity(
        id = id,
        projectId1 = idProyecto1,
        projectId2 = idProyecto2,
        project1Title = project1?.titulo,
        project2Title = project2?.titulo,
        project1Student = creador1 ?: project1?.apprentices?.firstNotNullOfOrNull { it.toGeneralUser()?.name },
        project2Student = creador2 ?: project2?.apprentices?.firstNotNullOfOrNull { it.toGeneralUser()?.name },
        // El backend guarda porcentaje 0–100; el dominio usa fracción 0–1.
        similitud = porcentaje / 100.0,
        createdAt = createdAt,
        fecha = fecha,
        detalles = detalles?.toDomain(),
    )
}

// ------------------------------------------------------------ Notificaciones

fun NotificationDto.toDomain(): Notification = Notification(
    id = id,
    mensaje = titulo.ifEmpty { descripcion ?: "" },
    tipo = tipo,
    userId = idUsuario.takeIf { it != 0 },
    leido = leida,
    createdAt = fecha ?: createdAt,
    titulo = titulo,
    descripcion = descripcion,
    enlace = enlace,
    fecha = fecha,
)

/** PUT completo como el frontend: conserva todos los campos y cambia `leida`. */
fun Notification.toRequest(leida: Boolean = leido): NotificationRequest = NotificationRequest(
    titulo = titulo ?: mensaje,
    descripcion = descripcion,
    tipo = tipo,
    enlace = enlace,
    leida = leida,
    fecha = fecha ?: createdAt?.take(10) ?: hoy(),
    idUsuario = userId ?: 0,
)

// ------------------------------------------------------------ Reportes de falla

fun BugReportDto.toDomain(): BugReport = BugReport(
    id = id,
    titulo = titulo ?: "",
    descripcion = descripcion,
    tipo = tipo,
    estado = estado,
    projectId = null,
    reporterId = idUsuario.takeIf { it != 0 },
    reporterName = generalUser?.nombreCompleto(),
    createdAt = createdAt,
    updatedAt = updatedAt,
    fecha = fecha,
)

fun BugReport.toCreateRequest(idUsuario: Int?, fecha: String = hoy()): BugReportCreateRequest =
    BugReportCreateRequest(
        titulo = titulo.ifEmpty { null },
        descripcion = descripcion,
        tipo = tipo,
        estado = estado,
        fecha = fecha,
        idUsuario = idUsuario,
    )

fun BugReport.toUpdateRequest(fecha: String, idUsuario: Int): BugReportUpdateRequest =
    BugReportUpdateRequest(
        titulo = titulo.ifEmpty { null },
        descripcion = descripcion,
        tipo = tipo,
        estado = estado,
        fecha = fecha,
        idUsuario = idUsuario,
    )

// ------------------------------------------------------------ Observaciones

fun CommentDto.toDomain(): Comment = Comment(
    id = id,
    texto = texto,
    proyectoId = idProyecto,
    usuarioId = idUsuario,
    respuestaA = respuestaA,
    autorNombre = user?.nombreCompleto(),
    autorFoto = user?.fotoUrl,
    createdAt = createdAt,
    updatedAt = updatedAt,
    respuestas = replies?.map { it.toDomain() } ?: emptyList(),
)

// ------------------------------------------------------------ Motor y demos

fun MotorConfigDto.toDomain(): MotorConfig = MotorConfig(id = id, umbral = umbral, meses = meses)

fun PublicResumenDto.toDomain(): ResumenPublico = ResumenPublico(
    umbral = umbral,
    meses = meses,
    totalPropuestas = totalPropuestas,
    totalProgramas = totalProgramas,
)

fun com.example.proyectwin.data.api.dto.DemoSimilitudResponse.toDomain(): DemoResultado =
    DemoResultado(
        umbral = umbral,
        meses = meses,
        total = total,
        sobre = sobre,
        coincidencias = coincidencias.map { DemoCoincidencia(it.id, it.titulo, it.porcentaje) },
    )

// ------------------------------------------------------------ Bitácora

fun AuditLogDto.toDomain(): AuditLog = AuditLog(
    id = id,
    accion = accion,
    entidad = entidad,
    entidadId = entidadId,
    usuarioId = idUsuario,
    usuarioNombre = user?.nombreCompleto(),
    detalle = detalle?.toString(),
    ip = ip,
    createdAt = createdAt,
)

// ------------------------------------------------------------ Sesión

/** Usuario de login + token: lo que se persiste en la sesión local. */
fun com.example.proyectwin.data.api.dto.LoginResponse.toSessionUser(): GeneralUser =
    user.toDomain().copy(token = token)
