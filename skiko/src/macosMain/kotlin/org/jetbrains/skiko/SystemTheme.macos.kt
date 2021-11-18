package org.jetbrains.skiko

import platform.Foundation.NSUserDefaults

val currentSystemTheme: SystemTheme
    get() = when (NSUserDefaults.standardUserDefaults.stringForKey("AppleInterfaceStyle")) {
        "Dark" -> SystemTheme.DARK
        else -> SystemTheme.LIGHT
    }