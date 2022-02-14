package org.jetbrains.skiko

import platform.AppKit.NSWorkspace
import platform.Foundation.NSURL

actual fun openUri(uri: String) {
    NSWorkspace.sharedWorkspace.openURL(NSURL.URLWithString(uri)!!)
}