package org.jetbrains.skiko.redrawer

import org.jetbrains.skiko.Library

internal class DisplayLinkThrottler(windowPtr: Long) {
    private val implPtr = create(windowPtr)

    internal fun dispose() = dispose(implPtr)

    /*
     * Creates a DisplayLink if needed with refresh rate matching NSScreen of NSWindow passed in [windowPtr].
     * If DisplayLink is already active, blocks until next vsync for physical screen of NSWindow passed in [windowPtr].
     */
    private external fun create(windowPtr: Long): Long

    fun waitVSync() = waitVSync(implPtr)

    private external fun dispose(implPtr: Long)

    private external fun waitVSync(implPtr: Long)

    companion object {
        init {
            Library.load()
        }
    }
}