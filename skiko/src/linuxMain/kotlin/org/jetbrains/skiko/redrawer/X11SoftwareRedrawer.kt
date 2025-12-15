@file:OptIn(ExperimentalForeignApi::class)

package org.jetbrains.skiko.redrawer

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.Pinned
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.invoke
import kotlinx.cinterop.pin
import kotlinx.cinterop.pointed
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.Surface
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.X11SkikoWindow
import org.jetbrains.skiko.currentNanoTime
import org.jetbrains.skiko.internal.x11.*

internal class X11SoftwareRedrawer(
    private val layer: SkiaLayer,
) : Redrawer {
    private val window: X11SkikoWindow = requireNotNull(layer.x11Window) {
        "X11SoftwareRedrawer requires SkiaLayer to be attached to X11SkikoWindow"
    }
    private val display = window.display
    private val xWindow = window.window
    private val gc = window.gc

    override val renderInfo: String = "Native software (X11/XPutImage)"

    private var bufferWidth: Int = 0
    private var bufferHeight: Int = 0
    private var pinnedPixels: Pinned<ByteArray>? = null
    private var xImage: CPointer<XImage>? = null
    private var surface: Surface? = null

    override fun dispose() {
        destroyBackBuffer()
    }

    override fun needRender(throttledToVsync: Boolean) {
        window.requestRender(throttledToVsync)
    }

    override fun syncBounds() {
        // Backbuffer is recreated lazily in renderImmediately().
    }

    override fun update(nanoTime: Long) {
        layer.update(nanoTime)
    }

    override fun renderImmediately() {
        recreateBackBufferIfNeeded()

        val surface = surface ?: return
        val xImage = xImage ?: return

        layer.update(currentNanoTime())

        val canvas = surface.canvas
        canvas.clear(0xFFFFFFFF.toInt())
        layer.draw(canvas)
        surface.flushAndSubmit()

        XPutImage(
            display,
            xWindow,
            gc,
            xImage,
            0,
            0,
            0,
            0,
            window.width.convert(),
            window.height.convert(),
        )
        XFlush(display)
    }

    private fun recreateBackBufferIfNeeded() {
        val w = window.width.coerceAtLeast(1)
        val h = window.height.coerceAtLeast(1)
        if (w == bufferWidth && h == bufferHeight && surface != null && xImage != null) return

        bufferWidth = w
        bufferHeight = h

        destroyBackBuffer()

        val buffer = ByteArray(w * h * 4)
        val pinned = buffer.pin()

        val rowBytes = w * 4
        val imageInfo = ImageInfo(
            width = w,
            height = h,
            colorType = ColorType.BGRA_8888,
            alphaType = ColorAlphaType.OPAQUE,
            colorSpace = ColorSpace.sRGB,
        )
        surface = Surface.makeRasterDirect(imageInfo, pinned.addressOf(0).rawValue, rowBytes)

        val visual = XDefaultVisual(display, window.screen)
        val depth = XDefaultDepth(display, window.screen)
        xImage = XCreateImage(
            display,
            visual,
            depth.convert(),
            ZPixmap,
            0,
            pinned.addressOf(0),
            w.convert(),
            h.convert(),
            32,
            rowBytes,
        )

        pinnedPixels = pinned
    }

    private fun destroyBackBuffer() {
        surface?.close()
        surface = null

        xImage?.let { img ->
            img.pointed.data = null
            destroyXImage(img)
        }
        xImage = null

        pinnedPixels?.unpin()
        pinnedPixels = null
    }
}

private fun destroyXImage(image: CPointer<XImage>) {
    val destroy = image.pointed.f.destroy_image
    if (destroy != null) {
        destroy(image)
    } else {
        XFree(image)
    }
}

