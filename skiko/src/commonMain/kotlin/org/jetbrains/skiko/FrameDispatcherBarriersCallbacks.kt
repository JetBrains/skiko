package org.jetbrains.skiko

/**
 * Interface for abstracting a set of callbacks which are called in order to throttle rendering
 */
interface FrameDispatcherBarriersCallbacks {
    fun signalVsync()
    fun signalCompletion()
    fun enableVsyncBarrier()
    fun disableVsyncBarrier()
}
