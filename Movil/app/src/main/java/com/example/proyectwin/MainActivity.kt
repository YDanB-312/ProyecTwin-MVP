package com.example.proyectwin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.proyectwin.navigation.AppNavigation.AUTH_GRAPH
import com.example.proyectwin.navigation.AppNavigation.authGraph
import com.example.proyectwin.navigation.AppNavigation.aprendizGraph
import com.example.proyectwin.navigation.AppNavigation.instructorGraph
import com.example.proyectwin.navigation.AppNavigation.adminGraph
import com.example.proyectwin.ui.theme.ProyecTwinTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProyecTwinTheme {
                val navController = rememberNavController()
                
                NavHost(
                    navController = navController, 
                    startDestination = AUTH_GRAPH
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
