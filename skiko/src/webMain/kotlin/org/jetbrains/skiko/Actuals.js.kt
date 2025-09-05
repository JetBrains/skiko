package org.jetbrains.skiko

import org.jetbrains.skiko.w3c.window

internal actual inline fun <R> maybeSynchronized(lock: Any, block: () -> R): R =
    block()

actual fun currentNanoTime(): Long = (window.performance.now() * 1_000_000).toLong()

internal actual fun URIHandler_openUri(uri: String) {
    window.open(uri, target = "_blank")
}

internal actual fun ClipboardManager_setText(text: String) {
    window.navigator.clipboard.writeText(text)
}

internal actual fun ClipboardManager_getText(): String? {
    // TODO("implement ClipboardManager_getText")
    // kotlinx.browser.window.navigator.clipboard.readText()
    return null
}

internal actual fun ClipboardManager_hasText(): Boolean = !ClipboardManager_getText().isNullOrEmpty()

actual typealias Cursor = String

internal actual fun getCursorById(id: PredefinedCursorsId): Cursor =
    when (id) {
        PredefinedCursorsId.DEFAULT -> "default"
        PredefinedCursorsId.CROSSHAIR -> "crosshair"
        PredefinedCursorsId.HAND -> "pointer"
        PredefinedCursorsId.TEXT -> "text"
    }

internal actual fun loadOpenGLLibrary() {
   // Nothing to do here
}

internal actual fun loadAngleLibrary() {
    // Nothing to do here
}