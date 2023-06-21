package org.jetbrains.skiko

// tvOS doesn't have support for clipboard
internal actual fun ClipboardManager_setText(text: String) {
}

internal actual fun ClipboardManager_getText(): String? {
    return null
}

internal actual fun ClipboardManager_hasText(): Boolean = false
