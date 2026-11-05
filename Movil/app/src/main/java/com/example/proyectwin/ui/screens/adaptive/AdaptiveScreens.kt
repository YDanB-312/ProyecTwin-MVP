package com.example.proyectwin.ui.screens.adaptive

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.NavHostController
import com.example.proyectwin.data.model.Notification
import com.example.proyectwin.navigation.AppNavigation
import com.example.proyectwin.navigation.NotificationRoutes
import com.example.proyectwin.ui.components.LocalIsTwoPane
import com.example.proyectwin.ui.components.TwoPaneListDetail
import com.example.proyectwin.ui.screens.AlertsScreen
import com.example.proyectwin.ui.screens.admin.*
import com.example.proyectwin.ui.screens.aprendiz.*
import com.example.proyectwin.ui.screens.instructor.*

private fun String.idDeRuta(): Int? =
    substringAfterLast('/').substringBefore('?').toIntOrNull()

@Composable
fun AdminUsersAdaptive(navController: NavHostController, bottomBar: @Composable () -> Unit = {}) {
    val expanded = LocalIsTwoPane.current
    var selected by rememberSaveable { mutableStateOf<Int?>(null) }
    TwoPaneListDetail(
        expanded = expanded,
        selectedId = selected,
        onSelected = { selected = it },
        list = { _ ->
            UserManagementScreen(
                onBack = { navController.popBackStack() },
                onNavigate = { route ->
                    val id = route.idDeRuta()
                    if (expanded && route.startsWith("admin/user/") && id != null) selected = id
                    else navController.navigate(route)
                },
                bottomBar = bottomBar,
            )
        },
        detail = { id, close ->
            UserDetailScreen(
                userId = id.toString(),
                onBack = close,
                onNavigate = { route -> navController.navigate(route) },
            )
        },
    )
}

@Composable
fun AdminProjectsAdaptive(navController: NavHostController) {
    val expanded = LocalIsTwoPane.current
    var selected by rememberSaveable { mutableStateOf<Int?>(null) }
    TwoPaneListDetail(
        expanded = expanded,
        selectedId = selected,
        onSelected = { selected = it },
        list = { _ ->
            AdminProjectsScreen(
                onBack = { navController.popBackStack() },
                onProjectDetail = { id ->
                    if (expanded) selected = id
                    else navController.navigate(
                        AppNavigation.ADMIN_PROJECT_DETAIL.replace("{projectId}", id.toString()),
                    )
                },
            )
        },
        detail = { id, close ->
            ProjectDetailScreen(
                projectId = id.toString(),
                onBack = close,
                onNavigate = { route -> navController.navigate(route) },
                // La intervención admin conserva el retorno al detalle.
                editarRoute = "aprendiz_new_project/{projectId}?retorno=admin",
                similitudesRoute = AppNavigation.ADMIN_SIMILARITY_LIST_FILTRO,
                similarityRoute = AppNavigation.ADMIN_SIMILARITY_DETAIL,
            )
        },
    )
}

@Composable
fun AdminFichasAdaptive(navController: NavHostController) {
    val expanded = LocalIsTwoPane.current
    var selected by rememberSaveable { mutableStateOf<Int?>(null) }
    TwoPaneListDetail(
        expanded = expanded,
        selectedId = selected,
        onSelected = { selected = it },
        list = { _ ->
            AdminFichasScreen(
                onBack = { navController.popBackStack() },
                onCreateFicha = { navController.navigate(AppNavigation.INSTRUCTOR_CREAR_FICHA_BASE) },
                onNavigate = { route -> navController.navigate(route) },
                onFichaDetail = { id ->
                    if (expanded) selected = id
                    else navController.navigate(
                        AppNavigation.ADMIN_FICHA_DETAIL.replace("{fichaId}", id.toString()),
                    )
                },
            )
        },
        detail = { id, close ->
            AdminFichaDetailScreen(
                fichaId = id.toString(),
                onBack = close,
                onNavigate = { route -> navController.navigate(route) },
            )
        },
    )
}

@Composable
fun AdminSimilaritiesAdaptive(
    navController: NavHostController,
    proyectoId: Int? = null,
    bottomBar: @Composable () -> Unit = {},
) {
    val expanded = LocalIsTwoPane.current
    var selected by rememberSaveable { mutableStateOf<Int?>(null) }
    TwoPaneListDetail(
        expanded = expanded,
        selectedId = selected,
        onSelected = { selected = it },
        list = { _ ->
            AdminSimilarityListScreen(
                proyectoId = proyectoId,
                onBack = { navController.popBackStack() },
                onNavigate = { route ->
                    val id = route.idDeRuta()
                    if (expanded && id != null) selected = id
                    else navController.navigate(route)
                },
                bottomBar = bottomBar,
            )
        },
        detail = { id, close ->
            AdminSimilarityDetailScreen(
                similarityId = id.toString(),
                onBack = close,
                onNavigate = { route -> navController.navigate(route) },
            )
        },
    )
}

@Composable
fun InstructorRevisionAdaptive(navController: NavHostController, bottomBar: @Composable () -> Unit = {}) {
    val expanded = LocalIsTwoPane.current
    var selected by rememberSaveable { mutableStateOf<Int?>(null) }
    TwoPaneListDetail(
        expanded = expanded,
        selectedId = selected,
        onSelected = { selected = it },
        list = { _ ->
            RevisionPropuestasScreen(
                onBack = { navController.popBackStack() },
                onProjectDetail = { id ->
                    if (expanded) selected = id
                    else navController.navigate(
                        AppNavigation.INSTRUCTOR_DETAIL.replace("{projectId}", id.toString()),
                    )
                },
                bottomBar = bottomBar,
            )
        },
        detail = { id, close ->
            ProjectDetailScreen(
                projectId = id.toString(),
                onBack = close,
                onNavigate = { route -> navController.navigate(route) },
                editarRoute = AppNavigation.INSTRUCTOR_DETAIL,
                similitudesRoute = AppNavigation.INSTRUCTOR_SIMILITUDES,
                similarityRoute = AppNavigation.INSTRUCTOR_SIMILARITY_DETAIL,
            )
        },
    )
}

@Composable
fun InstructorFichasAdaptive(navController: NavHostController, bottomBar: @Composable () -> Unit = {}) {
    val expanded = LocalIsTwoPane.current
    var selected by rememberSaveable { mutableStateOf<Int?>(null) }
    TwoPaneListDetail(
        expanded = expanded,
        selectedId = selected,
        onSelected = { selected = it },
        list = { _ ->
            ManageFichasScreen(
                onBack = { navController.popBackStack() },
                onViewDetail = { id ->
                    val fichaId = id.toIntOrNull()
                    if (expanded && fichaId != null) selected = fichaId
                    else navController.navigate(
                        AppNavigation.INSTRUCTOR_FICHA_DETAIL.replace("{fichaId}", id),
                    )
                },
                onViewDirectory = { navController.navigate(AppNavigation.INSTRUCTOR_FICHAS) },
                onCreateFicha = { navController.navigate(AppNavigation.INSTRUCTOR_CREAR_FICHA_BASE) },
                onNavigate = { route -> navController.navigate(route) },
                bottomBar = bottomBar,
            )
        },
        detail = { id, close ->
            DetalleFichaInstructorScreen(
                fichaId = id.toString(),
                onBack = close,
                onNavigate = { route -> navController.navigate(route) },
                onCompaneroDetail = { userId ->
                    navController.navigate(
                        AppNavigation.INSTRUCTOR_COMPANERO_DETAIL.replace("{userId}", userId.toString()),
                    )
                },
            )
        },
    )
}

@Composable
fun InstructorSimilaritiesAdaptive(
    navController: NavHostController,
    proyectoId: Int? = null,
    bottomBar: @Composable () -> Unit = {},
) {
    val expanded = LocalIsTwoPane.current
    var selected by rememberSaveable { mutableStateOf<Int?>(null) }
    TwoPaneListDetail(
        expanded = expanded,
        selectedId = selected,
        onSelected = { selected = it },
        list = { _ ->
            SimilitudesInstructorScreen(
                proyectoId = proyectoId,
                onBack = { navController.popBackStack() },
                onNavigate = { route ->
                    val id = route.idDeRuta()
                    if (expanded && id != null) selected = id
                    else navController.navigate(route)
                },
                bottomBar = bottomBar,
            )
        },
        detail = { id, close ->
            SimilarityDetailScreen(
                similarityId = id.toString(),
                detalleProyectoRoute = AppNavigation.INSTRUCTOR_DETAIL,
                similarityRoute = AppNavigation.INSTRUCTOR_SIMILARITY_DETAIL,
                onBack = close,
                onNavigate = { route -> navController.navigate(route) },
            )
        },
    )
}

@Composable
fun AprendizSimilaritiesAdaptive(
    navController: NavHostController,
    proyectoId: Int? = null,
    bottomBar: @Composable () -> Unit = {},
) {
    val expanded = LocalIsTwoPane.current
    var selected by rememberSaveable { mutableStateOf<Int?>(null) }
    TwoPaneListDetail(
        expanded = expanded,
        selectedId = selected,
        onSelected = { selected = it },
        list = { _ ->
            SimilitudesScreen(
                proyectoId = proyectoId,
                onBack = { navController.popBackStack() },
                onNavigate = { route ->
                    val id = route.idDeRuta()
                    if (expanded && id != null) selected = id
                    else navController.navigate(route)
                },
                bottomBar = bottomBar,
            )
        },
        detail = { id, close ->
            SimilarityDetailScreen(
                similarityId = id.toString(),
                detalleProyectoRoute = AppNavigation.APRENDIZ_DETAIL,
                onBack = close,
                onNavigate = { route -> navController.navigate(route) },
            )
        },
    )
}

@Composable
fun AlertsAdaptive(
    navController: NavHostController,
    profileRoute: String,
    similitudesRoute: String,
    detailRoute: String,
    fichaRoute: String,
    reporteRoute: String = "",
    bottomBar: @Composable () -> Unit = {},
) {
    val expanded = LocalIsTwoPane.current
    var selected by rememberSaveable { mutableStateOf<Int?>(null) }
    val resolverDestino: (Notification) -> String? = { alert ->
        NotificationRoutes.resolverDestino(
            tipo = alert.notifType,
            enlaceModulo = alert.enlaceModulo,
            enlaceId = alert.enlaceId,
            similitudesRoute = similitudesRoute,
            detailRoute = detailRoute,
            fichaRoute = fichaRoute,
            reporteRoute = reporteRoute,
        )
    }
    val seleccionar: ((Notification) -> Unit)? = if (expanded) {
        { alert -> selected = alert.id }
    } else {
        null
    }

    TwoPaneListDetail(
        expanded = expanded,
        selectedId = selected,
        onSelected = { selected = it },
        list = { _ ->
            AlertsScreen(
                onNavigate = { route -> navController.navigate(route) },
                onBack = { navController.popBackStack() },
                profileRoute = profileRoute,
                similitudesRoute = similitudesRoute,
                detailRoute = detailRoute,
                fichaRoute = fichaRoute,
                reporteRoute = reporteRoute,
                onSelectAlert = seleccionar,
                bottomBar = bottomBar,
            )
        },
        detail = { id, close ->
            NotificationDetailPane(
                notificationId = id,
                resolverDestino = resolverDestino,
                onClose = close,
                onNavigate = { route -> navController.navigate(route) },
            )
        },
    )
}

@Composable
fun AdminBugReportsAdaptive(navController: NavHostController) {
    val expanded = LocalIsTwoPane.current
    var selected by rememberSaveable { mutableStateOf<Int?>(null) }
    TwoPaneListDetail(
        expanded = expanded,
        selectedId = selected,
        onSelected = { selected = it },
        list = { _ ->
            BugReportsScreen(
                onBack = { navController.popBackStack() },
                onNavigate = { route ->
                    val id = route.idDeRuta()
                    if (expanded && id != null) selected = id
                    else navController.navigate(route)
                },
            )
        },
        detail = { id, close ->
            BugReportDetailScreen(
                bugId = id.toString(),
                onBack = close,
            )
        },
    )
}

@Composable
fun AprendizProjectsAdaptive(navController: NavHostController, bottomBar: @Composable () -> Unit = {}) {
    val expanded = LocalIsTwoPane.current
    var selected by rememberSaveable { mutableStateOf<Int?>(null) }
    TwoPaneListDetail(
        expanded = expanded,
        selectedId = selected,
        onSelected = { selected = it },
        list = { _ ->
            ProjectsScreen(
                onNavigate = { route -> navController.navigate(route) },
                onNewProject = { navController.navigate(AppNavigation.rutaNuevoProyecto()) },
                onProjectDetail = { id ->
                    val projectId = id.toIntOrNull()
                    if (expanded && projectId != null) selected = projectId
                    else navController.navigate("aprendiz_detail/$id")
                },
                bottomBar = bottomBar,
            )
        },
        detail = { id, close ->
            ProjectDetailScreen(
                projectId = id.toString(),
                onBack = close,
                onNavigate = { route -> navController.navigate(route) },
                onEnviado = { projectId ->
                    navController.navigate(AppNavigation.rutaAnalisis(projectId))
                },
            )
        },
    )
}
