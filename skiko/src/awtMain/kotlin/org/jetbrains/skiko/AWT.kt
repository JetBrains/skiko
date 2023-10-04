package org.jetbrains.skiko

import java.awt.Canvas
import java.awt.Component

private val awt = getAWT().also {
    check(it != 0L) { "Can't get AWT" }
}

internal fun <T> Canvas.useDrawingSurfacePlatformInfo(
    block: (Long) -> T
) = useDrawingSurfaceInfo { block(it.platformInfo) }

internal fun <T> Canvas.useDrawingSurfaceInfo(
    block: (DrawingSurfaceInfo) -> T
): T = getDrawingSurface().use { drawingSurface ->
    drawingSurface.withLock {
        drawingSurface.getInfo().use { info ->
            block(info)
        }
    }
}

internal fun Component.getDrawingSurface() = DrawingSurface(this)

internal class DrawingSurface(
    component: Component
) : AutoCloseable {
    var ptr =
        getDrawingSurface(awt, component).also {
            check(it != 0L) { "Can't get DrawingSurface" }
        }
        private set

    fun lock() = lockDrawingSurface(ptr).also { check(it == 0) { "Can't lock DrawingSurface" } }

    fun unlock() = unlockDrawingSurface(ptr)

    inline fun <T> withLock(block: () -> T): T {
        lock()
        try {
            return block()
        } finally {
            unlock()
        }
    }

    fun getInfo(): DrawingSurfaceInfo {
        check(ptr != 0L) { "DrawingSurface.ptr is 0L. DrawingSurface might've been closed." }
        return DrawingSurfaceInfo(ptr)
    }

    override fun close() {
        freeDrawingSurface(awt, ptr)
        ptr = 0
    }
}

internal class DrawingSurfaceInfo(
    private val drawingSurface: Long
) : AutoCloseable {
    var ptr =
        getDrawingSurfaceInfo(drawingSurface).also {
            check(it != 0L) { "Can't get DrawingSurfaceInfo" }
        }
        private set

    val platformInfo: Long
        get() {
            check(ptr != 0L) { "DrawingSurfaceInfo.ptr is 0L. DrawingSurfaceInfo might've been closed." }
            return getPlatformInfo(ptr).also {
                check(it != 0L) { "Can't get platformInfo" }
            }
        }

    override fun close() {
        freeDrawingSurfaceInfo(drawingSurface, ptr)
        ptr = 0
    }
}

private external fun getAWT(): Long

private external fun getDrawingSurface(awt: Long, layer: Component): Long
private external fun freeDrawingSurface(awt: Long, drawingSurface: Long)

private external fun lockDrawingSurface(drawingSurface: Long): Int
private external fun unlockDrawingSurface(drawingSurface: Long)

private external fun getDrawingSurfaceInfo(drawingSurface: Long): Long
private external fun freeDrawingSurfaceInfo(drawingSurface: Long, drawingSurfaceInfo: Long)

private external fun getPlatformInfo(drawingSurfaceInfo: Long): Long