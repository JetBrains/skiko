package org.jetbrains.skiko

import org.jetbrains.skia.Rect

class TextMenuArguments(
    /**
     * Rectangle of selected text area
     */
    val targetRect: Rect,
    /**
     * Copy action. If null, then copy is not possible in current context
     */
    val onCopyRequested: (() -> Unit)?,
    /**
     * Paste action. If null, then paste is not possible in current context
     */
    val onPasteRequested: (() -> Unit)?,
    /**
     * Cut action. If null, then cut is not possible in current context
     */
    val onCutRequested: (() -> Unit)?,
    /**
     * SelectAll action. If null, then select all is not possible in current context
     */
    val onSelectAllRequested: (() -> Unit)?
)
