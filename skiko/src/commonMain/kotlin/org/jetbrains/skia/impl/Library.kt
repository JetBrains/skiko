package org.jetbrains.skia.impl

/**
 * Object representing native library containing non-Kotlin part of Skiko.
 */
expect class Library {
    companion object {
        fun staticLoad()
    }
}
