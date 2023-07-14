package org.jetbrains.skiko.swing

import org.jetbrains.skia.*
import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.impl.withNullableResult
import org.jetbrains.skiko.*
import org.jetbrains.skiko.Library
import java.awt.Graphics2D
import javax.swing.SwingUtilities
import kotlin.math.min

/**
 * Provides a way to draw on Skia canvas rendered off-screen with Metal GPU acceleration and then pass it to [java.awt.Graphics2D].
 * It provides better interoperability with Swing, but it is less efficient than on-screen rendering.
 *
 * For now, it uses drawing to [java.awt.image.BufferedImage] that cause VRAM <-> RAM memory transfer and so increased CPU usage.
 *
 * Content to draw is provided by [SkikoView].
 *
 * For on-screen rendering see [org.jetbrains.skiko.redrawer.MetalRedrawer].
 *
 * @see SwingRedrawerBase
 * @see SwingOffscreenDrawer
 */
internal class MetalSwingRedrawer(
    swingLayerProperties: SwingLayerProperties,
    private val skikoView: SkikoView,
    analytics: SkiaLayerAnalytics
) : SwingRedrawerBase(swingLayerProperties, analytics, GraphicsApi.METAL) {
    companion object {
        init {
            Library.load()
        }
    }

    private val adapter: MetalAdapter = chooseMetalAdapter(swingLayerProperties.adapterPriority).also {
        onDeviceChosen(it.name)
    }

    private val commandQueue: Long = createCommandQueue(adapter.ptr)
    private val context: DirectContext = makeMetalContext()

    private var texturePtr: Long = 0

    private var byteArray = ByteArray(0)

    init {
        onContextInit()
    }

    private val swingOffscreenDrawer = SwingOffscreenDrawer(swingLayerProperties)

    override fun dispose() {
        adapter.dispose()
        disposeMetalTexture(texturePtr)
        disposeCommandQueue(commandQueue)
        super.dispose()
    }

    override fun onRender(g: Graphics2D, width: Int, height: Int, nanoTime: Long) = autoCloseScope {
        texturePtr = makeMetalTexture(adapter.ptr, texturePtr, width, height)
        val renderTarget = makeRenderTarget().autoClose()
        val surface = Surface.makeFromBackendRenderTarget(
            context,
            renderTarget,
            SurfaceOrigin.TOP_LEFT,
            SurfaceColorFormat.BGRA_8888,
            ColorSpace.sRGB,
            SurfaceProps(pixelGeometry = PixelGeometry.UNKNOWN)
        )?.autoClose() ?: throw RenderException("Cannot create surface")

        val canvas = surface.canvas
        canvas.clear(Color.TRANSPARENT)
        SwingUtilities.invokeLater {
            redraw(g)
        }
//        skikoView.onRender(canvas, width, height, nanoTime)
//        flush(surface, g)
    }

    private fun flush(surface: Surface, g: Graphics2D) {
        surface.flushAndSubmit(syncCpu = true)

        val width = surface.width
        val height = surface.height

        val size = height * width * 4
        if (byteArray.size != size) {
            byteArray = ByteArray(size)
        }

        readPixelsFromTexture(texturePtr, byteArray)
        swingOffscreenDrawer.draw(g, byteArray, width, height)
    }

    override fun rendererInfo(): String {
        return super.rendererInfo() +
                "Video card: ${adapter.name}\n" +
                "Total VRAM: ${adapter.memorySize / 1024 / 1024} MB\n"
    }

    private fun makeRenderTarget() = BackendRenderTarget(
        makeMetalRenderTargetOffScreen(texturePtr)
    )

    private fun makeMetalContext(): DirectContext = DirectContext(
        makeMetalContext(adapter.ptr, commandQueue)
    )

    private external fun makeMetalContext(adapter: Long, commandQueue: Long): Long

    private external fun createCommandQueue(adapter: Long): Long
    private external fun disposeCommandQueue(commandQueue: Long)

    private external fun makeMetalRenderTargetOffScreen(texture: Long): Long

    private external fun makeMetalTexture(adapter: Long, oldTexture: Long, width: Int, height: Int): Long
    private external fun disposeMetalTexture(texture: Long): Long

    private fun readPixelsFromTexture(texture: Long, bytes: ByteArray) {
        try {
            withNullableResult(bytes) {
                readPixelsFromTexture(texture, it, commandQueue)
                true
            }
        } finally {
            reachabilityBarrier(bytes)
        }
    }

    private external fun readPixelsFromTexture(texture: Long, bytes: InteropPointer, commandQueue: Long)
}