package org.jetbrains.skiko.sample.js

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform