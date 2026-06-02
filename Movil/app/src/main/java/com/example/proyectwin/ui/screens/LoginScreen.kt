package com.example.proyectwin.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectwin.R
import com.example.proyectwin.ui.components.SenaButton
import com.example.proyectwin.ui.components.SenaCard
import com.example.proyectwin.ui.components.SenaTextField
import com.example.proyectwin.ui.theme.*

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.White, SenaBackground)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            
            // Premium Logo Header
            Surface(
                modifier = Modifier.size(100.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_proyectwin),
                        contentDescription = "Logo ProyecTwin",
                        modifier = Modifier.size(70.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = buildAnnotatedString {
                    append("Proyec")
                    withStyle(style = SpanStyle(color = SenaGreen)) {
                        append("Twin")
                    }
                },
                style = MaterialTheme.typography.headlineLarge
            )
            
            Text(
                text = "Gestión de proyectos de formación",
                style = MaterialTheme.typography.bodyMedium,
                color = SenaTextLight
            )

            Spacer(modifier = Modifier.height(48.dp))

            SenaCard(elevation = 4.dp) {
                Text(
                    "Iniciar Sesión",
                    style = MaterialTheme.typography.titleLarge,
                    color = SenaText,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                SenaTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Correo Electrónico",
                    placeholder = "tu@correo.com",
                    leadingIcon = Icons.Default.Email
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                SenaTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Contraseña",
                    placeholder = "••••••••",
                    leadingIcon = Icons.Default.Lock,
                    isPassword = true
                )

                TextButton(
                    onClick = { /* TODO */ },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        "¿Olvidaste tu contraseña?",
                        color = SenaGreen,
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                SenaButton(
                    text = "Entrar al Sistema",
                    onClick = { 
                        isLoading = true
                        // Simulamos carga y éxito
                        onLoginSuccess() 
                    },
                    isLoading = isLoading
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = SenaBorder)
                    Text(
                        "O",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = SenaTextLight,
                        style = MaterialTheme.typography.labelSmall
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = SenaBorder)
                }

                Spacer(modifier = Modifier.height(24.dp))

                SenaButton(
                    text = "Crear Cuenta Nueva",
                    onClick = { /* TODO */ },
                    isPrimary = false
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Footer Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_sena_blanco), // Usaremos el verde si es posible, pero el blanco con tinte funciona
                    contentDescription = "Logo SENA",
                    modifier = Modifier.size(30.dp),
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(SenaGreen)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Servicio Nacional de Aprendizaje",
                    style = MaterialTheme.typography.labelSmall,
                    color = SenaTextLight
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    ProyecTwinTheme {
        LoginScreen(onLoginSuccess = {})
    }
}
