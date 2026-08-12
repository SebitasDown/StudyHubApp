package org.studyhub.project.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.studyhub.project.StudyHubColors
import org.studyhub.project.ui.icons.AppIcon

// ─── Tarjeta de cristal ─────────────────────────────────────────────────

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    radius: Dp = 20.dp,
    contentPadding: Dp = 18.dp,
    background: Color = StudyHubColors.Glass,
    borderColor: Color = StudyHubColors.GlassBorder,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(radius))
            .background(background)
            .border(1.dp, borderColor, RoundedCornerShape(radius))
            .padding(contentPadding),
        content = content,
    )
}

// ─── Título de sección ──────────────────────────────────────────────────

@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    action: String? = null,
    onAction: (() -> Unit)? = null,
    icon: ImageVector? = null,
    iconTint: Color = StudyHubColors.PrimaryLight,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            AppIcon(icon, tint = iconTint, size = 18.dp)
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text,
            color = StudyHubColors.TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.weight(1f),
        )
        if (action != null && onAction != null) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onAction)
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = action,
                    color = StudyHubColors.PrimaryLight,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                )
                AppIcon(
                    Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    tint = StudyHubColors.PrimaryLight,
                    size = 16.dp,
                )
            }
        }
    }
}

// ─── Badge (píldora) ────────────────────────────────────────────────────

@Composable
fun Badge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    small: Boolean = false,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = if (small) 8.dp else 10.dp, vertical = if (small) 3.dp else 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = color,
            fontSize = if (small) 10.sp else 11.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

// ─── Icono en cuadro con gradiente (tile) ───────────────────────────────

@Composable
fun IconTile(
    icon: ImageVector,
    gradient: List<Color>,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    iconTint: Color = Color.White,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(13.dp))
            .background(Brush.linearGradient(gradient)),
        contentAlignment = Alignment.Center,
    ) {
        AppIcon(icon, tint = iconTint, size = (size.value * 0.5f).dp)
    }
}

// ─── Tarjeta de estadística (dashboard) ─────────────────────────────────

@Composable
fun StatCard(
    icon: ImageVector,
    tint: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    GlassCard(modifier = modifier, radius = 18.dp, contentPadding = 14.dp) {
        IconTile(
            icon = icon,
            gradient = listOf(tint.copy(alpha = 0.9f), tint.copy(alpha = 0.55f)),
            size = 38.dp,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = value,
            color = StudyHubColors.TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            text = label,
            color = StudyHubColors.TextSecondary,
            fontSize = 11.5.sp,
            lineHeight = 15.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

// ─── Barra de progreso con gradiente ────────────────────────────────────

@Composable
fun GlassProgress(
    progress: Float,
    color: Color = StudyHubColors.Primary,
    modifier: Modifier = Modifier,
    track: Color = StudyHubColors.SurfaceLight,
    height: Dp = 8.dp,
) {
    val safe = progress.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(999.dp))
            .background(track),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(safe)
                .height(height)
                .clip(RoundedCornerShape(999.dp))
                .background(Brush.linearGradient(listOf(color, color.copy(alpha = 0.7f)))),
        )
    }
}

// ─── Botón primario ─────────────────────────────────────────────────────

@Composable
fun PrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(13.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .shadow(12.dp, shape, spotColor = StudyHubColors.Primary.copy(alpha = 0.35f))
            .then(
                if (enabled) {
                    Modifier.background(Brush.linearGradient(listOf(StudyHubColors.Primary, StudyHubColors.PrimaryDark)))
                } else {
                    Modifier.background(StudyHubColors.SurfaceLight)
                },
            )
            .clickable(enabled = enabled && !loading, onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.5.dp,
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    AppIcon(icon, tint = Color.White, size = 18.dp)
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

// ─── Chip seleccionable ─────────────────────────────────────────────────

@Composable
fun GlassChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = StudyHubColors.PrimaryLight,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) color.copy(alpha = 0.18f) else StudyHubColors.Surface)
            .border(
                1.dp,
                if (selected) color.copy(alpha = 0.6f) else StudyHubColors.Border,
                RoundedCornerShape(999.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = if (selected) color else StudyHubColors.TextSecondary,
            fontSize = 12.5.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        )
    }
}

// ─── Estados de carga / error / vacío ───────────────────────────────────

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = StudyHubColors.Primary, strokeWidth = 3.dp)
    }
}

@Composable
fun ErrorState(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AppIcon(Icons.Outlined.ErrorOutline, tint = StudyHubColors.DangerLight, size = 40.dp)
        Spacer(Modifier.height(12.dp))
        Text(
            text = message,
            color = StudyHubColors.TextSecondary,
            fontSize = 13.5.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 30.dp),
        )
        if (onRetry != null) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Reintentar",
                color = StudyHubColors.PrimaryLight,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(StudyHubColors.Surface)
                    .clickable(onClick = onRetry)
                    .padding(horizontal = 22.dp, vertical = 10.dp),
            )
        }
    }
}

@Composable
fun EmptyState(text: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = text,
            color = StudyHubColors.TextTertiary,
            fontSize = 13.5.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
    }
}

// ─── Fila de lista con icono ────────────────────────────────────────────

@Composable
fun RowScope.LeadingIcon(
    icon: ImageVector,
    tint: Color,
    size: Dp = 40.dp,
    iconSize: Dp = 20.dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(tint.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center,
    ) {
        AppIcon(icon, tint = tint, size = iconSize)
    }
}

// ─── Anillo de puntaje (circular) ───────────────────────────────────────

@Composable
fun ScoreRing(
    score: Int,
    label: String,
    modifier: Modifier = Modifier,
    color: Color = StudyHubColors.PrimaryLight,
    size: Dp = 96.dp,
) {
    val stroke = 9.dp
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Canvas(Modifier.size(size)) {
            val arcSize = size.toPx() - stroke.toPx()
            val topLeft = androidx.compose.ui.geometry.Offset(stroke.toPx() / 2, stroke.toPx() / 2)
            drawArc(
                color = StudyHubColors.SurfaceLight,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = androidx.compose.ui.geometry.Size(arcSize, arcSize),
                style = androidx.compose.ui.graphics.drawscope.Stroke(stroke.toPx()),
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * (score.coerceIn(0, 100) / 100f),
                useCenter = false,
                topLeft = topLeft,
                size = androidx.compose.ui.geometry.Size(arcSize, arcSize),
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    stroke.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                ),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$score%",
                color = StudyHubColors.TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = label,
                color = StudyHubColors.TextTertiary,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

// ─── Campo de texto glass ───────────────────────────────────────────────

@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    placeholder: String = "",
    singleLine: Boolean = true,
    isPassword: Boolean = false,
) {
    androidx.compose.material3.OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        shape = RoundedCornerShape(12.dp),
        textStyle = androidx.compose.ui.text.TextStyle(
            color = StudyHubColors.TextPrimary,
            fontSize = 14.sp,
        ),
        label = if (label.isNotEmpty()) {
            { Text(label, color = StudyHubColors.TextSecondary, fontSize = 13.sp) }
        } else null,
        placeholder = if (placeholder.isNotEmpty()) {
            { Text(placeholder, color = StudyHubColors.TextTertiary, fontSize = 14.sp) }
        } else null,
        visualTransformation = if (isPassword) {
            androidx.compose.ui.text.input.PasswordVisualTransformation()
        } else {
            androidx.compose.ui.text.input.VisualTransformation.None
        },
        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
            focusedBorderColor = StudyHubColors.Primary,
            unfocusedBorderColor = StudyHubColors.Border,
            focusedContainerColor = StudyHubColors.Surface,
            unfocusedContainerColor = StudyHubColors.Surface,
            focusedTextColor = StudyHubColors.TextPrimary,
            unfocusedTextColor = StudyHubColors.TextPrimary,
            cursorColor = StudyHubColors.Primary,
            focusedPlaceholderColor = StudyHubColors.TextTertiary,
            unfocusedPlaceholderColor = StudyHubColors.TextTertiary,
            focusedLabelColor = StudyHubColors.PrimaryLight,
            unfocusedLabelColor = StudyHubColors.TextSecondary,
        ),
    )
}

// ─── Avatar de iniciales ─────────────────────────────────────────────────

@Composable
fun InitialsAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    gradient: List<Color> = listOf(StudyHubColors.Primary, StudyHubColors.PrimaryDark),
) {
    val initials = name
        .trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .mapNotNull { it.firstOrNull() }
        .joinToString("")
        .uppercase()
        .ifEmpty { "?" }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.linearGradient(gradient)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            color = Color.White,
            fontSize = (size.value * 0.38f).sp,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}
