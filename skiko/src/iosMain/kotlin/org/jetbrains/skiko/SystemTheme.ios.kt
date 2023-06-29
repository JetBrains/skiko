package org.jetbrains.skiko

import platform.UIKit.UITraitCollection
import platform.UIKit.UIUserInterfaceStyle.UIUserInterfaceStyleDark
import platform.UIKit.UIUserInterfaceStyle.UIUserInterfaceStyleLight
import platform.UIKit.currentTraitCollection

actual val currentSystemTheme: SystemTheme
    get() = if (available(OS.Ios to OSVersion(13))) {
        /*
         * Getting trait collection for the current execution context supports only on iOS 13.0+
         * https://developer.apple.com/documentation/uikit/uitraitcollection/3238080-currenttraitcollection?language=objc
         */
        when (UITraitCollection.currentTraitCollection.userInterfaceStyle) {
            UIUserInterfaceStyleDark -> SystemTheme.DARK
            UIUserInterfaceStyleLight -> SystemTheme.LIGHT
            else -> SystemTheme.UNKNOWN
        }
    } else {
        SystemTheme.LIGHT
    }
