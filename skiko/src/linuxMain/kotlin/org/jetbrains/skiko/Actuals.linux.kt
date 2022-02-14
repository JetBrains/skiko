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