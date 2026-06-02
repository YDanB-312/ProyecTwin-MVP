package com.example.proyectwin.ui.components

import androidx.compose.foundation.background
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.unit.sp
import com.example.proyectwin.ui.theme.*

@Composable
fun SenaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isPrimary: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isPrimary) SenaGreen else Color.Transparent,
            contentColor = if (isPrimary) Color.White else SenaGreen
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isPrimary) 2.dp else 0.dp,
            pressedElevation = if (isPrimary) 0.dp else 0.dp
        ),
        border = if (!isPrimary) androidx.compose.foundation.BorderStroke(1.dp, SenaBorder) else null,
        enabled = !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = if (isPrimary) Color.White else SenaGreen,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(icon, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                )
            }
        }
    }
}

@Composable
fun SenaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    isPassword: Boolean = false
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = SenaText
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = SenaTextLight) },
            leadingIcon = leadingIcon?.let {
                { Icon(it, contentDescription = null, tint = SenaTextLight) }
            },
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SenaGreen,
                unfocusedBorderColor = SenaBorder,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedLabelColor = SenaGreen,
                unfocusedLabelColor = SenaTextLight
            ),
            singleLine = true
        )
    }
}

@Composable
fun SenaCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    elevation: Dp = 2.dp,
    containerColor: Color = Color.White,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick ?: {},
        enabled = onClick != null,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = elevation
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun SenaChip(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Surface(
        color = if (isSelected) color else color.copy(alpha = 0.1f),
        shape = CircleShape,
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable { onClick() } else Modifier
        ),
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            color = if (isSelected) Color.White else color,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SenaTopBar(
    title: String,
    showProfile: Boolean = true,
    showNotifications: Boolean = true
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = SenaHeader
        ),
        actions = {
            if (showNotifications) {
                IconButton(onClick = { /* TODO */ }) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White)
                }
            }
            if (showProfile) {
                Surface(
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(36.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                    }
                }
            }
        }
    )
}

@Composable
fun SenaSegmentedFilter(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(24.dp),
        color = SenaBorder.copy(alpha = 0.3f)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val tabWidth = maxWidth / options.size
            val selectedIndex = options.indexOf(selectedOption)
            val indicatorOffset by animateDpAsState(
                targetValue = tabWidth * selectedIndex,
                animationSpec = tween(durationMillis = 300),
                label = "IndicatorOffset"
            )

            // Indicador deslizante (Color de acento SENA)
            Surface(
                modifier = Modifier
                    .width(tabWidth)
                    .fillMaxHeight()
                    .offset(x = indicatorOffset)
                    .padding(4.dp),
                shape = RoundedCornerShape(20.dp),
                color = SenaGreen,
                shadowElevation = 4.dp
            ) {}

            // Opciones de texto
            Row(modifier = Modifier.fillMaxSize()) {
                options.forEach { option ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onOptionSelected(option) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (option == selectedOption) FontWeight.Bold else FontWeight.Normal,
                            color = if (option == selectedOption) Color.White else SenaTextLight
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SenaComponentsPreview() {
    ProyecTwinTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SenaButton(text = "Iniciar Sesión", onClick = {})
            SenaButton(text = "Cargando...", onClick = {}, isLoading = true)
            SenaButton(text = "Crear cuenta", onClick = {}, isPrimary = false)
            SenaTextField(
                value = "",
                onValueChange = {},
                label = "Correo Electrónico",
                placeholder = "tu@correo.com"
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SenaChip(text = "Aprobado", color = SenaSuccess)
                SenaChip(text = "Pendiente", color = SenaWarning)
                SenaChip(text = "Rechazado", color = SenaDanger)
            }
            SenaCard {
                Text("Tarjeta Premium", style = MaterialTheme.typography.titleMedium, color = SenaText)
                Text("Con bordes más redondeados y sombra sutil.", style = MaterialTheme.typography.bodySmall, color = SenaTextLight)
            }
        }
    }
}
