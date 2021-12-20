package org.jetbrains.skiko

actual val currentSystemTheme: SystemTheme
    // TODO: getting actual OS system theme
    get() = SystemTheme.UNKNOWN