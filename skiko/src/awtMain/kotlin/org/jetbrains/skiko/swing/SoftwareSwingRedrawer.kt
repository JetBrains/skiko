package org.jetbrains.skiko.swing

import org.jetbrains.skia.*
import org.jetbrains.skiko.GraphicsApi
import org.jetbrains.skiko.SkiaLayerAnalytics
import org.jetbrains.skiko.SkikoRenderDelegate
import org.jetbrains.skiko.autoCloseScope
import java.awt.Graphics2D

/**
 * Provides a way to draw on Skia canvas using software rendering without GPU acceleration and then draw it on [java.awt.Graphics2D].
 *
 * Content to draw is provided by [SkikoRenderDelegate].
 *
 * @see SwingRedrawerBase
 * @see SoftwareSwingDrawer
 */
internal class SoftwareSwingRedrawer(
    swingLayerProperties: SwingLayerProperties,
    private val renderDelegate: SkikoRenderDelegate,
    analytics: SkiaLayerAnalytics
) : SwingRedrawerBase(
    swingLayerProperties,
    analytics,
    GraphicsApi.SOFTWARE_FAST
) {
    init {
        onDeviceChosen("Software")
    }

    private val storage = Bitmap()

    init {
        onContextInit()
    }

    override fun dispose() {
        super.dispose()
        storage.close()
    }

    override fun onRender(g: Graphics2D, width: Int, height: Int, nanoTime: Long) = autoCloseScope {
        if (storage.width != width || storage.height != height) {
            storage.allocPixelsFlags(ImageInfo.makeS32(width, height, ColorAlphaType.PREMUL), false)
        }

        val pixelsPointer = storage.peekPixels()?.addr!!
        val surface = Surface.makeRasterDirect(
            imageInfo = storage.imageInfo,
            pixelsPtr = pixelsPointer,
            rowBytes = storage.rowBytes
        ).autoClose()

        surface.canvas.clear(Color.TRANSPARENT)
        renderDelegate.onRender(surface.canvas, width, height, nanoTime)

        flush(g, surface)
    }

    private fun flush(g: Graphics2D, surface: Surface) = autoCloseScope() {
        getSwingDrawer().draw(g, surface)
    }
}