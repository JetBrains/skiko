package org.jetbrains.skiko

actual val currentSystemTheme: SystemTheme
    get() {
        Library.load()
        return when (getCurrentSystemTheme()) {
            0 -> SystemTheme.LIGHT
            1 -> SystemTheme.DARK
            else -> SystemTheme.UNKNOWN
        }
    }

// Common
private external fun getCurrentSystemTheme(): Int
