package com.example.proyectwin.data.repository

import com.example.proyectwin.data.mock.MockDataProvider
import com.example.proyectwin.data.model.BugReport
import com.example.proyectwin.data.model.Project
import com.example.proyectwin.data.model.Similarity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ProjectsRepository {

    fun getAllProjects(): Flow<List<Project>> = flow {
        emit(MockDataProvider.getAllProjects())
    }

    fun getProjectById(id: Int): Flow<Project?> = flow {
        emit(MockDataProvider.findProjectById(id))
    }

    fun getProjectsByStudent(studentId: Int): Flow<List<Project>> = flow {
        emit(MockDataProvider.getProjectsByStudent(studentId))
    }

    fun getProjectsByInstructor(instructorId: Int): Flow<List<Project>> = flow {
        emit(MockDataProvider.getProjectsByInstructor(instructorId))
    }

    fun getProjectsByFicha(fichaId: Int): Flow<List<Project>> = flow {
        emit(MockDataProvider.getProjectsByFicha(fichaId))
    }

    fun getBugReportsByProject(projectId: Int): Flow<List<BugReport>> = flow {
        emit(MockDataProvider.getBugReportsByProject(projectId))
    }

    fun getSimilaritiesByProject(projectId: Int): Flow<List<Similarity>> = flow {
        emit(MockDataProvider.getSimilaritiesByProject(projectId))
    }
}
