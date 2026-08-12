package org.studyhub.project.net

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter

actual fun platformCurrentTimeHM(): String {
    val formatter = NSDateFormatter().apply { dateFormat = "HH:mm" }
    return formatter.stringFromDate(NSDate())
}
