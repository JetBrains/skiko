package org.jetbrains.skiko

actual val currentSystemTheme: SystemTheme
    // TODO: getting actual OS/browser system theme
    get() = SystemTheme.UNKNOWN