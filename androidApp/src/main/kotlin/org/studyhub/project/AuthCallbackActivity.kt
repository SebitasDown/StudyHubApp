package org.studyhub.project

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import org.studyhub.project.net.GoogleOAuth
import org.studyhub.project.net.TokenStore

/**
 * Recibe el deep link `studyhub://auth/callback?token=...&user=...` del flujo Google OAuth,
 * guarda el token y devuelve al usuario a la app principal.
 */
class AuthCallbackActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TokenStore.init(applicationContext)

        val data = intent?.data
        GoogleOAuth.handleCallback(
            token = data?.getQueryParameter("token"),
            userJson = data?.getQueryParameter("user"),
        )

        startActivity(
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
        )
        finish()
    }
}
