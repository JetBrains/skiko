package org.jetbrains.skiko

import kotlinx.cinterop.*
import platform.windows.SW_SHOWNORMAL
import platform.windows.ShellExecute

actual typealias Cursor = Any

internal actual fun getCursorById(id: PredefinedCursorsId) : Cursor {
    return Any()
}

@OptIn(ExperimentalForeignApi::class)
internal actual fun URIHandler_openUri(uri: String) {
    memScoped {
        ShellExecute?.let { it(null, "open".wcstr.ptr, uri.wcstr.ptr, null, null, SW_SHOWNORMAL) }
    }
}

internal actual fun ClipboardManager_setText(text: String) {
    //TODO: Not implemented yet
}
internal actual fun ClipboardManager_getText(): String? {
    return null
}
internal actual fun ClipboardManager_hasText(): Boolean {
    return false
}

internal actual fun CursorManager_setCursor(component: Any, cursor: Cursor) {
    //TODO: Not implemented yet
}

internal actual fun CursorManager_getCursor(component: Any): Cursor? {
    return null
}