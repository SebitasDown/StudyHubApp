package org.studyhub.project.net

import platform.Foundation.NSLog

actual fun logError(tag: String, message: String) {
    NSLog("[$tag] %@", message)
}
