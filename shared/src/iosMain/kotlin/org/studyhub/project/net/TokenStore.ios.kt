package org.studyhub.project.net

import platform.Foundation.NSBundle
import platform.Foundation.NSUserDefaults

actual object TokenStore {
    private const val KEY_TOKEN = "studyhub_access_token"

    actual fun save(token: String) {
        NSUserDefaults.standardUserDefaults.setObject(token, forKey = KEY_TOKEN)
    }

    actual fun load(): String? =
        NSUserDefaults.standardUserDefaults.stringForKey(KEY_TOKEN)

    actual fun clear() {
        NSUserDefaults.standardUserDefaults.removeObjectForKey(KEY_TOKEN)
    }

    actual fun saveString(key: String, value: String) {
        NSUserDefaults.standardUserDefaults.setObject(value, forKey = key)
    }

    actual fun loadString(key: String): String? =
        NSUserDefaults.standardUserDefaults.stringForKey(key)

    actual fun removeString(key: String) {
        NSUserDefaults.standardUserDefaults.removeObjectForKey(key)
    }

    actual fun clearAll() {
        val domain = NSBundle.mainBundle.bundleIdentifier ?: "studyhub"
        NSUserDefaults.standardUserDefaults.removePersistentDomainForName(domain)
        NSUserDefaults.standardUserDefaults.synchronize()
    }
}
