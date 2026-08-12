package org.studyhub.project.net

/** Log de errores por plataforma (Logcat en Android, consola en iOS). */
expect fun logError(tag: String, message: String)
