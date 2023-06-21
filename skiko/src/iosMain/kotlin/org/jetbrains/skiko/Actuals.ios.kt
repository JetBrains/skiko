package org.jetbrains.skiko

import platform.UIKit.UIPasteboard

internal actual fun ClipboardManager_setText(text: String) {
    UIPasteboard.generalPasteboard.string = text
}
internal actual fun ClipboardManager_getText(): String? {
    return UIPasteboard.generalPasteboard.string
}

internal actual fun ClipboardManager_hasText(): Boolean = UIPasteboard.generalPasteboard.hasStrings()
