package org.jetbrains.skiko

import platform.AppKit.*
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

actual typealias Cursor = NSCursor

// TODO: not sure if it is correct.
internal actual fun CursorManager_setCursor(component: Any, cursor: Cursor) {
    if (component is NSView) {
        component.resetCursorRects()
        component.addCursorRect(component.bounds(), cursor)
    }
}

internal actual fun CursorManager_getCursor(component: Any): Cursor? {
    return if (component is NSView) {
        // TODO: likely incorrect.
        NSCursor.currentCursor
    } else {
        null
    }
}

internal actual fun getCursorById(id: PredefinedCursorsId): Cursor =
    when (id) {
        PredefinedCursorsId.DEFAULT -> NSCursor.arrowCursor
        PredefinedCursorsId.CROSSHAIR -> NSCursor.crosshairCursor
        PredefinedCursorsId.HAND -> NSCursor.pointingHandCursor
        PredefinedCursorsId.TEXT -> NSCursor.IBeamCursor
    }