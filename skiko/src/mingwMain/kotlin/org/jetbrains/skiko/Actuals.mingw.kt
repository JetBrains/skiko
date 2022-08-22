package org.jetbrains.skiko

import platform.windows.*

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

private val cursorCache = mutableMapOf<LPWSTR, HCURSOR>()

actual data class Cursor(val value: LPWSTR)

internal actual fun CursorManager_setCursor(component: Any, cursor: Cursor) {
    val c = cursorCache[cursor.value]
    if (c == null) {
        val hCursor = LoadCursorW(null, cursor.value) ?: throw Error("Failed to load cursor")
        cursorCache[cursor.value] = hCursor
        SetCursor(hCursor)
    } else {
        SetCursor(c)
    }
}

internal actual fun CursorManager_getCursor(component: Any): Cursor? {
    val cursor = GetCursor() ?: return null
    for (entry in cursorCache) {
        if(entry.value == cursor) {
            return Cursor(entry.key)
        }
    }
    return null
}

internal actual fun getCursorById(id: PredefinedCursorsId): Cursor =
    when (id) {
        PredefinedCursorsId.DEFAULT -> Cursor(IDC_ARROW!!)
        PredefinedCursorsId.CROSSHAIR -> Cursor(IDC_CROSS!!)
        PredefinedCursorsId.HAND -> Cursor(IDC_HAND!!)
        PredefinedCursorsId.TEXT -> Cursor(IDC_IBEAM!!)
    }
