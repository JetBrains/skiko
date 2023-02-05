package org.jetbrains.skiko

actual fun URIHandler_openUri(uri: String) {
    TODO("Implement URIHandler_openUri() on Linux")
}

internal actual fun ClipboardManager_setText(text: String) {
    TODO("Implement ClipboardManager_setText() on Linux")
}

internal actual fun ClipboardManager_getText(): String? {
    TODO("Implement ClipboardManager_getText() on Linux")
}

internal actual fun ClipboardManager_hasText(): Boolean = !ClipboardManager_getText().isNullOrEmpty()

actual typealias Cursor = Any

internal actual fun CursorManager_setCursor(component: Any, cursor: Cursor) {
    TODO("Implement CursorManager_setCursor on Linux")
}

internal actual fun CursorManager_getCursor(component: Any): Cursor? {
    TODO("Implement CursorManager_getCursor on Linux")
}

internal actual fun getCursorById(id: PredefinedCursorsId): Cursor =
    when (id) {
        PredefinedCursorsId.DEFAULT -> Any()
        PredefinedCursorsId.CROSSHAIR -> Any()
        PredefinedCursorsId.HAND -> Any()
        PredefinedCursorsId.TEXT -> Any()
    }