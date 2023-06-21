package org.jetbrains.skiko

import platform.Foundation.NSURL.Companion.URLWithString
import platform.UIKit.UIApplication

internal actual fun URIHandler_openUri(uri: String) {
    UIApplication.sharedApplication.openURL(URLWithString(uri)!!)
}

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