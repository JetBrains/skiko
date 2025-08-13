package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.withContext
import org.jetbrains.skia.BackendRenderTarget
import org.jetbrains.skia.DirectContext
import org.jetbrains.skiko.*
import org.jetbrains.skiko.context.AngleContextHandler

internal class AngleRedrawer(
    private val layer: SkiaLayer,
    analytics: SkiaLayerAnalytics,
    private val properties: SkiaLayerProperties
) : AWTRedrawer(layer, analytics, GraphicsApi.ANGLE) {
    init {
        try {
            loadAngleLibrary()
        } catch (e: Exception) {
            throw RenderException("Failed to load ANGLE library", cause = e)
        }
    }

    private val contextHandler = AngleContextHandler(layer)
    override val renderInfo: String get() = contextHandler.rendererInfo()

    private var drawLock = Any()

    private var device: Long = 0L
        get() {
            if (field == 0L) {
                throw RenderException("ANGLE device is not initialized or already disposed")
            }
            return field
        }

    private val frameDispatcher = FrameDispatcher(MainUIDispatcher) {
        if (layer.isShowing) {
            update(System.nanoTime())
            draw()
        }
    }

    private val adapterName get() = AngleApi.glGetString(AngleApi.GL_RENDERER)

    init {
        device = layer.backedLayer.useDrawingSurfacePlatformInfo { platformInfo ->
            createAngleDevice(platformInfo, layer.transparency).takeIf { it != 0L }
                ?: throw RenderException("Failed to create ANGLE device.")
        }
        adapterName.let { adapterName ->
            if (adapterName != null && !isVideoCardSupported(GraphicsApi.ANGLE, hostOs, adapterName)) {
                throw RenderException("Cannot create ANGLE redrawer.")
            }
            onDeviceChosen(adapterName)
        }
        onContextInit()
    }

    override fun dispose() = synchronized(drawLock) {
        frameDispatcher.cancel()
        makeCurrent(device)
        contextHandler.dispose()
        disposeDevice(device)
        device = 0L
        super.dispose()
    }

    override fun needRedraw() {
        check(!isDisposed) { "ANGLE redrawer is disposed" }
        frameDispatcher.scheduleFrame()
    }

    override fun redrawImmediately() {
        check(!isDisposed) { "ANGLE redrawer is disposed" }
        inDrawScope {
            update(System.nanoTime())
            drawAndSwap(withVsync = SkikoProperties.windowsWaitForVsyncOnRedrawImmediately)
        }
    }

    private fun draw() {
        inDrawScope {
            drawAndSwap(withVsync = properties.isVsyncEnabled)
        }
    }

    private fun drawAndSwap(withVsync: Boolean) = synchronized(drawLock) {
        if (isDisposed) {
            return
        }
        makeCurrent(device)
        contextHandler.draw()
        swapBuffers(device, withVsync)
    }

    fun makeContext() = DirectContext(
        makeAngleContext(device).takeIf { it != 0L }
            ?: throw RenderException("Failed to make GL context.")
    )

    fun makeRenderTarget(width: Int, height: Int) = BackendRenderTarget(
        makeAngleRenderTarget(device, width, height).takeIf { it != 0L }
            ?: throw RenderException("Failed to make ANGLE render target.")
    )
}

private external fun createAngleDevice(platformInfo: Long, transparency: Boolean): Long
private external fun makeCurrent(device: Long)
private external fun makeAngleContext(device: Long): Long
private external fun makeAngleRenderTarget(device: Long, width: Int, height: Int): Long
private external fun swapBuffers(device: Long, waitForVsync: Boolean)
private external fun disposeDevice(device: Long)
