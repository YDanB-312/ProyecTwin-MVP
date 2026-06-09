package com.example.proyectwin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.proyectwin.ui.components.SenaButton
import com.example.proyectwin.ui.components.SenaCard
import com.example.proyectwin.ui.components.SenaTextField
import com.example.proyectwin.ui.components.SenaTopBar
import com.example.proyectwin.ui.theme.*

@Composable
fun JoinFichaScreen(onBack: () -> Unit) {
    var fichaCode by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            SenaTopBar(title = "Unirse a Ficha", showProfile = false, showNotifications = false)
        },
        containerColor = SenaBackground,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
            }

            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = SenaGreen.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Groups, contentDescription = null, tint = SenaGreen, modifier = Modifier.size(40.dp))
                }
            }

            Text(
                "Ingresa el código de tu ficha",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = SenaText
            )

            Text(
                "Solicita a tu instructor el código de la ficha a la que perteneces e ingrésalo aquí.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = SenaTextLight
            )

            SenaCard {
                SenaTextField(
                    value = fichaCode,
                    onValueChange = { fichaCode = it },
                    label = "Código de Ficha",
                    placeholder = "Ej: ADSO-2568"
                )
                Spacer(modifier = Modifier.height(24.dp))
                SenaButton(
                    text = "Unirse a la Ficha",
                    onClick = { /* TODO */ }
                )
            }

            // Info Card
            SenaCard(containerColor = SenaBorder.copy(alpha = 0.2f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.QuestionMark, contentDescription = null, tint = SenaGreen)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("¿Qué es una ficha?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Es tu grupo de formación. Al unirte podrás trabajar en equipo con tus compañeros y recibir revisiones de tu instructor.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SenaTextLight
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun JoinFichaScreenPreview() {
    ProyecTwinTheme {
        JoinFichaScreen {}
    }
}
