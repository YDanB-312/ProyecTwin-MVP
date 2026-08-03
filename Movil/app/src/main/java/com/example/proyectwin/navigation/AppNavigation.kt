package com.example.proyectwin.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.proyectwin.ui.components.*
import com.example.proyectwin.ui.screens.aprendiz.*
import com.example.proyectwin.ui.screens.instructor.*
import com.example.proyectwin.ui.screens.admin.*
import com.example.proyectwin.ui.screens.auth.*
import com.example.proyectwin.ui.screens.AlertsScreen
import com.example.proyectwin.ui.screens.EditProfileScreen
import com.example.proyectwin.ui.screens.FichaDetailScreen
import com.example.proyectwin.ui.screens.ReportIssueScreen

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
    const val APRENDIZ_NEW_PROJECT = "aprendiz_new_project/{projectId}"
    const val APRENDIZ_ANALYZING = "aprendiz_analyzing"
    const val APRENDIZ_ANALYSIS_RESULT = "aprendiz_analysis_result"
    const val APRENDIZ_DETAIL = "aprendiz_detail/{projectId}"
    const val APRENDIZ_SIMILARITY = "aprendiz_similarity/{projectId}"
    const val APRENDIZ_PROFILE = "aprendiz_profile"
    const val APRENDIZ_ALERTS = "aprendiz/alerts"
    const val APRENDIZ_FICHA_DETAIL = "aprendiz/ficha"
    const val APRENDIZ_JOIN_FICHA = "aprendiz/join-ficha"
    const val APRENDIZ_COMPANERO_DETAIL = "aprendiz/companero/{nombre}/{iniciales}/{estado}"

    // Instructor Graph
    const val INSTRUCTOR_GRAPH = "instructor_graph"
    const val INSTRUCTOR_DASHBOARD = "instructor_dashboard"
    const val INSTRUCTOR_FICHAS = "instructor_fichas"
    const val INSTRUCTOR_JOIN_FICHA = "instructor_join_ficha"
    const val INSTRUCTOR_CREAR_FICHA = "instructor_crear_ficha"
    const val INSTRUCTOR_REVISION = "instructor/revision"
    const val INSTRUCTOR_DETAIL = "instructor_detail/{projectId}"
    const val INSTRUCTOR_PROFILE = "instructor/profile"
    const val INSTRUCTOR_SIMILARITY_DETAIL = "instructor/similarity-detail/{projectId}"
    const val INSTRUCTOR_MANAGE_FICHAS = "instructor/fichas/manage"
    const val INSTRUCTOR_ALERTS = "instructor/alerts"
    const val INSTRUCTOR_FICHA_DETAIL = "instructor_ficha_detail/{fichaId}"

    // Admin Graph
    const val ADMIN_GRAPH = "admin_graph"
    const val ADMIN_DASHBOARD = "admin_dashboard"
    const val ADMIN_USERS = "admin/users"
    const val ADMIN_BUGS = "admin/bugs"
    const val ADMIN_PROJECTS = "admin/projects"
    const val ADMIN_PROJECT_DETAIL = "admin/project/{projectId}"
    const val ADMIN_SIMILARITY_LIST = "admin/similarities"
    const val ADMIN_SIMILARITY_DETAIL = "admin/similarity/{projectId}"
    const val ADMIN_NEW_USER = "admin/users/new"
    const val ADMIN_USER_DETAIL = "admin/user/{userId}"
    const val ADMIN_PROFILE = "admin/profile"
    const val ADMIN_BUG_DETAIL = "admin/bug/{bugId}"
    const val ADMIN_ALERTS = "admin/alerts"
    const val ADMIN_NOTIFICACIONES = "admin/notificaciones"

    // Shared screens
    const val EDIT_PROFILE = "edit_profile"
    const val REPORT_ISSUE = "report_issue"

    // Containers
    const val ADMIN_MAIN = "admin_main"

    val aprendizTabs = listOf(
        SenaBottomNavItem(APRENDIZ_DASHBOARD, "Inicio", Icons.Default.Home, Icons.Default.Home),
        SenaBottomNavItem(APRENDIZ_PROJECTS, "Proyectos", Icons.Default.Folder, Icons.Default.Folder),
        SenaBottomNavItem(APRENDIZ_ALERTS, "Alertas", Icons.Default.Notifications, Icons.Default.Notifications),
        SenaBottomNavItem(APRENDIZ_PROFILE, "Perfil", Icons.Default.Person, Icons.Default.Person)
    )

    val instructorTabs = listOf(
        SenaBottomNavItem(INSTRUCTOR_DASHBOARD, "Inicio", Icons.Default.Home, Icons.Default.Home),
        SenaBottomNavItem(INSTRUCTOR_REVISION, "Revisiones", Icons.AutoMirrored.Filled.List, Icons.AutoMirrored.Filled.List),
        SenaBottomNavItem(INSTRUCTOR_MANAGE_FICHAS, "Fichas", Icons.AutoMirrored.Filled.Assignment, Icons.AutoMirrored.Filled.Assignment),
        SenaBottomNavItem(INSTRUCTOR_PROFILE, "Perfil", Icons.Default.Person, Icons.Default.Person)
    )

    val adminTabs = listOf(
        SenaBottomNavItem(ADMIN_DASHBOARD, "Inicio", Icons.Default.Home, Icons.Default.Home),
        SenaBottomNavItem(ADMIN_USERS, "Usuarios", Icons.Default.People, Icons.Default.People),
        SenaBottomNavItem(ADMIN_SIMILARITY_LIST, "Similitud", Icons.Default.Compare, Icons.Default.Compare),
        SenaBottomNavItem(ADMIN_PROFILE, "Perfil", Icons.Default.Person, Icons.Default.Person)
    )

    fun NavGraphBuilder.authGraph(navController: NavHostController) {
        navigation(startDestination = HOME, route = AUTH_GRAPH) {
            composable(HOME) { HomeScreen(
                onLoginClick = { navController.navigate(LOGIN) },
                onRegisterClick = { navController.navigate(REGISTER) },
            ) }
            composable(LOGIN) { LoginScreen(
                onLoginSuccess = { role: String ->
                    val destination = when(role) {
                        "instructor" -> INSTRUCTOR_DASHBOARD
                        "admin" -> ADMIN_DASHBOARD
                        else -> APRENDIZ_DASHBOARD
                    }
                    navController.navigate(destination) { popUpTo(AUTH_GRAPH) { inclusive = true } }
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
        val bottomBar = @Composable {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            SenaBottomNavigationBar(
                currentRoute = currentRoute,
                onNavigate = { route -> 
                    navController.navigate(route) {
                        popUpTo(APRENDIZ_DASHBOARD) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                items = aprendizTabs
            )
        }

        navigation(startDestination = APRENDIZ_DASHBOARD, route = APRENDIZ_GRAPH) {
            composable(APRENDIZ_DASHBOARD) { 
                DashboardScreen(onNavigate = { route -> navController.navigateTo(route) }, bottomBar = { bottomBar() }) 
            }
            composable(APRENDIZ_PROJECTS) { ProjectsScreen(
                onNavigate = { route -> navController.navigateTo(route) },
                onNewProject = { navController.navigate(APRENDIZ_NEW_PROJECT.replace("{projectId}", "")) },
                onProjectDetail = { id -> navController.navigate("aprendiz_detail/$id") },
                bottomBar = { bottomBar() }
            ) }
            composable(
                route = APRENDIZ_NEW_PROJECT,
                arguments = listOf(navArgument("projectId") { type = NavType.StringType })
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
                NewProjectScreen(
                    projectId = projectId,
                    onBack = { navController.popBackStack() },
                    onSubmit = { navController.navigate(APRENDIZ_ANALYZING) }
                )
            }
            composable(APRENDIZ_ANALYZING) { AnalyzingProjectScreen(
                onCancel = { navController.popBackStack(APRENDIZ_NEW_PROJECT, inclusive = true) },
                onAnalysisComplete = { navController.navigate(APRENDIZ_ANALYSIS_RESULT) { popUpTo(APRENDIZ_ANALYZING) { inclusive = true } } }
            ) }
            composable(APRENDIZ_ANALYSIS_RESULT) { AnalysisResultScreen(
                onBack = { navController.popBackStack() },
                onViewDetail = { id -> navController.navigate(APRENDIZ_SIMILARITY.replace("{projectId}", id)) }
            ) }
            composable(
                route = APRENDIZ_DETAIL,
                arguments = listOf(navArgument("projectId") { type = NavType.StringType })
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
                ProjectDetailScreen(
                    projectId = projectId, 
                    onBack = { navController.popBackStack() },
                    onNavigate = { route -> navController.navigateTo(route) }
                )
            }
            composable(
                route = APRENDIZ_SIMILARITY,
                arguments = listOf(navArgument("projectId") { type = NavType.StringType })
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
                SimilarityDetailScreen(projectId = projectId, onBack = { navController.popBackStack() })
            }
            composable(APRENDIZ_PROFILE) { ProfileScreen(onBack = { navController.popBackStack() }, onNavigate = { route -> navController.navigateTo(route) }, bottomBar = { bottomBar() }) }
            composable(APRENDIZ_ALERTS) { AlertsScreen(
                onNavigate = { route -> navController.navigateTo(route) },
                onBack = { navController.popBackStack() },
                profileRoute = APRENDIZ_PROFILE,
                similarityRoute = APRENDIZ_SIMILARITY,
                detailRoute = "aprendiz_detail/{id}",
                bottomBar = { bottomBar() }
            ) }
            composable(APRENDIZ_FICHA_DETAIL) { FichaDetailScreen(onBack = { navController.popBackStack() }, onNavigate = { route -> navController.navigateTo(route) }) }
            composable(APRENDIZ_JOIN_FICHA) { UnirseFichaAprendizScreen(onBack = { navController.popBackStack() }, onJoined = { navController.popBackStack() }) }
            composable(
                route = APRENDIZ_COMPANERO_DETAIL,
                arguments = listOf(
                    navArgument("nombre") { type = NavType.StringType },
                    navArgument("iniciales") { type = NavType.StringType },
                    navArgument("estado") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val nombre = backStackEntry.arguments?.getString("nombre") ?: ""
                val iniciales = backStackEntry.arguments?.getString("iniciales") ?: ""
                val estado = backStackEntry.arguments?.getString("estado") ?: ""
                DetalleCompaneroScreen(
                    nombre = nombre,
                    iniciales = iniciales,
                    estado = estado,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(EDIT_PROFILE) { EditProfileScreen(onBack = { navController.popBackStack() }, onNavigate = { route -> navController.navigateTo(route) }) }
            composable(REPORT_ISSUE) { ReportIssueScreen(onBack = { navController.popBackStack() }, onNavigate = { route -> navController.navigateTo(route) }) }
        }
    }

    fun NavGraphBuilder.instructorGraph(navController: NavHostController) {
        val bottomBar = @Composable {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            SenaBottomNavigationBar(
                currentRoute = currentRoute,
                onNavigate = { route -> 
                    navController.navigate(route) {
                        popUpTo(INSTRUCTOR_DASHBOARD) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                items = instructorTabs
            )
        }

        navigation(startDestination = INSTRUCTOR_DASHBOARD, route = INSTRUCTOR_GRAPH) {
            composable(INSTRUCTOR_DASHBOARD) { InstructorDashboardScreen(onNavigate = { route -> navController.navigateTo(route) }, bottomBar = { bottomBar() }) }
            composable(INSTRUCTOR_FICHAS) { FichaDirectoryScreen(
                onBack = { navController.popBackStack() },
                onCreateFicha = { navController.navigate(INSTRUCTOR_CREAR_FICHA) },
                onNavigate = { route -> navController.navigateTo(route) }
            ) }
            composable(INSTRUCTOR_JOIN_FICHA) { JoinFichaScreen(
                onBack = { navController.popBackStack() },
                onFichaCreated = { navController.navigate(INSTRUCTOR_FICHAS) }
            ) }
            composable(INSTRUCTOR_CREAR_FICHA) { CrearFichaScreen(
                onBack = { navController.popBackStack() },
                onFichaCreated = { navController.popBackStack() }
            ) }
            composable(INSTRUCTOR_REVISION) { RevisionPropuestasScreen(
                onBack = { navController.popBackStack() },
                onProjectDetail = { id -> navController.navigate("instructor_detail/$id") },
                bottomBar = { bottomBar() }
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
                onNavigate = { route -> navController.navigateTo(route) },
                bottomBar = { bottomBar() }
            ) }
            composable(
                route = INSTRUCTOR_SIMILARITY_DETAIL,
                arguments = listOf(
                    navArgument("projectId") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                val similarityId = backStackEntry.arguments?.getString("projectId") ?: ""
                InstructorSimilarityDetailScreen(
                    similarityId = similarityId,
                    onBack = { navController.popBackStack() },
                    onNavigate = { route -> navController.navigateTo(route) }
                )
            }
            composable(INSTRUCTOR_MANAGE_FICHAS) { ManageFichasScreen(
                onBack = { navController.popBackStack() },
                onViewDetail = { fichaId -> navController.navigate(INSTRUCTOR_FICHA_DETAIL.replace("{fichaId}", fichaId)) },
                onViewDirectory = { fichaId -> navController.navigate(INSTRUCTOR_FICHAS) },
                onCreateFicha = { navController.navigate(INSTRUCTOR_CREAR_FICHA) },
                onNavigate = { route -> navController.navigateTo(route) },
                bottomBar = { bottomBar() }
            ) }
            composable(INSTRUCTOR_ALERTS) { AlertsScreen(
                onNavigate = { route -> navController.navigateTo(route) },
                onBack = { navController.popBackStack() },
                profileRoute = INSTRUCTOR_PROFILE,
                similarityRoute = INSTRUCTOR_SIMILARITY_DETAIL,
                detailRoute = "instructor_detail/{id}",
                bottomBar = { bottomBar() }
            ) }
            composable(
                route = INSTRUCTOR_FICHA_DETAIL,
                arguments = listOf(navArgument("fichaId") { type = NavType.StringType })
            ) { backStackEntry ->
                val fichaId = backStackEntry.arguments?.getString("fichaId") ?: ""
                DetalleFichaInstructorScreen(
                    fichaId = fichaId,
                    onBack = { navController.popBackStack() },
                    onNavigate = { route -> navController.navigateTo(route) }
                )
            }
            composable(EDIT_PROFILE) { EditProfileScreen(onBack = { navController.popBackStack() }, onNavigate = { route -> navController.navigateTo(route) }) }
            composable(REPORT_ISSUE) { ReportIssueScreen(onBack = { navController.popBackStack() }, onNavigate = { route -> navController.navigateTo(route) }) }
        }
    }

    fun NavGraphBuilder.adminGraph(navController: NavHostController) {
        val bottomBar = @Composable {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            SenaBottomNavigationBar(
                currentRoute = currentRoute,
                onNavigate = { route -> 
                    navController.navigate(route) {
                        popUpTo(ADMIN_DASHBOARD) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                items = adminTabs
            )
        }

        navigation(startDestination = ADMIN_DASHBOARD, route = ADMIN_GRAPH) {
            composable(ADMIN_DASHBOARD) { AdminDashboardScreen(onNavigate = { route -> navController.navigateTo(route) }, bottomBar = { bottomBar() }) }
            composable(ADMIN_USERS) { UserManagementScreen(
                onBack = { navController.popBackStack() },
                onNavigate = { route -> navController.navigateTo(route) },
                bottomBar = { bottomBar() }
            ) }
            composable(ADMIN_BUGS) { BugReportsScreen(
                onBack = { navController.popBackStack() },
                onNavigate = { route -> navController.navigateTo(route) }
            ) }
            composable(ADMIN_PROJECTS) { AdminProjectsScreen(
                onBack = { navController.popBackStack() },
                onNavigate = { route -> navController.navigateTo(route) }
            ) }
            composable(
                route = ADMIN_PROJECT_DETAIL,
                arguments = listOf(navArgument("projectId") { type = NavType.StringType })
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
                AdminProjectDetailScreen(
                    projectId = projectId, 
                    onBack = { navController.popBackStack() },
                    onNavigate = { route -> navController.navigateTo(route) }
                )
            }
            composable(ADMIN_SIMILARITY_LIST) { AdminSimilarityListScreen(
                onBack = { navController.popBackStack() },
                onNavigate = { route -> navController.navigateTo(route) },
                bottomBar = { bottomBar() }
            ) }
            composable(
                route = ADMIN_SIMILARITY_DETAIL,
                arguments = listOf(
                    navArgument("projectId") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                val similarityId = backStackEntry.arguments?.getString("projectId") ?: ""
                AdminSimilarityDetailScreen(
                    similarityId = similarityId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(ADMIN_NEW_USER) { NewUserScreen(onBack = { navController.popBackStack() }) }
            composable(
                route = ADMIN_USER_DETAIL,
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                UserDetailScreen(
                    userId = userId, 
                    onBack = { navController.popBackStack() },
                    onNavigate = { route -> navController.navigateTo(route) }
                )
            }
            composable(ADMIN_PROFILE) { AdminProfileScreen(
                onBack = { navController.popBackStack() },
                onNavigate = { route -> navController.navigateTo(route) },
                bottomBar = { bottomBar() }
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
                onNavigate = { route -> navController.navigateTo(route) },
                onBack = { navController.popBackStack() },
                profileRoute = ADMIN_PROFILE,
                similarityRoute = ADMIN_SIMILARITY_DETAIL,
                detailRoute = "admin/project/{id}",
                bottomBar = { bottomBar() }
            ) }
            composable(ADMIN_NOTIFICACIONES) { AdminNotificacionesScreen(
                onBack = { navController.popBackStack() },
                onNavigateToUser = { id -> navController.navigate(ADMIN_USER_DETAIL.replace("{userId}", id)) },
                onNavigateToReport = { id -> navController.navigate(ADMIN_BUG_DETAIL.replace("{bugId}", id)) },
                onNavigateToSimilarity = { id -> navController.navigate(ADMIN_SIMILARITY_DETAIL.replace("{projectId}", id)) }
            ) }
            composable(EDIT_PROFILE) { EditProfileScreen(onBack = { navController.popBackStack() }, onNavigate = { route -> navController.navigateTo(route) }) }
            composable(REPORT_ISSUE) { ReportIssueScreen(onBack = { navController.popBackStack() }, onNavigate = { route -> navController.navigateTo(route) }) }
        }
    }
}

private fun NavHostController.navigateTo(route: String) {
    if (route == AppNavigation.HOME) {
        navigate(route) {
            popUpTo(0) { inclusive = true }
        }
    } else {
        navigate(route)
    }
}
