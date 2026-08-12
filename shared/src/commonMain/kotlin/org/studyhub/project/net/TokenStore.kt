package org.studyhub.project.net

/** Almacena el JWT y claves de preferencias de forma persistente por plataforma (SharedPreferences en Android, NSUserDefaults en iOS). */
expect object TokenStore {
    fun save(token: String)
    fun load(): String?
    fun clear()

    /** Guarda una preferencia arbitraria (ej. profesor IA elegido, id de conversación). */
    fun saveString(key: String, value: String)

    /** Lee una preferencia arbitraria; null si no existe. */
    fun loadString(key: String): String?

    /** Elimina una preferencia arbitraria. */
    fun removeString(key: String)

    /** Borra TODOS los datos guardados (token, caché, cola pendiente). Se usa al cerrar sesión. */
    fun clearAll()
}
