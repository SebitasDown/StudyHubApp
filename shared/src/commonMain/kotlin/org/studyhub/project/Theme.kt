package org.studyhub.project

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * StudyHub — Premium Dark Glassmorphism.
 * Tokens tomados del frontend web (styles.css) para mantener la misma identidad visual.
 */
object StudyHubColors {
    val Bg = Color(0xFF0F172A)
    val Surface = Color(0xFF1E293B)
    val SurfaceLight = Color(0xFF334155)
    val SurfaceHover = Color(0xFF3B4A63)

    val Glass = Color(0x991E293B)          // rgba(30,41,59,.6)
    val GlassBorder = Color(0x1A94A3B8)    // rgba(148,163,184,.1)
    val Border = Color(0x1F94A3B8)         // rgba(148,163,184,.12)

    val Primary = Color(0xFF6366F1)
    val PrimaryDark = Color(0xFF4F46E5)
    val PrimaryLight = Color(0xFF818CF8)
    val Secondary = Color(0xFF10B981)
    val SecondaryLight = Color(0xFF34D399)
    val Accent = Color(0xFFF59E0B)
    val AccentLight = Color(0xFFFBBF24)
    val AccentDark = Color(0xFFD97706)
    val Danger = Color(0xFFF43F5E)
    val DangerLight = Color(0xFFFB7185)
    val Info = Color(0xFF3B82F6)
    val InfoLight = Color(0xFF93BBFD)

    val TextPrimary = Color(0xFFF8FAFC)
    val TextSecondary = Color(0xFF94A3B8)
    val TextTertiary = Color(0xFF64748B)
}

private val StudyHubColorScheme = darkColorScheme(
    primary = StudyHubColors.Primary,
    onPrimary = Color.White,
    primaryContainer = StudyHubColors.PrimaryDark,
    onPrimaryContainer = Color(0xFFC7D2FE),
    inversePrimary = StudyHubColors.PrimaryLight,
    secondary = StudyHubColors.Secondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF064E3B),
    onSecondaryContainer = Color(0xFFA7F3D0),
    tertiary = StudyHubColors.Accent,
    onTertiary = Color(0xFF1C1917),
    tertiaryContainer = Color(0xFF78350F),
    onTertiaryContainer = Color(0xFFFCD34D),
    background = StudyHubColors.Bg,
    onBackground = StudyHubColors.TextPrimary,
    surface = StudyHubColors.Surface,
    onSurface = StudyHubColors.TextPrimary,
    surfaceVariant = StudyHubColors.SurfaceLight,
    onSurfaceVariant = StudyHubColors.TextSecondary,
    surfaceTint = StudyHubColors.Primary,
    outline = StudyHubColors.TextTertiary,
    outlineVariant = StudyHubColors.SurfaceLight,
    error = StudyHubColors.Danger,
    onError = Color.White,
    errorContainer = Color(0xFF4C0519),
    onErrorContainer = Color(0xFFFDA4AF),
)

private val StudyHubShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
)

@Composable
fun StudyHubTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = StudyHubColorScheme,
        shapes = StudyHubShapes,
        // TODO: cargar la fuente Inter como recurso y aplicarla aquí
        content = content,
    )
}
