package org.jetbrains.skiko

actual fun URIHandler_openUri(uri: String) {
    TODO("Implement URIHandler_openUri() on Android Native")
}

internal actual fun ClipboardManager_setText(text: String) {
    TODO("Implement ClipboardManager_setText() on Android Native")
}

internal actual fun ClipboardManager_getText(): String? {
    TODO("Implement ClipboardManager_getText() on Android Native")
}

internal actual fun ClipboardManager_hasText(): Boolean = !ClipboardManager_getText().isNullOrEmpty()

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
