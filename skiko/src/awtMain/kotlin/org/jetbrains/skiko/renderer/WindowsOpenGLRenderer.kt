package org.jetbrains.skiko.renderer

import kotlinx.coroutines.*
import org.jetbrains.skia.*
import org.jetbrains.skiko.*

internal class WindowsOpenGLRenderer(
    layer: SkiaLayer,
    analytics: SkiaLayerAnalytics,
    internal val properties: SkiaLayerProperties
) : AbstractOpenGLRenderer(layer, analytics) {
    init {
        loadOpenGLLibrary()
    }

    private val device: Long = layer.backedLayer.useDrawingSurfacePlatformInfo {
        getDevice(it).also { devicePtr ->
            check(devicePtr != 0L) { "Can't get device" }
        }
    }

    private val context = createContext(device, layer.contentHandle, layer.transparency).also {
        if (it == 0L) {
            throw RenderException("Cannot create Windows GL context")
        }
        makeCurrent(device, it)
        adapterName.also { adapterName ->
            if (adapterName != null && !isVideoCardSupported(GraphicsApi.OPENGL, hostOs, adapterName)) {
                throw RenderException("Cannot create Windows GL context")
            }
        }
        onDeviceChosen(adapterName)
    }

    init {
        makeCurrent()
        // For vsync we will use dwmFlush instead of swapInterval,
        // because it isn't reliable with DWM (Desktop Windows Manager): interval between frames isn't stable (14-19ms).
        // With dwmFlush it is stable (16.6-16.8 ms)
        // GLFW also uses dwmFlush (https://www.glfw.org/docs/3.0/window.html#window_swap)
        setSwapInterval(0)
        onContextInit()
    }

    override fun releaseResources() {
        makeCurrent()
        disposeGlResources()
        deleteContext(context)
    }

    override suspend fun LayerDrawScope.renderFrame(immediate: Boolean) {
        drawFrame()
        swapBuffers()
        OpenGLApi.instance.glFinish()
        if (SkikoProperties.windowsWaitForVsyncOnRedrawImmediately) {
            dwmFlush()
        }
    }

    override fun makeCurrent() = makeCurrent(device, context)
    internal fun swapBuffers() = swapBuffers(device)
}

/**
 * Draw all, swap all, then one dwmFlush for vsync.
 */
internal object WindowsGLFrameBatch : GLFrameBatch<WindowsOpenGLRenderer>() {
    override suspend fun drawAndPresent() {
        drawAll()

        for ((_, renderer) in toRedrawVisible) {
            renderer.swapBuffers()
        }

        for ((_, renderer) in toRedrawVisible) {
            renderer.makeCurrent()
            OpenGLApi.instance.glFinish()
        }

        val isVsyncEnabled = toRedrawVisible.all { (_, renderer) -> renderer.properties.isVsyncEnabled }
        if (isVsyncEnabled) {
            withContext(dispatcherToBlockOn) {
                dwmFlush() // wait for vsync
            }
        }
    }
}

private external fun makeCurrent(device: Long, context: Long)
private external fun getDevice(platformInfo: Long): Long
private external fun createContext(device: Long, contentHandle:Long, transparency: Boolean): Long
private external fun deleteContext(context: Long)
private external fun setSwapInterval(interval: Int)
private external fun swapBuffers(device: Long)

// TODO according to https://bugs.chromium.org/p/chromium/issues/detail?id=467617 dwmFlush has lag 3 ms after vsync.
//  Maybe we should use D3DKMTWaitForVerticalBlankEvent? See also https://www.vsynctester.com/chromeisbroken.html
// TODO should we support Windows 7? DWM can be disabled on Windows 7.
//  it that case there will be a crash or just no frame limit (I don't know exactly).
private external fun dwmFlush()