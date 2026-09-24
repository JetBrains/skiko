package org.jetbrains.skiko.sample.js

internal class JsPlatform: Platform {
    override val name: String = "Kotlin/JS"
}

actual fun getPlatform(): Platform = JsPlatform()