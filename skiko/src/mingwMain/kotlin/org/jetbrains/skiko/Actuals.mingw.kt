package org.jetbrains.skiko

import platform.windows.SW_SHOWNORMAL
import platform.windows.ShellExecuteA

internal actual inline fun <R> maybeSynchronized(lock: Any, block: () -> R): R =
    block()

actual fun currentNanoTime(): Long = kotlin.system.getTimeNanos()

internal actual fun URIHandler_openUri(uri: String) {
    ShellExecuteA(null, "open", uri, null, null, SW_SHOWNORMAL)
}

internal actual fun ClipboardManager_setText(text: String) {
    TODO("Implement ClipboardManager_setText() on Linux")
}

internal actual fun ClipboardManager_getText(): String? {
    TODO("Implement ClipboardManager_getText() on Linux")
}

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
