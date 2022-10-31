package org.jetbrains.skiko

interface TextActions {
    /**
     * Copy action. If null, then copy is not possible in current context
     */
    val copy: (() -> Unit)?

    /**
     * Paste action. If null, then paste is not possible in current context
     */
    val paste: (() -> Unit)?

    /**
     * Cut action. If null, then cut is not possible in current context
     */
    val cut: (() -> Unit)?

    /**
     * SelectAll action. If null, then select all is not possible in current context
     */
    val selectAll: (() -> Unit)?
}
