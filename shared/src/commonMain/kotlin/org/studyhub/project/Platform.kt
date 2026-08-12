package org.studyhub.project

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform