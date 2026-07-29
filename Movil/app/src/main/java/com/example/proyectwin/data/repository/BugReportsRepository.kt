package com.example.proyectwin.data.repository

import com.example.proyectwin.data.mock.MockDataProvider
import com.example.proyectwin.data.model.BugReport
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class BugReportsRepository {

    fun getAllBugReports(): Flow<List<BugReport>> = flow {
        emit(MockDataProvider.getAllBugReports())
    }

    fun getBugReportsByProject(projectId: Int): Flow<List<BugReport>> = flow {
        emit(MockDataProvider.getBugReportsByProject(projectId))
    }
}
