package org.jetbrains.skiko

import org.jetbrains.skiko.w3c.window

actual val currentSystemTheme: SystemTheme
    get() = when(val matchMedia = window.matchMedia) {
        null -> SystemTheme.UNKNOWN
        else -> when(matchMedia("(prefers-color-scheme: dark)").matches) {
            true -> SystemTheme.DARK
            else -> SystemTheme.LIGHT
        }
    }
