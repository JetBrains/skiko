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

internal actual fun ClipboardManager_hasText(): Boolean = UIPasteboard.generalPasteboard.hasStrings()

// TODO: not sure if correct.
actual typealias Cursor = Any

internal actual fun CursorManager_setCursor(component: Any, cursor: Cursor) {}

internal actual fun CursorManager_getCursor(component: Any): Cursor? = null

internal actual fun getCursorById(id: PredefinedCursorsId): Cursor =
    when (id) {
        PredefinedCursorsId.DEFAULT -> Any()
        PredefinedCursorsId.CROSSHAIR -> Any()
        PredefinedCursorsId.HAND -> Any()
        PredefinedCursorsId.TEXT -> Any()
    }