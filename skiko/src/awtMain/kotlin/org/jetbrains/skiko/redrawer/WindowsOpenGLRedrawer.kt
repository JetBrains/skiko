package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.*
import org.jetbrains.skiko.*
import org.jetbrains.skiko.context.OpenGLContextHandler

internal class WindowsOpenGLRedrawer(
    private val layer: SkiaLayer,
    analytics: SkiaLayerAnalytics,
    private val properties: SkiaLayerProperties
) : AWTRedrawer(layer, analytics, GraphicsApi.OPENGL) {
    init {
        loadOpenGLLibrary()
    }

    private val contextHandler = OpenGLContextHandler(layer)
    override val renderInfo: String get() = contextHandler.rendererInfo()

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

    private val adapterName get() = OpenGLApi.instance.glGetString(OpenGLApi.instance.GL_RENDERER)

    init {
        makeCurrent()
        // For vsync we will use dwmFlush instead of swapInterval,
        // because it isn't reliable with DWM (Desktop Windows Manager): interval between frames isn't stable (14-19ms).
        // With dwmFlush it is stable (16.6-16.8 ms)
        // GLFW also uses dwmFlush (https://www.glfw.org/docs/3.0/window.html#window_swap)
        setSwapInterval(0)
        onContextInit()
    }

    override fun dispose() {
        check(!isDisposed) { "WindowsOpenGLRedrawer is disposed" }
        makeCurrent()
        contextHandler.dispose()
        deleteContext(context)
        super.dispose()
    }

    override fun needRedraw(throttledToVsync: Boolean) {
        check(!isDisposed) { "WindowsOpenGLRedrawer is disposed" }
        toRedraw.add(this)
        frameDispatcher.scheduleFrame()
    }

    override fun redrawImmediately(updateNeeded: Boolean) {
        check(!isDisposed) { "WindowsOpenGLRedrawer is disposed" }
        if (updateNeeded) {
            update()
        }
        inDrawScope {
            if (!isDisposed) { // Redrawer may be disposed in user code, during `update`
                makeCurrent()
                contextHandler.draw()
                swapBuffers()
                OpenGLApi.instance.glFinish()
                if (SkikoProperties.windowsWaitForVsyncOnRedrawImmediately) {
                    dwmFlush()
                }
            }
        }
    }

    private fun draw() {
        inDrawScope(contextHandler::draw)
    }

    private fun makeCurrent() = makeCurrent(device, context)
    private fun swapBuffers() = swapBuffers(device)

    companion object {
        private val toRedraw = mutableSetOf<WindowsOpenGLRedrawer>()
        private val toRedrawCopy = mutableSetOf<WindowsOpenGLRedrawer>()
        private val toRedrawVisible = toRedrawCopy
            .asSequence()
            .filterNot(WindowsOpenGLRedrawer::isDisposed)
            .filter { it.layer.isShowing }

        private val frameDispatcher = FrameDispatcher(MainUIDispatcher) {
            toRedrawCopy.addAll(toRedraw)
            toRedraw.clear()

            val nanoTime = System.nanoTime()
            for (redrawer in toRedrawVisible) {
                try {
                    redrawer.update(nanoTime)
                } catch (e: CancellationException) {
                    // continue
                }
            }

            for (redrawer in toRedrawVisible) {
                redrawer.makeCurrent()
                redrawer.draw()
            }

            for (redrawer in toRedrawVisible) {
                redrawer.swapBuffers()
            }

            for (redrawer in toRedrawVisible) {
                redrawer.makeCurrent()
                OpenGLApi.instance.glFinish()
            }

            val isVsyncEnabled = toRedrawVisible.all { it.properties.isVsyncEnabled }
            if (isVsyncEnabled) {
                withContext(dispatcherToBlockOn) {
                    dwmFlush() // wait for vsync
                }
            }

            // Without clearing we will have a memory leak
            toRedrawCopy.clear()
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