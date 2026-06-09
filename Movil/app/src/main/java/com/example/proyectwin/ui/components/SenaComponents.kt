package com.example.proyectwin.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectwin.ui.theme.*

// --- DATA MODELS ---

data class ProjectItem(
    val name: String,
    val status: String,
    val statusColor: Color,
    val date: String,
    val members: Int,
    val instructor: String,
    val instructorInitials: String,
)

// --- REUSABLE COMPONENTS ---

@Composable
fun SenaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isPrimary: Boolean = true,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isPrimary) SenaGreen else Color.Transparent,
            contentColor = if (isPrimary) Color.White else SenaGreen
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isPrimary) 2.dp else 0.dp
        ),
        border = if (!isPrimary) androidx.compose.foundation.BorderStroke(1.dp, SenaBorder) else null,
        enabled = enabled && !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(icon, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(text, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
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
            Text(label, style = MaterialTheme.typography.labelSmall, color = SenaTextLight, modifier = Modifier.padding(bottom = 6.dp))
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = SenaTextMuted, style = MaterialTheme.typography.bodyMedium) },
            leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null, tint = SenaGreen) } },
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
                onDone = { focusManager.clearFocus() }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SenaGreen,
                unfocusedBorderColor = SenaBorder,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            singleLine = true
        )
    }
}

@Composable
fun SenaCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    elevation: Dp = 0.5.dp,
    containerColor: Color = Color.White,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick ?: {},
        enabled = onClick != null,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = elevation)
    ) {
        Column(modifier = Modifier.padding(20.dp), content = content)
    }
}

@Composable
fun SenaSectionHeader(title: String, actionText: String? = null, onActionClick: (() -> Unit)? = null) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = SenaText)
        if (actionText != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(actionText, color = SenaGreen, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
fun SenaProgressBar(progress: Float, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = SenaTextSecondary)
            Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SenaGreen)
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = SenaGreen,
            trackColor = SenaBorderSoft
        )
    }
}

@Composable
fun SenaActionCard(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    SenaCard(modifier = modifier, onClick = onClick) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Surface(modifier = Modifier.size(56.dp), shape = RoundedCornerShape(16.dp), color = SenaGreen.copy(alpha = 0.08f)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = SenaGreen, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = SenaText, textAlign = TextAlign.Center)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = SenaTextLight, textAlign = TextAlign.Center)
        }
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
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            color = if (isSelected) Color.White else color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ProjectListItemCard(project: ProjectItem, onClick: () -> Unit) {
    SenaCard(
        onClick = onClick,
        elevation = 0.5.dp,
        modifier = Modifier.padding(bottom = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = SenaGreen.copy(alpha = 0.08f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null, tint = SenaGreen)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        project.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = SenaText,
                        maxLines = 1
                    )
                    Text(
                        "Instructor: ${project.instructor}",
                        style = MaterialTheme.typography.labelSmall,
                        color = SenaTextSecondary
                    )
                }
                SenaChip(text = project.status, color = project.statusColor)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = SenaBorderSoft)
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = SenaTextMuted, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(project.date, style = MaterialTheme.typography.labelSmall, color = SenaTextMuted)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Groups, contentDescription = null, tint = SenaTextMuted, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("${project.members} Miembros", style = MaterialTheme.typography.labelSmall, color = SenaTextMuted)
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard(message: String, icon: ImageVector) {
    SenaCard(containerColor = SenaBorderSoft.copy(alpha = 0.5f)) {
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = SenaTextMuted, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium, color = SenaTextSecondary, textAlign = TextAlign.Center)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SenaTopBar(
    title: String, 
    showProfile: Boolean = true, 
    showNotifications: Boolean = true,
    onNavigateToProfile: (() -> Unit)? = null,
    onNavigateToAlerts: (() -> Unit)? = null
) {
    TopAppBar(
        title = { Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Color.White) },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = SenaHeader),
        actions = {
            if (showNotifications) IconButton(onClick = { onNavigateToAlerts?.invoke() }) { Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White) }
            if (showProfile) {
                Surface(modifier = Modifier.padding(end = 12.dp).size(36.dp), shape = CircleShape, color = Color.White.copy(alpha = 0.2f)) {
                    IconButton(onClick = { onNavigateToProfile?.invoke() }) { Icon(Icons.Default.Person, contentDescription = null, tint = Color.White) }
                }
            }
        }
    )
}

@Composable
fun SenaSegmentedFilter(options: List<String>, selectedOption: String, onOptionSelected: (String) -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(24.dp), color = SenaBorder.copy(alpha = 0.3f)) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val tabWidth = maxWidth / options.size
            val selectedIndex = options.indexOf(selectedOption)
            val indicatorOffset by animateDpAsState(targetValue = tabWidth * selectedIndex, animationSpec = tween(300), label = "")
            Surface(modifier = Modifier.width(tabWidth).fillMaxHeight().offset(x = indicatorOffset).padding(4.dp), shape = RoundedCornerShape(20.dp), color = SenaGreen, shadowElevation = 4.dp) {}
            Row(modifier = Modifier.fillMaxSize()) {
                options.forEach { option ->
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().clickable { onOptionSelected(option) }, contentAlignment = Alignment.Center) {
                        Text(option, style = MaterialTheme.typography.labelMedium, fontWeight = if (option == selectedOption) FontWeight.Bold else FontWeight.Normal, color = if (option == selectedOption) Color.White else SenaTextLight)
                    }
                }
            }
        }
    }
}
