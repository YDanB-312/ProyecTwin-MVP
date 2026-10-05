package com.example.proyectwin.data.api

import com.example.proyectwin.data.local.SessionManager
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Caché en memoria del token Bearer de Sanctum.
 *
 * El interceptor de OkHttp corre en un hilo de red y no puede suspender, por lo
 * que el token vive aquí: [SessionManager] es la fuente de verdad persistente
 * (DataStore) y este clase mantiene una copia sincronizada en las escrituras
 * de sesión (login/logout). En el arranque se hidrata de DataStore de forma
 * perezosa la primera vez que se necesita.
 */
@Singleton
class TokenProvider @Inject constructor(
    private val sessionManager: SessionManager,
) {

    @Volatile
    private var cached: String? = null

    val token: String?
        get() = cached ?: runBlocking {
            sessionManager.getToken()?.also { cached = it }
        }

    /** Sincroniza el token en memoria tras iniciar sesión (o cambiarlo). */
    fun update(token: String?) {
        cached = token
    }

    /** Precarga el token persistido al arrancar la aplicación. */
    suspend fun hydrate() {
        cached = sessionManager.getToken()
    }
}
