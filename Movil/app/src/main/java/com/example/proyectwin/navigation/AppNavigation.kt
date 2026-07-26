package com.example.proyectwin.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.example.proyectwin.ui.screens.AlertsScreen
import com.example.proyectwin.ui.screens.EditProfileScreen
import com.example.proyectwin.ui.screens.FichaDetailScreen
import com.example.proyectwin.ui.screens.ReportIssueScreen
import com.example.proyectwin.ui.screens.admin.AdminDashboardScreen
import com.example.proyectwin.ui.screens.admin.AdminNotificacionesScreen
import com.example.proyectwin.ui.screens.admin.AdminProfileScreen
import com.example.proyectwin.ui.screens.admin.AdminProjectDetailScreen
import com.example.proyectwin.ui.screens.admin.AdminProjectsScreen
import com.example.proyectwin.ui.screens.admin.AdminSimilarityDetailScreen
import com.example.proyectwin.ui.screens.admin.AdminSimilarityListScreen
import com.example.proyectwin.ui.screens.admin.BugReportDetailScreen
import com.example.proyectwin.ui.screens.admin.BugReportsScreen
import com.example.proyectwin.ui.screens.admin.NewUserScreen
import com.example.proyectwin.ui.screens.admin.UserDetailScreen
import com.example.proyectwin.ui.screens.admin.UserManagementScreen
import com.example.proyectwin.ui.screens.aprendiz.AnalysisResultScreen
import com.example.proyectwin.ui.screens.aprendiz.AnalyzingProjectScreen
import com.example.proyectwin.ui.screens.aprendiz.DashboardScreen
import com.example.proyectwin.ui.screens.aprendiz.NewProjectScreen
import com.example.proyectwin.ui.screens.aprendiz.ProfileScreen
import com.example.proyectwin.ui.screens.aprendiz.ProjectDetailScreen
import com.example.proyectwin.ui.screens.aprendiz.ProjectsScreen
import com.example.proyectwin.ui.screens.aprendiz.SimilarityDetailScreen
import com.example.proyectwin.ui.screens.aprendiz.UnirseFichaAprendizScreen
import com.example.proyectwin.ui.screens.auth.ConfirmationScreen
import com.example.proyectwin.ui.screens.auth.ForgotPasswordScreen
import com.example.proyectwin.ui.screens.auth.HomeScreen
import com.example.proyectwin.ui.screens.auth.LoginScreen
import com.example.proyectwin.ui.screens.auth.NotFoundScreen
import com.example.proyectwin.ui.screens.auth.RegisterScreen
import com.example.proyectwin.ui.screens.auth.ResetPasswordScreen
import com.example.proyectwin.ui.screens.instructor.DetalleFichaInstructorScreen
import com.example.proyectwin.ui.screens.instructor.DetalleProyectoInstructorScreen
import com.example.proyectwin.ui.screens.instructor.FichaDirectoryScreen
import com.example.proyectwin.ui.screens.instructor.InstructorDashboardScreen
import com.example.proyectwin.ui.screens.instructor.InstructorProfileScreen
import com.example.proyectwin.ui.screens.instructor.InstructorSimilarityDetailScreen
import com.example.proyectwin.ui.screens.instructor.JoinFichaScreen
import com.example.proyectwin.ui.screens.instructor.ManageFichasScreen
import com.example.proyectwin.ui.screens.instructor.RevisionPropuestasScreen

object AppNavigation {
    // Auth Graph
    const val AUTH_GRAPH = "auth_graph"
    const val HOME = "home"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val RESET_PASSWORD = "reset_password"
    const val CONFIRMATION = "confirmation"
    const val NOT_FOUND = "not_found"

    // Aprendiz Graph
    const val APRENDIZ_GRAPH = "aprendiz_graph"
    const val APRENDIZ_DASHBOARD = "aprendiz_dashboard"
    const val APRENDIZ_PROJECTS = "aprendiz_projects"
    const val APRENDIZ_NEW_PROJECT = "aprendiz_new_project"
    const val APRENDIZ_ANALYZING = "aprendiz_analyzing"
    const val APRENDIZ_ANALYSIS_RESULT = "aprendiz_analysis_result"
    const val APRENDIZ_DETAIL = "aprendiz_detail/{projectId}"
    const val APRENDIZ_SIMILARITY = "aprendiz_similarity"
    const val APRENDIZ_PROFILE = "aprendiz_profile"
    const val APRENDIZ_ALERTS = "aprendiz/alerts"
    const val APRENDIZ_FICHA_DETAIL = "aprendiz/ficha"
    const val APRENDIZ_JOIN_FICHA = "aprendiz/join-ficha"

    // Instructor Graph
    const val INSTRUCTOR_GRAPH = "instructor_graph"
    const val INSTRUCTOR_DASHBOARD = "instructor_dashboard"
    const val INSTRUCTOR_FICHAS = "instructor_fichas"
    const val INSTRUCTOR_JOIN_FICHA = "instructor_join_ficha"
    const val INSTRUCTOR_REVISION = "instructor/revision"
    const val INSTRUCTOR_DETAIL = "instructor_detail/{projectId}"
    const val INSTRUCTOR_PROFILE = "instructor/profile"
    const val INSTRUCTOR_SIMILARITY_DETAIL = "instructor/similarity-detail"
    const val INSTRUCTOR_MANAGE_FICHAS = "instructor/fichas/manage"
    const val INSTRUCTOR_ALERTS = "instructor/alerts"
    const val INSTRUCTOR_FICHA_DETAIL = "instructor_ficha_detail"

    // Admin Graph
    const val ADMIN_GRAPH = "admin_graph"
    const val ADMIN_DASHBOARD = "admin_dashboard"
    const val ADMIN_USERS = "admin/users"
    const val ADMIN_BUGS = "admin/bugs"
    const val ADMIN_PROJECTS = "admin/projects"
    const val ADMIN_PROJECT_DETAIL = "admin/project/{projectId}"
    const val ADMIN_SIMILARITY_LIST = "admin/similarities"
    const val ADMIN_SIMILARITY_DETAIL = "admin/similarity/detail"
    const val ADMIN_NEW_USER = "admin/users/new"
    const val ADMIN_USER_DETAIL = "admin/user/{userId}"
    const val ADMIN_PROFILE = "admin/profile"
    const val ADMIN_BUG_DETAIL = "admin/bug/{bugId}"
    const val ADMIN_ALERTS = "admin/alerts"
    const val ADMIN_NOTIFICACIONES = "admin/notificaciones"

    // Shared screens
    const val EDIT_PROFILE = "edit_profile"
    const val REPORT_ISSUE = "report_issue"

    fun NavGraphBuilder.authGraph(navController: NavHostController) {
        navigation(startDestination = HOME, route = AUTH_GRAPH) {
            composable(HOME) { HomeScreen(
                onLoginClick = { navController.navigate(LOGIN) },
                onRegisterClick = { navController.navigate(REGISTER) },
            ) }
            composable(LOGIN) { LoginScreen(
                onLoginSuccess = {
                    navController.navigate(APRENDIZ_DASHBOARD) { popUpTo(AUTH_GRAPH) { inclusive = true } }
                },
                onRegisterClick = { navController.navigate(REGISTER) },
                onForgotPasswordClick = { navController.navigate(FORGOT_PASSWORD) }
            ) }
            composable(REGISTER) { RegisterScreen(
                onBackToLogin = { navController.popBackStack() },
                onRegisterSuccess = { navController.navigate(LOGIN) }
            ) }
            composable(FORGOT_PASSWORD) { ForgotPasswordScreen(
                onBackToLogin = { navController.popBackStack() },
                onPasswordReset = { navController.navigate(CONFIRMATION) }
            ) }
            composable(RESET_PASSWORD) { ResetPasswordScreen(
                onBackToLogin = { navController.popBackStack() },
                onResetSuccess = { navController.navigate(LOGIN) { popUpTo(LOGIN) { inclusive = true } } }
            ) }
            composable(CONFIRMATION) { ConfirmationScreen(
                onGoToLogin = { navController.navigate(LOGIN) { popUpTo(AUTH_GRAPH) { inclusive = true } } }
            ) }
            composable(NOT_FOUND) { NotFoundScreen(
                onGoHome = { navController.navigate(HOME) { popUpTo(AUTH_GRAPH) { inclusive = true } } },
                onGoLogin = { navController.navigate(LOGIN) }
            ) }
        }
    }

    fun NavGraphBuilder.aprendizGraph(navController: NavHostController) {
        navigation(startDestination = APRENDIZ_DASHBOARD, route = APRENDIZ_GRAPH) {
            composable(APRENDIZ_DASHBOARD) { DashboardScreen(onNavigate = { route -> navController.navigate(route) }) }
            composable(APRENDIZ_PROJECTS) { ProjectsScreen(
                onNavigate = { route -> navController.navigate(route) },
                onNewProject = { navController.navigate(APRENDIZ_NEW_PROJECT) },
                onProjectDetail = { id -> navController.navigate("aprendiz_detail/$id") }
            ) }
            composable(APRENDIZ_NEW_PROJECT) { NewProjectScreen(
                onBack = { navController.popBackStack() },
                onSubmit = { navController.navigate(APRENDIZ_ANALYZING) }
            ) }
            composable(APRENDIZ_ANALYZING) { AnalyzingProjectScreen(
                onCancel = { navController.popBackStack(APRENDIZ_NEW_PROJECT, inclusive = true) },
                onAnalysisComplete = { navController.navigate(APRENDIZ_ANALYSIS_RESULT) { popUpTo(APRENDIZ_ANALYZING) { inclusive = true } } }
            ) }
            composable(APRENDIZ_ANALYSIS_RESULT) { AnalysisResultScreen(
                onBack = { navController.popBackStack() },
                onViewDetail = { _ -> navController.navigate(APRENDIZ_SIMILARITY) }
            ) }
            composable(
                route = APRENDIZ_DETAIL,
                arguments = listOf(navArgument("projectId") { type = NavType.StringType })
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
                ProjectDetailScreen(
                    projectId = projectId, 
                    onBack = { navController.popBackStack() },
                    onNavigate = { route -> navController.navigate(route) }
                )
            }
            composable(APRENDIZ_SIMILARITY) { SimilarityDetailScreen(onBack = { navController.popBackStack() }) }
            composable(APRENDIZ_PROFILE) { ProfileScreen(onBack = { navController.popBackStack() }, onNavigate = { route -> navController.navigate(route) }) }
            composable(APRENDIZ_ALERTS) { AlertsScreen(
                onNavigate = { route -> navController.navigate(route) },
                onBack = { navController.popBackStack() },
                profileRoute = APRENDIZ_PROFILE,
                similarityRoute = APRENDIZ_SIMILARITY,
                detailRoute = "aprendiz_detail/{id}"
            ) }
            composable(APRENDIZ_FICHA_DETAIL) { FichaDetailScreen(onBack = { navController.popBackStack() }, onNavigate = { route -> navController.navigate(route) }) }
            composable(APRENDIZ_JOIN_FICHA) { UnirseFichaAprendizScreen(onBack = { navController.popBackStack() }, onJoined = { navController.popBackStack() }) }
            composable(EDIT_PROFILE) { EditProfileScreen(onBack = { navController.popBackStack() }, onNavigate = { route -> navController.navigate(route) }) }
            composable(REPORT_ISSUE) { ReportIssueScreen(onBack = { navController.popBackStack() }, onNavigate = { route -> navController.navigate(route) }) }
        }
    }

    fun NavGraphBuilder.instructorGraph(navController: NavHostController) {
        navigation(startDestination = INSTRUCTOR_DASHBOARD, route = INSTRUCTOR_GRAPH) {
            composable(INSTRUCTOR_DASHBOARD) { InstructorDashboardScreen(onNavigate = { route -> navController.navigate(route) }) }
            composable(INSTRUCTOR_FICHAS) { FichaDirectoryScreen(
                onBack = { navController.popBackStack() },
                onCreateFicha = { navController.navigate(INSTRUCTOR_JOIN_FICHA) },
                onNavigate = { route -> navController.navigate(route) }
            ) }
            composable(INSTRUCTOR_JOIN_FICHA) { JoinFichaScreen(
                onBack = { navController.popBackStack() },
                onFichaCreated = { navController.navigate(INSTRUCTOR_FICHAS) }
            ) }
            composable(INSTRUCTOR_REVISION) { RevisionPropuestasScreen(
                onBack = { navController.popBackStack() },
                onProjectDetail = { id -> navController.navigate("instructor_detail/$id") }
            ) }
            composable(
                route = INSTRUCTOR_DETAIL,
                arguments = listOf(navArgument("projectId") { type = NavType.StringType })
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
                DetalleProyectoInstructorScreen(
                    projectId = projectId, 
                    onBack = { navController.popBackStack() }
                )
            }
            composable(INSTRUCTOR_PROFILE) { InstructorProfileScreen(
                onBack = { navController.popBackStack() },
                onNavigate = { route -> navController.navigate(route) }
            ) }
            composable(INSTRUCTOR_SIMILARITY_DETAIL) { InstructorSimilarityDetailScreen(
                onBack = { navController.popBackStack() },
                onNavigate = { route -> navController.navigate(route) }
            ) }
            composable(INSTRUCTOR_MANAGE_FICHAS) { ManageFichasScreen(
                onBack = { navController.popBackStack() },
                onViewDetail = { id -> navController.navigate(INSTRUCTOR_FICHA_DETAIL) },
                onViewDirectory = { id -> navController.navigate(INSTRUCTOR_FICHAS) },
                onCreateFicha = { navController.navigate(INSTRUCTOR_JOIN_FICHA) },
                onNavigate = { route -> navController.navigate(route) }
            ) }
            composable(INSTRUCTOR_ALERTS) { AlertsScreen(
                onNavigate = { route -> navController.navigate(route) },
                onBack = { navController.popBackStack() },
                profileRoute = INSTRUCTOR_PROFILE,
                similarityRoute = INSTRUCTOR_SIMILARITY_DETAIL,
                detailRoute = "instructor_detail/{id}"
            ) }
            composable(INSTRUCTOR_FICHA_DETAIL) { DetalleFichaInstructorScreen(
                onBack = { navController.popBackStack() },
                onNavigate = { route -> navController.navigate(route) }
            ) }
            composable(EDIT_PROFILE) { EditProfileScreen(onBack = { navController.popBackStack() }, onNavigate = { route -> navController.navigate(route) }) }
            composable(REPORT_ISSUE) { ReportIssueScreen(onBack = { navController.popBackStack() }, onNavigate = { route -> navController.navigate(route) }) }
        }
    }

    fun NavGraphBuilder.adminGraph(navController: NavHostController) {
        navigation(startDestination = ADMIN_DASHBOARD, route = ADMIN_GRAPH) {
            composable(ADMIN_DASHBOARD) { AdminDashboardScreen(onNavigate = { route -> navController.navigate(route) }) }
            composable(ADMIN_USERS) { UserManagementScreen(
                onBack = { navController.popBackStack() },
                onNavigate = { route -> navController.navigate(route) }
            ) }
            composable(ADMIN_BUGS) { BugReportsScreen(
                onBack = { navController.popBackStack() },
                onNavigate = { route -> navController.navigate(route) }
            ) }
            composable(ADMIN_PROJECTS) { AdminProjectsScreen(
                onBack = { navController.popBackStack() },
                onNavigate = { route -> navController.navigate(route) }
            ) }
            composable(
                route = ADMIN_PROJECT_DETAIL,
                arguments = listOf(navArgument("projectId") { type = NavType.StringType })
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
                AdminProjectDetailScreen(
                    projectId = projectId, 
                    onBack = { navController.popBackStack() },
                    onNavigate = { route -> navController.navigate(route) }
                )
            }
            composable(ADMIN_SIMILARITY_LIST) { AdminSimilarityListScreen(
                onBack = { navController.popBackStack() },
                onNavigate = { route -> navController.navigate(route) }
            ) }
            composable(ADMIN_SIMILARITY_DETAIL) { AdminSimilarityDetailScreen(
                onBack = { navController.popBackStack() }
            ) }
            composable(ADMIN_NEW_USER) { NewUserScreen(onBack = { navController.popBackStack() }) }
            composable(
                route = ADMIN_USER_DETAIL,
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                UserDetailScreen(
                    userId = userId, 
                    onBack = { navController.popBackStack() },
                    onNavigate = { route -> navController.navigate(route) }
                )
            }
            composable(ADMIN_PROFILE) { AdminProfileScreen(
                onBack = { navController.popBackStack() },
                onNavigate = { route -> navController.navigate(route) }
            ) }
            composable(
                route = ADMIN_BUG_DETAIL,
                arguments = listOf(navArgument("bugId") { type = NavType.StringType })
            ) { backStackEntry ->
                val bugId = backStackEntry.arguments?.getString("bugId") ?: ""
                BugReportDetailScreen(
                    bugId = bugId, 
                    onBack = { navController.popBackStack() }
                )
            }
            composable(ADMIN_ALERTS) { AlertsScreen(
                onNavigate = { route -> navController.navigate(route) },
                onBack = { navController.popBackStack() },
                profileRoute = ADMIN_PROFILE,
                similarityRoute = ADMIN_SIMILARITY_DETAIL,
                detailRoute = "admin/project/{id}"
            ) }
            composable(ADMIN_NOTIFICACIONES) { AdminNotificacionesScreen(
                onBack = { navController.popBackStack() },
                onNavigateToUser = { id -> navController.navigate(ADMIN_USER_DETAIL.replace("{userId}", id)) },
                onNavigateToReport = { id -> navController.navigate(ADMIN_BUG_DETAIL.replace("{bugId}", id)) },
                onNavigateToSimilarity = { navController.navigate(ADMIN_SIMILARITY_DETAIL) }
            ) }
            composable(EDIT_PROFILE) { EditProfileScreen(onBack = { navController.popBackStack() }, onNavigate = { route -> navController.navigate(route) }) }
            composable(REPORT_ISSUE) { ReportIssueScreen(onBack = { navController.popBackStack() }, onNavigate = { route -> navController.navigate(route) }) }
        }
    }
}
