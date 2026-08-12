package org.studyhub.project.net

import android.annotation.SuppressLint
import android.content.Context

actual object TokenStore {
    private const val PREFS = "studyhub_session"
    private const val KEY_TOKEN = "access_token"

    @SuppressLint("StaticFieldLeak")
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private fun prefs() =
        appContext!!.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    actual fun save(token: String) {
        prefs().edit().putString(KEY_TOKEN, token).apply()
    }

    actual fun load(): String? = prefs().getString(KEY_TOKEN, null)

    actual fun clear() {
        prefs().edit().remove(KEY_TOKEN).apply()
    }

    actual fun saveString(key: String, value: String) {
        prefs().edit().putString(key, value).apply()
    }

    actual fun loadString(key: String): String? = prefs().getString(key, null)

    actual fun removeString(key: String) {
        prefs().edit().remove(key).apply()
    }

    actual fun clearAll() {
        prefs().edit().clear().apply()
    }
}
