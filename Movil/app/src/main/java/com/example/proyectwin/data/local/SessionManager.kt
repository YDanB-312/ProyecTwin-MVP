package com.example.proyectwin.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.proyectwin.data.model.GeneralUser
import com.example.proyectwin.data.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "proyectwin_session")

class SessionManager(private val context: Context) {

    companion object {
        private val KEY_USER_ID = intPreferencesKey("user_id")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_USER_ROLE = stringPreferencesKey("user_role")
        private val KEY_USER_TOKEN = stringPreferencesKey("user_token")
        private val KEY_USER_FOTO = stringPreferencesKey("user_foto")
        private val KEY_USER_TELEFONO = stringPreferencesKey("user_telefono")
        private val KEY_USER_FICHA_ID = intPreferencesKey("user_ficha_id")
        private val KEY_USER_DOCUMENTO = stringPreferencesKey("user_documento")
    }

    val currentUser: Flow<GeneralUser?> = context.dataStore.data.map { prefs ->
        val id = prefs[KEY_USER_ID] ?: return@map null
        GeneralUser(
            id = id,
            name = prefs[KEY_USER_NAME] ?: "",
            email = prefs[KEY_USER_EMAIL] ?: "",
            role = prefs[KEY_USER_ROLE] ?: UserRole.APRENDIZ.value,
            token = prefs[KEY_USER_TOKEN],
            fotoPerfil = prefs[KEY_USER_FOTO],
            telefono = prefs[KEY_USER_TELEFONO],
            fichaId = prefs[KEY_USER_FICHA_ID],
            documentoIdentidad = prefs[KEY_USER_DOCUMENTO]
        )
    }

    val isLoggedIn: Flow<Boolean> = currentUser.map { it != null }

    suspend fun saveUser(user: GeneralUser) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = user.id
            prefs[KEY_USER_NAME] = user.name
            prefs[KEY_USER_EMAIL] = user.email
            prefs[KEY_USER_ROLE] = user.role
            if (user.token != null) prefs[KEY_USER_TOKEN] = user.token
            if (user.fotoPerfil != null) prefs[KEY_USER_FOTO] = user.fotoPerfil
            if (user.telefono != null) prefs[KEY_USER_TELEFONO] = user.telefono
            if (user.fichaId != null) prefs[KEY_USER_FICHA_ID] = user.fichaId
            if (user.documentoIdentidad != null) prefs[KEY_USER_DOCUMENTO] = user.documentoIdentidad
        }
    }

    suspend fun updateFoto(fotoBase64: String?) {
        context.dataStore.edit { prefs ->
            if (fotoBase64 != null) prefs[KEY_USER_FOTO] = fotoBase64
            else prefs.remove(KEY_USER_FOTO)
        }
    }

    suspend fun updateProfile(name: String, email: String, telefono: String?) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_NAME] = name
            prefs[KEY_USER_EMAIL] = email
            if (telefono != null) prefs[KEY_USER_TELEFONO] = telefono
            else prefs.remove(KEY_USER_TELEFONO)
        }
    }

    suspend fun joinFicha(fichaId: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_FICHA_ID] = fichaId
        }
    }

    suspend fun getToken(): String? {
        return context.dataStore.data.first()[KEY_USER_TOKEN]
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}
