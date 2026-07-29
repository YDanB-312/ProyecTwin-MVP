package com.example.proyectwin.ui.theme

import androidx.compose.ui.graphics.Color

// --- EMERALD LUSH PREMIUM PALETTE ---
// All color values are defined here. Light theme is the default.
// Dark variants are accessed via SenaColorScheme (provided by ProyecTwinTheme).

// Primary Brand Colors (unchanged between themes)
val SenaGreen = Color(0xFF10B981)
val SenaDarkGreen = Color(0xFF065F46)
val SenaAccent = Color(0xFF34D399)

data class SenaColorScheme(
    val background: Color,
    val backgroundElevated: Color,
    val surface: Color,
    val header: Color,
    val text: Color,
    val textSecondary: Color,
    val textLight: Color,
    val textMuted: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
    val info: Color,
    val border: Color,
    val borderSoft: Color,
    val glassEffect: Color,
    val green: Color,
    val darkGreen: Color,
    val accent: Color,
    val dangerHover: Color,
    val dangerActive: Color,
    val primary10: Color,
    val success10: Color,
    val warning10: Color,
    val danger10: Color,
    val header10: Color,
    val info10: Color,
    val filaZebra: Color,
)

fun lightSenaColors() = SenaColorScheme(
    background = Color(0xFFF8FAFC),
    backgroundElevated = Color(0xFFFFFFFF),
    surface = Color(0xFFF1F5F9),
    header = Color(0xFF064E3B),
    text = Color(0xFF0F172A),
    textSecondary = Color(0xFF334155),
    textLight = Color(0xFF64748B),
    textMuted = Color(0xFF94A3B8),
    success = Color(0xFF059669),
    warning = Color(0xFFF59E0B),
    danger = Color(0xFFEF4444),
    info = Color(0xFF3B82F6),
    border = Color(0xFFE2E8F0),
    borderSoft = Color(0xFFF1F5F9),
    glassEffect = Color(0x1AFFFFFF),
    green = SenaGreen,
    darkGreen = SenaDarkGreen,
    accent = SenaAccent,
    dangerHover = Color(0xFF991B1B),
    dangerActive = Color(0xFF7F1D1D),
    primary10 = Color(0x0D059669),
    success10 = Color(0x1A10B981),
    warning10 = Color(0x1AF59E0B),
    danger10 = Color(0x1AEF4444),
    header10 = Color(0x1A064E3B),
    info10 = Color(0x1A3B82F6),
    filaZebra = Color(0x05000000),
)

fun darkSenaColors() = SenaColorScheme(
    background = Color(0xFF0F172A),
    backgroundElevated = Color(0xFF1E293B),
    surface = Color(0xFF1E293B),
    header = Color(0xFF022C22),
    text = Color(0xFFE2E8F0),
    textSecondary = Color(0xFFCBD5E1),
    textLight = Color(0xFF94A3B8),
    textMuted = Color(0xFF64748B),
    success = Color(0xFF34D399),
    warning = Color(0xFFFBBF24),
    danger = Color(0xFFF87171),
    info = Color(0xFF60A5FA),
    border = Color(0xFF334155),
    borderSoft = Color(0xFF1E293B),
    glassEffect = Color(0x1A000000),
    green = SenaGreen,
    darkGreen = SenaDarkGreen,
    accent = Color(0xFF14B8A6),
    dangerHover = Color(0xFFB91C1C),
    dangerActive = Color(0xFF991B1B),
    primary10 = Color(0x1A10B981),
    success10 = Color(0x1F34D399),
    warning10 = Color(0x1FFBBF24),
    danger10 = Color(0x1FF87171),
    header10 = Color(0x1F022C22),
    info10 = Color(0x2660A5FA),
    filaZebra = Color(0x08FFFFFF),
)
