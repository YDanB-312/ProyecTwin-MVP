package com.example.proyectwin.di

import com.example.proyectwin.data.local.SessionManager
import com.example.proyectwin.domain.repository.NotificationsRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Acceso a dependencias desde composables que no son `@AndroidEntryPoint`
 * (p. ej. [com.example.proyectwin.ui.components.SenaTopBar], que vive fuera
 * del árbol de ViewModels).
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppEntryPoint {
    fun sessionManager(): SessionManager
    fun notificationsRepository(): NotificationsRepository
}
