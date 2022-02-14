package org.jetbrains.skiko

/**
 * Manager for opening external links. Is open so that platform could provide an override if default doesn't fit.
 */
open class URIManager {
    /**
     * Asynchronous request to open a URI in system browser.
     * [uri] a universal resource identifier to open, exact set of supported APIs is platform dependent
     */

    open fun openUri(uri: String) = URIHandler_openUri(uri)
}

internal expect fun URIHandler_openUri(uri: String)

/**
 * Manager for controlling system clipboard. Is open so that platform could provide an override if default doesn't fit.
 */
open class ClipboardManager {
    open fun setText(text: String) = ClipboardManager_setText(text)
    open fun getText(): String? = ClipboardManager_getText()
}

internal expect fun ClipboardManager_setText(text: String)
internal expect fun ClipboardManager_getText(): String?

