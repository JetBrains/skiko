package org.jetbrains.skiko

import platform.Foundation.NSURL.Companion.URLWithString
import platform.UIKit.UIApplication

actual fun openUri(uri: String) {
    UIApplication.sharedApplication.openURL(URLWithString(uri)!!)
}