package org.jetbrains.skiko

import platform.UIKit.*

// tvOS doesn't have support for clipboard
internal actual fun ClipboardManager_setText(text: String) {
}

internal actual fun ClipboardManager_getText(): String? {
    return null
}

internal actual fun ClipboardManager_hasText(): Boolean = false

internal actual fun UIView.skikoInitializeUIView() {
    userInteractionEnabled = true
}
