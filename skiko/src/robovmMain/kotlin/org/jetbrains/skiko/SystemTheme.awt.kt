package org.jetbrains.skiko

import org.robovm.apple.foundation.NSProcessInfo
import org.robovm.apple.uikit.UITraitCollection
import org.robovm.apple.uikit.UIUserInterfaceStyle

// FIXME: have to keep it at .awt name as .desktop implementations uses actual that point to it
actual val currentSystemTheme: SystemTheme
    get() = if (NSProcessInfo.getSharedProcessInfo().operatingSystemVersion.majorVersion >= 13) {
        /*
         * Getting trait collection for the current execution context supports only on iOS 13.0+
         * https://developer.apple.com/documentation/uikit/uitraitcollection/3238080-currenttraitcollection?language=objc
         */
        when (UITraitCollection.getCurrentTraitCollection().userInterfaceStyle) {
            UIUserInterfaceStyle.Dark -> SystemTheme.DARK
            UIUserInterfaceStyle.Light -> SystemTheme.LIGHT
            else -> SystemTheme.UNKNOWN
        }
    } else {
        SystemTheme.LIGHT
    }
