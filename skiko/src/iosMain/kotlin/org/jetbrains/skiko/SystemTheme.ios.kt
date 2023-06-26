package org.jetbrains.skiko

import kotlinx.cinterop.useContents
import platform.Foundation.NSProcessInfo
import platform.UIKit.*
import platform.UIKit.UIUserInterfaceStyle.*

actual val currentSystemTheme: SystemTheme
    get() = if (supportsCurrentTraitCollectionApi) {
        when (UITraitCollection.currentTraitCollection.userInterfaceStyle) {
            UIUserInterfaceStyleDark -> SystemTheme.DARK
            UIUserInterfaceStyleLight -> SystemTheme.LIGHT
            else -> SystemTheme.UNKNOWN
        }
    } else {
        SystemTheme.LIGHT
    }

/*
 * Getting trait collection for the current execution context supports only on iOS 13.0+
 * https://developer.apple.com/documentation/uikit/uitraitcollection/3238080-currenttraitcollection?language=objc
 */
private val supportsCurrentTraitCollectionApi get() = NSProcessInfo.processInfo.operatingSystemVersion.useContents {
    majorVersion >= 13
}
