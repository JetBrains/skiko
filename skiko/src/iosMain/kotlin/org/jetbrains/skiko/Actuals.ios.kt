package org.jetbrains.skiko

import platform.Foundation.NSURL.Companion.URLWithString
import platform.UIKit.UIApplication
import platform.UIKit.UIPasteboard

internal actual fun URIHandler_openUri(uri: String) {
    UIApplication.sharedApplication.openURL(URLWithString(uri)!!)
}

internal actual fun ClipboardManager_setText(text: String) {
    UIPasteboard.generalPasteboard.string = text
}
internal actual fun ClipboardManager_getText(): String? {
    return UIPasteboard.generalPasteboard.string
}