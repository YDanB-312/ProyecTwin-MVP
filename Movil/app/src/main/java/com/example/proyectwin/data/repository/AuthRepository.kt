package com.example.proyectwin.data.repository

import com.example.proyectwin.data.local.SessionManager
import com.example.proyectwin.data.mock.MockDataProvider
import com.example.proyectwin.data.model.GeneralUser
import kotlinx.coroutines.flow.Flow

class AuthRepository(private val sessionManager: SessionManager) {

    val currentUser: Flow<GeneralUser?> = sessionManager.currentUser
    val isLoggedIn: Flow<Boolean> = sessionManager.isLoggedIn

    suspend fun login(email: String, password: String): Result<GeneralUser> {
        val user = MockDataProvider.findUserByEmail(email)
        if (user != null && password == "123456") {
            sessionManager.saveUser(user)
            return Result.success(user)
        }
        return Result.failure(Exception("Credenciales inválidas"))
    }

    suspend fun register(name: String, email: String, password: String, role: String): Result<GeneralUser> {
        val existing = MockDataProvider.findUserByEmail(email)
        if (existing != null) {
            return Result.failure(Exception("El email ya está registrado"))
        }
        val newUser = GeneralUser(
            id = MockDataProvider.users.size + 1,
            name = name,
            email = email,
            role = role,
            token = "mock-token-${email.split("@")[0]}"
        )
        sessionManager.saveUser(newUser)
        return Result.success(newUser)
    }

    suspend fun logout() {
        sessionManager.clearSession()
    }

    suspend fun updateProfile(name: String, email: String, telefono: String?) {
        sessionManager.updateProfile(name, email, telefono)
    }

    suspend fun updateFoto(fotoBase64: String?) {
        sessionManager.updateFoto(fotoBase64)
    }

    suspend fun joinFicha(fichaId: Int) {
        sessionManager.joinFicha(fichaId)
    }
}
