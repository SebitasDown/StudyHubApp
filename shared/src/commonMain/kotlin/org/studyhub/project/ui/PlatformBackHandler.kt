package org.studyhub.project.ui

import androidx.compose.runtime.Composable

/**
 * Intercepta el botón back del sistema (Android): si [enabled] y el usuario lo presiona,
 * ejecuta [onBack] en lugar de cerrar la app. En iOS es un no-op (no hay botón back).
 */
@Composable
expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)
