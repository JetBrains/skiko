package org.jetbrains.skiko

val currentSystemTheme: SystemTheme
    get() = when (getCurrentSystemTheme()) {
        0 -> SystemTheme.LIGHT
        1 -> SystemTheme.DARK
        else -> SystemTheme.UNKNOWN
    }

// Common
external private fun getCurrentSystemTheme(): Int
