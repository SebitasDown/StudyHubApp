package org.studyhub.project.net

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Puente del flujo Google OAuth.
 *
 * Flujo: la app abre el navegador con [authUrl] → Google → el backend redirige a
 * `${FRONTEND_URL}/auth/callback?token=...&user=...` → esa página (o un bridge como
 * `web/oauth-bridge.html`) reenvía al deep link `studyhub://auth/callback?token=...&user=...`
 * → la plataforma abre la app y llama a [handleCallback], que guarda el JWT y notifica a Compose.
 */
object GoogleOAuth {
    var pendingToken by mutableStateOf<String?>(null)
        private set

    fun authUrl(): String = "$BASE_URL/auth/google"

    /** Lo llaman AuthCallbackActivity (Android) y el onOpenURL (iOS) al recibir el deep link. */
    fun handleCallback(token: String?, userJson: String?) {
        if (!token.isNullOrBlank()) {
            TokenStore.save(token)
            pendingToken = token
        }
    }

    /** Consume el token pendiente (para que no se re-aplique en cada recomposición). */
    fun consume(): String? {
        val token = pendingToken
        pendingToken = null
        return token
    }
}
