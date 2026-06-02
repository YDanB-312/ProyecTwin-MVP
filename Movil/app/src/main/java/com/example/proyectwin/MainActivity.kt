package com.example.proyectwin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.proyectwin.ui.screens.*
import com.example.proyectwin.ui.theme.ProyecTwinTheme
import com.example.proyectwin.ui.theme.SenaGreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProyecTwinTheme {
                val navController = rememberNavController()
                
                NavHost(navController = navController, startDestination = "login") {
                    composable("login") {
                        LoginScreen(onLoginSuccess = { 
                            navController.navigate("main/home") {
                                popUpTo("login") { inclusive = true }
                            }
                        })
                    }
                    composable("main/home") {
                        MainContainer(currentRoute = "main/home", onNavigate = { route ->
                            navController.navigate(route) {
                                launchSingleTop = true
                            }
                        })
                    }
                    composable("main/projects") {
                        MainContainer(currentRoute = "main/projects", onNavigate = { route ->
                            navController.navigate(route) { launchSingleTop = true }
                        })
                    }
                    composable("main/alerts") {
                        MainContainer(currentRoute = "main/alerts", onNavigate = { route ->
                            navController.navigate(route) { launchSingleTop = true }
                        })
                    }
                    composable("main/profile") {
                        MainContainer(currentRoute = "main/profile", onNavigate = { route ->
                            navController.navigate(route) { launchSingleTop = true }
                        })
                    }
                    composable("project/new") {
                        NewProjectScreen(onBack = { navController.popBackStack() })
                    }
                    composable("project/detail/{id}") {
                        ProjectDetailScreen(onBack = { navController.popBackStack() })
                    }
                    composable("project/similarity/{id}") {
                        SimilarityDetailScreen(onBack = { navController.popBackStack() })
                    }
                    composable("ficha/detail") {
                        FichaDetailScreen(onBack = { navController.popBackStack() })
                    }
                    composable("ficha/join") {
                        JoinFichaScreen(onBack = { navController.popBackStack() })
                    }
                    composable("ficha/directory") {
                        FichaDirectoryScreen(onBack = { navController.popBackStack() })
                    }
                    composable("report/issue") {
                        ReportIssueScreen(onBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}

@Composable
fun MainContainer(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                val items = listOf(
                    Triple("main/home", "Inicio", Icons.Default.Home),
                    Triple("main/projects", "Proyectos", Icons.AutoMirrored.Filled.Assignment),
                    Triple("main/alerts", "Alertas", Icons.Default.Notifications),
                    Triple("main/profile", "Perfil", Icons.Default.Person)
                )
                
                items.forEach { (route, label, icon) ->
                    val isSelected = currentRoute == route
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) },
                        selected = isSelected,
                        onClick = { onNavigate(route) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SenaGreen,
                            selectedTextColor = SenaGreen,
                            indicatorColor = SenaGreen.copy(alpha = 0.1f),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (currentRoute) {
                "main/home" -> DashboardScreen(onNavigate = onNavigate)
                "main/projects" -> ProjectsScreen(
                    onNewProject = { onNavigate("project/new") },
                    onProjectDetail = { onNavigate("project/detail/$it") }
                )
                "main/alerts" -> AlertsScreen(onNavigate = onNavigate)
                "main/profile" -> ProfileScreen(onNavigate = onNavigate)
            }
        }
    }
}
