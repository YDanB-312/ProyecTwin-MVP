package com.example.proyectwin.data.repository

import com.example.proyectwin.data.mock.MockDataProvider
import com.example.proyectwin.data.model.Notification
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class NotificationsRepository {

    fun getNotificationsByUser(userId: Int): Flow<List<Notification>> = flow {
        emit(MockDataProvider.getNotificationsByUser(userId))
    }

    fun getUnreadNotificationsByUser(userId: Int): Flow<List<Notification>> = flow {
        emit(MockDataProvider.getUnreadNotificationsByUser(userId))
    }

    fun getUnreadCount(userId: Int): Flow<Int> = flow {
        emit(MockDataProvider.getUnreadNotificationsByUser(userId).size)
    }
}
