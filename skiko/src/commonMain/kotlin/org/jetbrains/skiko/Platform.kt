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
    /**
     * Set current system clipboard content as text.
     */
    open fun setText(text: String) = ClipboardManager_setText(text)
    /**
     * Get current system clipboard content as text.
     */
    open fun getText(): String? = ClipboardManager_getText()

    /**
     * Returns true, if clipboard contains text
     */
    open fun hasText(): Boolean = ClipboardManager_hasText()
}

internal expect fun ClipboardManager_setText(text: String)
internal expect fun ClipboardManager_getText(): String?
internal expect fun ClipboardManager_hasText(): Boolean

/**
 * Manager to control cursor per native component.
 */
open class CursorManager {
    /**
     * Set cursor for the given component.
     */
    open fun setCursor(component: Any?, cursor: Cursor) {
        if (component != null) CursorManager_setCursor(component, cursor)
    }
    /**
     * Get cursor for the given component.
     */
    open fun getCursor(component: Any?): Cursor? = if (component != null) {
        CursorManager_getCursor(component)
    } else { null }
}

/**
 * Sets cursor for the platform component.
 */
internal expect fun CursorManager_setCursor(component: Any, cursor: Cursor)

/**
 * Gets current cursor for the platform component, or null if not defined or known.
 */
internal expect fun CursorManager_getCursor(component: Any): Cursor?
