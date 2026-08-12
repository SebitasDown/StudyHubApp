package org.studyhub.project.net

actual fun platformCurrentTimeHM(): String =
    java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
