package org.jetbrains.skiko.macos

import kotlinx.coroutines.channels.Channel
import org.jetbrains.skiko.Library
import java.lang.RuntimeException

internal class DisplayLink(
    screenID: Long
) : AutoCloseable {
    @Volatile
    private var isDisposed = false

    private val ptr = nativeInit(screenID)

    init {
        if (ptr == 0L) {
            isDisposed = true
            throw DisplayLinkDisposed()
        }
    }

    private val channel = Channel<Unit>()

    override fun close() {
        // ptr can be 0, if
        if (ptr != 0L) {
            check(!isDisposed)
            nativeDispose(ptr)
            isDisposed = true
            channel.close(DisplayLinkDisposed())
        }
    }

    suspend fun await() {
        check(!isDisposed)
        channel.receive()
    }

    // Will be called in the other thread by macOs system on vsync
    internal fun outputCallback() {
        check(!isDisposed)
        channel.trySend(Unit)
    }

    private external fun nativeInit(screenId: Long): Long
    private external fun nativeDispose(displayLink: Long)

    companion object {
        init {
            Library.load()
        }
    }
}

internal class DisplayLinkDisposed : RuntimeException()