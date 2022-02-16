package org.jetbrains.skiko

/**
 * Identifiers of predefined cursors.
 */
enum class PredefinedCursorsId {
    DEFAULT,
    CROSSHAIR,
    TEXT,
    HAND
}

/**
 * Predefined platform cursors.
 */
object PredefinedCursors {
    val DEFAULT: Cursor = getCursorById(PredefinedCursorsId.DEFAULT)
    val CROSSHAIR: Cursor = getCursorById(PredefinedCursorsId.CROSSHAIR)
    val TEXT: Cursor = getCursorById(PredefinedCursorsId.TEXT)
    val HAND: Cursor = getCursorById(PredefinedCursorsId.HAND)
}

/**
 * Pointer device cursor abstraction.
 */
expect class Cursor

internal expect fun getCursorById(id: PredefinedCursorsId): Cursor
