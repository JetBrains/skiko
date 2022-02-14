package org.jetbrains.skiko

import kotlinx.coroutines.await

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
    TODO("implement ClipboardManager_getText")
    // kotlinx.browser.window.navigator.clipboard.readText()
}