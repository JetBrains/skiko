package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.*
import org.jetbrains.skia.*
import org.jetbrains.skiko.*

/**
 * Every GL call stays on the EDT, so this backend needs no `drawLock`.
 */
internal class WindowsOpenGLRedrawer(
    host: AwtSurfaceHost,
    analytics: SkiaLayerAnalytics,
    private val properties: SkiaLayerProperties
) : AbstractOpenGLRedrawer(host, analytics, properties) {
    init {
        loadOpenGLLibrary()
    }

    private val device: Long = host.backedLayer.useDrawingSurfacePlatformInfo {
        getDevice(it).also { devicePtr ->
            check(devicePtr != 0L) { "Can't get device" }
        }
    }

    private val context = createContext(device, host.contentHandle, host.transparency).also {
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

    override suspend fun renderFrame(scope: LayerDrawScope, immediate: Boolean) {
        makeCurrent()
        with(scope) { drawFrame() }
        swapBuffers()
        OpenGLApi.instance.glFinish()
        if (immediate && SkikoProperties.windowsWaitForVsyncOnRedrawImmediately) {
            // The looped path waits for vsync off the EDT in paceAfterFrame; the immediate path waits inline.
            dwmFlush()
        }
    }

    override suspend fun runFrame(frame: suspend () -> Unit) {
        frame()
        if (properties.isVsyncEnabled) {
            withContext(dispatcherToBlockOn) {
                dwmFlush() // wait for vsync
            }
        }
    }

    override fun acquireSurface(width: Int, height: Int): Surface {
        check(!isDisposed) { "WindowsOpenGLRedrawer is disposed" }
        makeCurrent()
        if (!ensureContext()) {
            throw RenderException("Cannot init graphic context")
        }
        createSurface(width, height, host.pixelGeometry)
        return glSurface ?: throw RenderException("Cannot create surface for ${width}x$height")
    }

    override fun present() {
        if (isDisposed) return
        makeCurrent()
        flushGl()
        swapBuffers()
        OpenGLApi.instance.glFinish()
    }

    private fun makeCurrent() = makeCurrent(device, context)
    private fun swapBuffers() = swapBuffers(device)
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