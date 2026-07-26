package com.example.proyectwin.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectwin.ui.theme.*

// --- PREMIUM COMPONENTS (EMERALD LUSH EDITION) ---

@Composable
fun SenaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isPrimary: Boolean = true,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    containerColor: Color? = null
) {
    val primaryGradient = Brush.linearGradient(colors = listOf(SenaGreen, SenaDarkGreen))
    
    Surface(
        modifier = modifier
            .height(56.dp)
            .widthIn(min = 120.dp)
            .clip(CircleShape)
            .then(if (isPrimary && enabled && !isLoading) Modifier.background(primaryGradient) else Modifier)
            .clickable(enabled = enabled && !isLoading) { onClick() },
        color = when {
            !isPrimary -> Color.Transparent
            containerColor != null -> containerColor
            else -> if (enabled) Color.Transparent else SenaBorder
        },
        border = if (!isPrimary) androidx.compose.foundation.BorderStroke(2.dp, SenaGreen) else null,
        shape = CircleShape
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = if (isPrimary) Color.White else SenaGreen, strokeWidth = 2.dp)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (icon != null) {
                        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = if (isPrimary) Color.White else SenaGreen)
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Text(
                        text = text, 
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Black, 
                            letterSpacing = 1.sp,
                            color = if (isPrimary) Color.White else SenaGreen
                        )
                    )
                }
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
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next
) {
    val focusManager = LocalFocusManager.current
    Column(modifier = modifier.fillMaxWidth()) {
        if (label.isNotEmpty()) {
            Text(
                text = label, 
                style = MaterialTheme.typography.labelMedium, 
                color = SenaTextSecondary, 
                modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = SenaTextMuted) },
            leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null, tint = SenaGreen) } },
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            shape = RoundedCornerShape(20.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
                onDone = { focusManager.clearFocus() }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SenaGreen,
                unfocusedBorderColor = SenaBorder,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                cursorColor = SenaGreen
            ),
            singleLine = true
        )
    }
}

@Composable
fun SenaCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    elevation: Dp = 4.dp,
    containerColor: Color = SenaBackgroundElevated,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick ?: {},
        enabled = onClick != null,
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = elevation)
    ) {
        Column(
            modifier = Modifier.padding(24.dp), 
            content = content
        )
    }
}

@Composable
fun SenaPageHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null
) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        if (icon != null) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(16.dp),
                color = SenaGreen.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = SenaGreen, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
        
        Text(
            title, 
            style = MaterialTheme.typography.headlineMedium, 
            fontWeight = FontWeight.Black, 
            color = SenaText
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                subtitle, 
                style = MaterialTheme.typography.bodyMedium, 
                color = SenaTextLight
            )
        }
    }
}

@Composable
fun SenaStatusBadge(status: String, modifier: Modifier = Modifier) {
    val color = when (status.lowercase()) {
        "pendiente", "en revisión" -> SenaWarning
        "aprobado", "revisado", "activo" -> SenaSuccess
        "rechazado", "error", "inactivo" -> SenaDanger
        else -> SenaTextMuted
    }

    Surface(
        color = color.copy(alpha = 0.12f),
        shape = CircleShape,
        modifier = modifier,
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Text(
            text = status.uppercase(),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SenaTopBar(
    title: String, 
    showProfile: Boolean = true, 
    showNotifications: Boolean = true,
    onBack: (() -> Unit)? = null,
    onLogout: (() -> Unit)? = null,
    onNavigateToProfile: (() -> Unit)? = null,
    onNavigateToAlerts: (() -> Unit)? = null
) {
    val backgroundBrush = Brush.linearGradient(colors = listOf(SenaHeader, SenaDarkGreen))
    val isDarkState = LocalThemeIsDark.current
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Box(modifier = Modifier.background(backgroundBrush)) {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        title, 
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold, 
                        color = Color.White
                    ) 
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { isDarkState.value = !isDarkState.value }) {
                        Icon(
                            if (isDarkState.value) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Tema",
                            tint = Color.White.copy(alpha = 0.9f)
                        )
                    }
                    if (showNotifications) {
                        IconButton(onClick = { onNavigateToAlerts?.invoke() }) { 
                            Icon(Icons.Default.Notifications, contentDescription = "Notificaciones", tint = Color.White.copy(alpha = 0.9f)) 
                        }
                    }
                    if (onLogout != null) {
                        IconButton(onClick = onLogout) { 
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Salir", tint = Color.White.copy(alpha = 0.9f)) 
                        }
                    }
                    if (showProfile) {
                        Surface(
                            modifier = Modifier.padding(end = 12.dp).size(36.dp), 
                            shape = CircleShape, 
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            IconButton(onClick = { onNavigateToProfile?.invoke() }) { 
                                Icon(Icons.Default.Person, contentDescription = "Perfil", tint = Color.White, modifier = Modifier.size(18.dp)) 
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun SenaMetricPill(icon: ImageVector, value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color.White,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, SenaBorderSoft)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = SenaGreen.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = SenaGreen, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = SenaText)
                Text(label, style = MaterialTheme.typography.labelSmall, color = SenaTextLight)
            }
        }
    }
}

@Composable
fun SenaSettingsItem(
    icon: ImageVector,
    title: String,
    description: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(44.dp),
            shape = RoundedCornerShape(14.dp),
            color = SenaGreen.copy(alpha = 0.08f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = SenaGreen, modifier = Modifier.size(22.dp))
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = SenaText)
            if (description != null) {
                Text(description, style = MaterialTheme.typography.bodySmall, color = SenaTextLight)
            }
        }
        
        if (trailing != null) {
            trailing()
        } else if (onClick != null) {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SenaTextMuted, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun SenaEmptyState(
    message: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    title: String? = null,
    action: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            color = SenaGreen.copy(alpha = 0.05f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon, 
                    contentDescription = null, 
                    tint = SenaGreen.copy(alpha = 0.4f), 
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(28.dp))
        if (title != null) {
            Text(
                title, 
                style = MaterialTheme.typography.titleLarge, 
                fontWeight = FontWeight.Black, 
                color = SenaText
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        Text(
            message, 
            style = MaterialTheme.typography.bodyMedium, 
            color = SenaTextSecondary, 
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 300.dp)
        )
        if (action != null) {
            Spacer(modifier = Modifier.height(32.dp))
            action()
        }
    }
}

@Composable
fun SenaAlertBanner(
    message: String,
    title: String,
    icon: ImageVector = Icons.Default.Warning,
    color: Color = SenaWarning,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = color.copy(alpha = 0.08f),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = SenaText)
                Text(message, style = MaterialTheme.typography.bodySmall, color = SenaTextSecondary, lineHeight = 18.sp)
                if (actionText != null && onAction != null) {
                    TextButton(
                        onClick = onAction,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(actionText, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = color)
                    }
                }
            }
        }
    }
}

@Composable
fun SenaSectionHeader(title: String, modifier: Modifier = Modifier, actionText: String? = null, onActionClick: (() -> Unit)? = null) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 4.dp), 
        horizontalArrangement = Arrangement.SpaceBetween, 
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title, 
            style = MaterialTheme.typography.titleLarge, 
            fontWeight = FontWeight.Black, 
            color = SenaText
        )
        if (actionText != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(actionText, color = SenaGreen, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black)
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = SenaGreen, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun SenaBottomBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 12.dp,
        shadowElevation = 24.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars).padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            content = content
        )
    }
}

@Composable
fun SenaChip(text: String, color: Color, modifier: Modifier = Modifier, isSelected: Boolean = false, onClick: (() -> Unit)? = null) {
    Surface(
        color = if (isSelected) color else color.copy(alpha = 0.1f),
        shape = CircleShape,
        modifier = modifier.then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            color = if (isSelected) Color.White else color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun SenaActionCard(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    SenaCard(modifier = modifier, onClick = onClick, elevation = 6.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Surface(
                modifier = Modifier.size(64.dp), 
                shape = RoundedCornerShape(20.dp), 
                color = SenaGreen.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = SenaGreen, modifier = Modifier.size(32.dp))
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = SenaText, textAlign = TextAlign.Center)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = SenaTextLight, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun TeamMemberRow(initials: String, name: String, role: String, isLider: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = if (isLider) SenaGreen else SenaBorderSoft
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(initials, color = if (isLider) Color.White else SenaText, fontWeight = FontWeight.Black)
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = SenaText)
            Text(role, style = MaterialTheme.typography.bodySmall, color = SenaTextLight)
        }
    }
}

// --- ICON EXTENSIONS (PREMIUM UI) ---
val Icons.Filled.Tasks: ImageVector get() = Icons.AutoMirrored.Filled.List
val Icons.Filled.ChalkboardTeacher: ImageVector get() = Icons.Default.School
