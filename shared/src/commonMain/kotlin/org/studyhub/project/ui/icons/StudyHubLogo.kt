package org.studyhub.project.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Logo oficial de StudyHub: birrete de graduación dibujado con paths vectoriales.
 * Se usa ÚNICAMENTE en la pantalla de login (y como SVG en la web).
 * Es tintable: el color de relleno base es negro y se aplica el tinte al renderizar.
 */
val StudyHubLogo: ImageVector by lazy {
    ImageVector.Builder(
        name = "StudyHubLogo",
        defaultWidth = 48.dp,
        defaultHeight = 48.dp,
        viewportWidth = 48f,
        viewportHeight = 48f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            // Mortaja (diamante superior)
            moveTo(24f, 8f)
            lineTo(42f, 24f)
            lineTo(24f, 36f)
            lineTo(6f, 24f)
            close()

            // Botón superior (diamante pequeño)
            moveTo(24f, 3.1f)
            lineTo(26.4f, 5.5f)
            lineTo(24f, 7.9f)
            lineTo(21.6f, 5.5f)
            close()

            // Banda inferior con esquinas redondeadas
            moveTo(12f, 33f)
            horizontalLineToRelative(24f)
            arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, dx1 = 2f, dy1 = 2f)
            verticalLineToRelative(1f)
            arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, dx1 = -2f, dy1 = 2f)
            horizontalLineToRelative(-24f)
            arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, dx1 = -2f, dy1 = -2f)
            verticalLineToRelative(-1f)
            arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, dx1 = 2f, dy1 = -2f)
            close()

            // Borla (cordón)
            moveTo(35.6f, 26.8f)
            lineTo(38.2f, 28.4f)
            lineTo(43.8f, 42.8f)
            lineTo(41.2f, 44f)
            close()

            // Punta de la borla (círculo en dos arcos)
            moveTo(42f, 39.8f)
            arcToRelative(2.6f, 2.6f, 0f, isMoreThanHalf = true, isPositiveArc = false, dx1 = 0f, dy1 = 5.2f)
            arcToRelative(2.6f, 2.6f, 0f, isMoreThanHalf = true, isPositiveArc = false, dx1 = 0f, dy1 = -5.2f)
            close()
        }
    }.build()
}
