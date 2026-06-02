package com.example.proyectwin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.proyectwin.ui.components.SenaCard
import com.example.proyectwin.ui.components.SenaTextField
import com.example.proyectwin.ui.components.SenaTopBar
import com.example.proyectwin.ui.theme.*

@Composable
fun FichaDirectoryScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            SenaTopBar(title = "Directorio de Ficha", showProfile = false, showNotifications = false)
        },
        containerColor = SenaBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with Back
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                    Text(
                        "Miembros de Ficha",
                        style = MaterialTheme.typography.titleLarge,
                        color = SenaText
                    )
                }
            }

            // Search
            item {
                SenaTextField(
                    value = "",
                    onValueChange = {},
                    label = "",
                    placeholder = "Buscar compañero...",
                    leadingIcon = Icons.Default.Search
                )
            }

            // Instructor Section
            item {
                DirectorySectionTitle("Instructor")
                SenaCard {
                    DirectoryMemberRow("CR", "Carlos Ruiz", "Instructor - TI", isOnline = true)
                }
            }

            // Classmates Section
            item {
                DirectorySectionTitle("Compañeros (11)")
            }

            val classmates = listOf(
                MemberInfo("JP", "Juan Perez", "Activo"),
                MemberInfo("LG", "Laura Gomez", "Activo"),
                MemberInfo("AM", "Ana Martinez", "Activo"),
                MemberInfo("DS", "Diana Sanchez", "Inactivo")
            )

            items(classmates) { member ->
                SenaCard(modifier = Modifier.padding(vertical = 4.dp)) {
                    DirectoryMemberRow(member.initials, member.name, member.status, isOnline = member.status == "Activo")
                }
            }
        }
    }
}

@Composable
fun DirectorySectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = SenaText,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun DirectoryMemberRow(initials: String, name: String, sub: String, isOnline: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(44.dp),
            shape = CircleShape,
            color = if (initials == "CR") SenaGreen else SenaBorder
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(initials, color = if (initials == "CR") Color.White else SenaText, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text(sub, style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
        }
        if (isOnline) {
            Surface(
                modifier = Modifier.size(10.dp),
                shape = CircleShape,
                color = SenaSuccess
            ) {}
        }
    }
}

data class MemberInfo(val initials: String, val name: String, val status: String)

@Preview(showBackground = true)
@Composable
fun FichaDirectoryScreenPreview() {
    ProyecTwinTheme {
        FichaDirectoryScreen(onBack = {})
    }
}
