package org.jetbrains.skiko

enum class SystemTheme {
    DARK,
    LIGHT,
    UNKNOWN
}

expect val currentSystemTheme: SystemTheme