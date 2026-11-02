package com.example.proyectwin.data.api

import kotlinx.serialization.SerializationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

@Serializable
private data class ErrorPayload(
    val message: String? = null,
    val code: String? = null,
    val errors: Map<String, List<String>>? = null,
    @SerialName("must_change_password")
    val mustChangePassword: Boolean? = null,
    @SerialName("requiere_confirmacion")
    val requiereConfirmacion: Boolean? = null,
    @SerialName("puede_desactivar")
    val puedeDesactivar: Boolean? = null,
    @SerialName("confirmacion_hash")
    val confirmacionHash: String? = null,
    val impacto: JsonObject? = null,
)

private val errorJson = Json { ignoreUnknownKeys = true }

/**
 * Mensaje exacto con el que el backend rechaza una cuenta suspendida
 * (middleware `cuenta.activa` y `AuthController::login`). El backend no envía
 * un flag para diferenciarlo de otros 403, así que se compara el mensaje.
 */
private const val MENSAJE_CUENTA_SUSPENDIDA = "Cuenta suspendida. Contacta al administrador."

/**
 * Errores de red tipados con mensajes en español, equivalentes a los `status`
 * del frontend web: `0` sin conexión o timeout, `4xx/5xx` HTTP.
 *
 * [fieldErrors] trae los mensajes de validación de Laravel (422)
 * mapeados a `{campo: primerMensaje}` para pintarlos en los formularios.
 */
sealed class ApiException(
    message: String,
    val status: Int = 0,
    cause: Throwable? = null,
) : Exception(message, cause) {

    class SinConexion(cause: Throwable) : ApiException(
        "No se pudo conectar con el servidor. Revisa tu conexión a internet.",
        0,
        cause,
    )

    class Timeout(cause: Throwable) : ApiException(
        "El servidor tardó demasiado en responder.",
        0,
        cause,
    )

    class Http(
        status: Int,
        message: String,
        val fieldErrors: Map<String, String> = emptyMap(),
        /** Código estable del backend; p. ej. `cuenta_no_activada`. */
        val code: String? = null,
        /** El backend bloquea la API hasta cambiar la contraseña temporal. */
        val mustChangePassword: Boolean = false,
        /** 403 del middleware `cuenta.activa`: la sesión ya no es válida. */
        val cuentaSuspendida: Boolean = false,
        val cuentaNoActivada: Boolean = false,
        val requiereConfirmacion: Boolean = false,
        val puedeDesactivar: Boolean = false,
        val confirmacionHash: String? = null,
        val impacto: Map<String, Int> = emptyMap(),
    ) : ApiException(message, status)

    class Formato(cause: Throwable) : ApiException(
        "El servidor devolvió una respuesta que no se pudo interpretar.",
        0,
        cause,
    )

    class Desconocido(message: String, cause: Throwable? = null) : ApiException(message, 0, cause)
}

/** Convierte cualquier throw de Retrofit/OkHttp en un [ApiException] tipado. */
fun Throwable.toApiException(): ApiException = when (this) {
    is ApiException -> this
    is SocketTimeoutException -> ApiException.Timeout(this)
    is IOException -> ApiException.SinConexion(this)
    is HttpException -> toApiException()
    is SerializationException -> ApiException.Formato(this)
    else -> ApiException.Desconocido(message ?: "Ocurrió un error inesperado.", this)
}

private fun HttpException.toApiException(): ApiException {
    val httpStatus = code()
    val body = runCatching { response()?.errorBody()?.string() }.getOrNull()
    val payload = body?.let { runCatching { errorJson.decodeFromString<ErrorPayload>(it) }.getOrNull() }

    val mensajeServidor = payload?.message
    val message = when {
        // El backend responde "Unauthenticated." en inglés; para el usuario se
        // muestra un mensaje claro de sesión vencida (como hace el frontend).
        httpStatus == 401 -> "Tu sesión expiró. Inicia sesión nuevamente."
        !mensajeServidor.isNullOrBlank() -> mensajeServidor
        else -> "Error del servidor ($httpStatus)."
    }
    val fieldErrors = payload?.errors
        .orEmpty()
        .mapValues { (_, mensajes) -> mensajes.firstOrNull().orEmpty() }
    val impacto = payload?.impacto
        .orEmpty()
        .mapValues { (_, valor) -> valor.jsonPrimitive.intOrNull ?: 0 }

    return ApiException.Http(
        status = httpStatus,
        message = message,
        fieldErrors = fieldErrors,
        code = payload?.code,
        mustChangePassword = payload?.mustChangePassword == true,
        // Solo este 403 invalida la sesión; los demás son permisos puntuales.
        cuentaSuspendida = httpStatus == 403 && mensajeServidor == MENSAJE_CUENTA_SUSPENDIDA,
        cuentaNoActivada = httpStatus == 403 && payload?.code == "cuenta_no_activada",
        requiereConfirmacion = payload?.requiereConfirmacion == true,
        puedeDesactivar = payload?.puedeDesactivar == true,
        confirmacionHash = payload?.confirmacionHash,
        impacto = impacto,
    )
}

/**
 * Ejecuta una llamada a la API envolviendo cualquier fallo en `Result.failure`
 * con un [ApiException]. Un 401 además dispara el evento global de sesión
 * expirada (la UI redirige al login, igual que en el frontend web).
 */
suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T> = try {
    Result.success(apiCall())
} catch (e: Throwable) {
    val error = e.toApiException()
    if (error is ApiException.Http) {
        when {
            error.status == 401 -> SessionEvents.notifyExpired()
            // Cuenta suspendida: la sesión ya no sirve; se cierra como un 401.
            error.cuentaSuspendida -> SessionEvents.notifyExpired()
            // 403 con la marca del middleware: la app debe ir al cambio obligatorio.
            error.status == 403 && error.mustChangePassword -> SessionEvents.notifyMustChangePassword()
        }
    }
    Result.failure(error)
}
