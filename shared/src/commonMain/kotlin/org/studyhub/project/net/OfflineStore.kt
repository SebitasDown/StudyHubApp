package org.studyhub.project.net

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/** Estado global de conectividad, observable por la UI (banner "sin conexión"). */
object OfflineState {
    var isOnline: Boolean = true
        private set

    /** Cantidad de acciones pendientes de sincronizar (para mostrarla en la UI). */
    var pendingCount: Int = 0
        private set

    /** Callback que la UI registra para recomponerse cuando cambia la conectividad. */
    var onChanged: (() -> Unit)? = null

    fun setOnline(value: Boolean) {
        if (isOnline != value) {
            isOnline = value
            onChanged?.invoke()
        }
    }

    fun setPending(count: Int) {
        if (pendingCount != count) {
            pendingCount = count
            onChanged?.invoke()
        }
    }
}

/**
 * Caché en disco de las respuestas GET (SharedPreferences en Android, NSUserDefaults en iOS).
 * Cada respuesta exitosa se guarda con su clave; al fallar la red se sirve la última copia.
 */
object OfflineCache {
    private const val PREFIX = "cache:"
    private val json = Json { ignoreUnknownKeys = true }

    fun save(key: String, jsonBody: String) {
        TokenStore.saveString(PREFIX + key, jsonBody)
    }

    fun load(key: String): String? =
        TokenStore.loadString(PREFIX + key)?.takeIf { it.isNotBlank() }

    fun remove(key: String) {
        TokenStore.removeString(PREFIX + key)
    }
}

/** Acción de escritura (POST/PUT/DELETE) que no se pudo enviar por falta de red. */
@Serializable
data class PendingAction(
    val method: String,
    val url: String,
    val body: String? = null,
)

/** Cola persistente de acciones pendientes; se reproduce en orden al volver la conexión. */
object PendingQueue {
    private const val KEY = "offline_queue"
    private val json = Json { ignoreUnknownKeys = true }

    fun add(action: PendingAction) {
        replace(all() + action)
    }

    fun all(): List<PendingAction> =
        TokenStore.loadString(KEY)
            ?.let { runCatching { json.decodeFromString<List<PendingAction>>(it) }.getOrNull() }
            ?: emptyList()

    fun replace(list: List<PendingAction>) {
        TokenStore.saveString(KEY, json.encodeToString(ListSerializer(PendingAction.serializer()), list))
        OfflineState.setPending(list.size)
    }

    fun clear() {
        TokenStore.removeString(KEY)
        OfflineState.setPending(0)
    }
}
