package org.jetbrains.skiko

actual val currentSystemTheme: SystemTheme
    get() = when (SystemThemeHelper.getCurrentSystemTheme()) {
        0 -> SystemTheme.LIGHT
        1 -> SystemTheme.DARK
        else -> SystemTheme.UNKNOWN
    }

private object SystemThemeHelper {
    init {
        Library.load()
    }

    external fun getCurrentSystemTheme(): Int
}
