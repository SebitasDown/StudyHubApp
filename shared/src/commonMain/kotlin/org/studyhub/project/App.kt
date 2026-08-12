package org.studyhub.project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.studyhub.project.net.GoogleOAuth
import org.studyhub.project.net.TokenStore
import org.studyhub.project.ui.MainApp

private enum class SessionState { CHECKING, LOGGED_OUT, LOGGED_IN }

/**
 * Raíz de la app: decide entre el login y la app principal según el token guardado.
 */
@Composable
@Preview
fun App() {
    StudyHubTheme {
        var state by remember { mutableStateOf(SessionState.CHECKING) }
        // Se recompone cuando llega el token del deep link de Google OAuth
        val oauthToken = GoogleOAuth.pendingToken

        LaunchedEffect(Unit) {
            state = if (TokenStore.load() != null) SessionState.LOGGED_IN else SessionState.LOGGED_OUT
        }

        LaunchedEffect(oauthToken) {
            if (oauthToken != null) {
                GoogleOAuth.consume()
                state = SessionState.LOGGED_IN
            }
        }

        // Fondo oscuro a todo lo ancho (detrás de la barra de estado) y el contenido
        // DENTRO del área segura: sin esto, el header/la campana chocan con los iconos
        // del sistema (batería, reloj) por el edge-to-edge.
        Box(Modifier.fillMaxSize().background(StudyHubColors.Bg)) {
            Box(Modifier.fillMaxSize().safeDrawingPadding()) {
                when (state) {
                    SessionState.CHECKING -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = StudyHubColors.Primary, strokeWidth = 3.dp)
                        }
                    }

                    SessionState.LOGGED_OUT -> LoginScreen(
                        onAuthenticated = { state = SessionState.LOGGED_IN },
                        onEnterDemo = { state = SessionState.LOGGED_IN },
                    )

                    SessionState.LOGGED_IN -> MainApp(onLogout = {
                        TokenStore.clear()
                        state = SessionState.LOGGED_OUT
                    })
                }
            }
        }
    }
}
