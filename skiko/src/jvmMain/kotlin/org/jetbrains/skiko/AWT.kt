package org.jetbrains.skiko

import java.awt.Component

private val awt = getAWT().also {
    check(it != 0L)
}

internal fun <T> Component.useDrawingSurfacePlatformInfo(
    block: (Long) -> T
) = useDrawingSurfaceInfo { block(it.platformInfo) }

internal fun <T> Component.useDrawingSurfaceInfo(
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
    val ptr = getDrawingSurface(awt, component).also {
        check(it != 0L)
    }

    fun lock() = lockDrawingSurface(ptr)

    fun unlock() = unlockDrawingSurface(ptr)

    inline fun <T> withLock(block: () -> T): T {
        lock()
        try {
            return block()
        } finally {
            unlock()
        }
    }

    fun getInfo() = DrawingSurfaceInfo(ptr)

    override fun close() {
        freeDrawingSurface(awt, ptr)
    }
}

internal class DrawingSurfaceInfo(
    private val drawingSurface: Long
) : AutoCloseable {
    val ptr = getDrawingSurfaceInfo(drawingSurface).also {
        check(it != 0L)
    }

    val platformInfo: Long get() = getPlatformInfo(ptr)

    override fun close() {
        freeDrawingSurfaceInfo(drawingSurface, ptr)
    }
}

private external fun getAWT(): Long

private external fun getDrawingSurface(awt: Long, layer: Component): Long
private external fun freeDrawingSurface(awt: Long, drawingSurface: Long)

private external fun lockDrawingSurface(drawingSurface: Long)
private external fun unlockDrawingSurface(drawingSurface: Long)

private external fun getDrawingSurfaceInfo(drawingSurface: Long): Long
private external fun freeDrawingSurfaceInfo(drawingSurface: Long, drawingSurfaceInfo: Long)

private external fun getPlatformInfo(drawingSurfaceInfo: Long): Long