package org.jetbrains.skiko.redrawer

import org.jetbrains.skiko.Library

internal class DisplayLinkThrottler {
    private val implPtr = create()

    internal fun dispose() = dispose(implPtr)

    /*
     * Creates a DisplayLink if needed with refresh rate matching NSScreen of NSWindow passed in [windowPtr].
     * If DisplayLink is already active, blocks until next vsync for physical screen of NSWindow passed in [windowPtr].
     */
    internal fun waitVSync(windowPtr: Long) = waitVSync(implPtr, windowPtr)

    private external fun create(): Long

    private external fun dispose(implPtr: Long)

    private external fun waitVSync(implPtr: Long, windowPtr: Long)

    companion object {
        init {
            Library.load()
        }
    }
}