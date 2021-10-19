package org.jetbrains.skiko

import platform.UIKit.*
import platform.UIKit.UIUserInterfaceStyle.*

val currentSystemTheme: SystemTheme
    get() = when (UITraitCollection.currentTraitCollection.userInterfaceStyle) {
        UIUserInterfaceStyleDark -> SystemTheme.DARK
        UIUserInterfaceStyleLight -> SystemTheme.LIGHT
        else -> SystemTheme.UNKNOWN
    }
