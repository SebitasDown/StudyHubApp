package org.studyhub.project.net

import android.util.Log

actual fun logError(tag: String, message: String) {
    Log.e(tag, message)
}
