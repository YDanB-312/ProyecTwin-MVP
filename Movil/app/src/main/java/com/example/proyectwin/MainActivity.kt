package com.example.proyectwin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.core.view.WindowCompat
import com.example.proyectwin.data.api.SessionEvents
import com.example.proyectwin.navigation.AppNavigation
import com.example.proyectwin.navigation.AppNavigation.AUTH_GRAPH
import com.example.proyectwin.navigation.AppNavigation.authGraph
import com.example.proyectwin.navigation.AppNavigation.aprendizGraph
import com.example.proyectwin.navigation.AppNavigation.instructorGraph
import com.example.proyectwin.navigation.AppNavigation.adminGraph
import com.example.proyectwin.ui.theme.LocalThemeIsDark
import com.example.proyectwin.ui.theme.ProyecTwinTheme
import com.example.proyectwin.ui.viewmodel.AuthUiState
import com.example.proyectwin.ui.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProyecTwinTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = hiltViewModel()
                val uiState = authViewModel.uiState.collectAsState().value

                val isDark = LocalThemeIsDark.current.value
                val view = LocalView.current
                SideEffect {
                    val window = (view.context as? android.app.Activity)?.window
                    if (window != null) {
                        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
                    }
                }

                LaunchedEffect(Unit) {
                    SessionEvents.expired.collect {
                        authViewModel.logout()
                    }
                }

                val startDestination = when (uiState) {
                    is AuthUiState.Loading -> AUTH_GRAPH
                    is AuthUiState.LoggedOut -> AUTH_GRAPH
                    is AuthUiState.Error -> AUTH_GRAPH
                    is AuthUiState.Registered -> AUTH_GRAPH
                    is AuthUiState.LoggedIn -> {
                        when (uiState.user.role) {
                            "instructor" -> AppNavigation.INSTRUCTOR_GRAPH
                            "admin" -> AppNavigation.ADMIN_GRAPH
                            else -> AppNavigation.APRENDIZ_GRAPH
                        }
                    }
                }

                LaunchedEffect(uiState) {
                    when (uiState) {
                        is AuthUiState.LoggedOut -> {
                            navController.navigate(AppNavigation.LOGIN) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                        is AuthUiState.LoggedIn -> {
                            val dest = when (uiState.user.role) {
                                "instructor" -> AppNavigation.INSTRUCTOR_GRAPH
                                "admin" -> AppNavigation.ADMIN_GRAPH
                                else -> AppNavigation.APRENDIZ_GRAPH
                            }
                            navController.navigate(dest) {
                                launchSingleTop = true
                                popUpTo(0) { inclusive = true }
                            }
                        }
                        else -> Unit
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = startDestination
                ) {
                    authGraph(navController)
                    aprendizGraph(navController)
                    instructorGraph(navController)
                    adminGraph(navController)
                }
            }
        }
    }
}
