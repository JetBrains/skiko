package org.jetbrains.skiko

import platform.AppKit.NSPasteboard
import platform.AppKit.NSPasteboardTypeString
import platform.AppKit.NSWorkspace
import platform.Foundation.NSURL

internal actual fun URIHandler_openUri(uri: String) {
    NSWorkspace.sharedWorkspace.openURL(NSURL.URLWithString(uri)!!)
}

internal actual fun ClipboardManager_setText(text: String) {
    NSPasteboard.generalPasteboard.setString(string = text, forType = NSPasteboardTypeString)
}

internal actual fun ClipboardManager_getText(): String? {
    return NSPasteboard.generalPasteboard.stringForType(dataType = NSPasteboardTypeString)
}