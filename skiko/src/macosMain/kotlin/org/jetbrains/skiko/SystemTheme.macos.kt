package org.jetbrains.skiko

import platform.AppKit.*
import platform.Foundation.*

val currentSystemTheme: SystemTheme
    get() = when (NSUserDefaults.standardUserDefaults.stringForKey("AppleInterfaceStyle")) {
        "Dark" -> SystemTheme.DARK
        else -> SystemTheme.LIGHT
    }