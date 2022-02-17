package org.jetbrains.skiko

import org.w3c.dom.HTMLElement

internal actual inline fun <R> maybeSynchronized(lock: Any, block: () -> R): R =
    block()

actual fun currentNanoTime(): Long = (kotlinx.browser.window.performance.now() * 1_000_000).toLong()

internal actual fun URIHandler_openUri(uri: String) {
    kotlinx.browser.window.open(uri, target = "_blank")
}

internal actual fun ClipboardManager_setText(text: String) {
    kotlinx.browser.window.navigator.clipboard.writeText(text)
}

internal actual fun ClipboardManager_getText(): String? {
    // TODO("implement ClipboardManager_getText")
    // kotlinx.browser.window.navigator.clipboard.readText()
    return null
}

actual typealias Cursor = String

internal actual fun CursorManager_setCursor(component: Any, cursor: Cursor) {
    if (component is HTMLElement) {
        component.style.cursor = cursor
    }
}

internal actual fun CursorManager_getCursor(component: Any): Cursor? {
    return if (component is HTMLElement) {
        component.style.cursor
    } else {
        null
    }
}

internal actual fun getCursorById(id: PredefinedCursorsId): Cursor =
    when (id) {
        PredefinedCursorsId.DEFAULT -> "default"
        PredefinedCursorsId.CROSSHAIR -> "crosshair"
        PredefinedCursorsId.HAND -> "pointer"
        PredefinedCursorsId.TEXT -> "text"
    }