package org.jetbrains.skiko.renderer

import kotlinx.coroutines.*
import org.jetbrains.skiko.*

internal class WindowsOpenGLRenderer(
    layer: SkiaLayer,
    analytics: SkiaLayerAnalytics,
    private val properties: SkiaLayerProperties
) : AbstractOpenGLRenderer(layer, analytics) {
    init {
        loadOpenGLLibrary()
    }

    private val device: Long = layer.backedLayer.useDrawingSurfacePlatformInfo {
        getDevice(it).also { devicePtr ->
            check(devicePtr != 0L) { "Can't get device" }
        }
    }

    private val glContext = createContext(device, layer.contentHandle, layer.transparency).also {
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
        check(!isDisposed) { "WindowsOpenGLRenderer is disposed" }
        makeCurrent()
        super.dispose()
        deleteContext(glContext)
    }

    override fun needRender(throttledToVsync: Boolean) {
        check(!isDisposed) { "WindowsOpenGLRenderer is disposed" }
        toRedraw.add(this)
        frameDispatcher.scheduleFrame()
    }

    override fun renderImmediately() {
        check(!isDisposed) { "WindowsOpenGLRenderer is disposed" }
        update()
        inDrawScope {
            if (!isDisposed) { // Renderer may be disposed in user code, during `update`
                makeCurrent()
                draw()
                swapBuffers()
                OpenGLApi.instance.glFinish()
                if (SkikoProperties.windowsWaitForVsyncOnRedrawImmediately) {
                    dwmFlush()
                }
            }
        }
    }

    private fun drawFrame() {
        inDrawScope { draw() }
    }

    private fun makeCurrent() = makeCurrent(device, glContext)
    private fun swapBuffers() = swapBuffers(device)

    companion object {
        private val toRedraw = mutableSetOf<WindowsOpenGLRenderer>()
        private val toRedrawCopy = mutableSetOf<WindowsOpenGLRenderer>()
        private val toRedrawVisible = toRedrawCopy
            .asSequence()
            .filterNot(WindowsOpenGLRenderer::isDisposed)
            .filter { it.layer.isShowing }

        private val frameDispatcher = FrameDispatcher(MainUIDispatcher) {
            toRedrawCopy.addAll(toRedraw)
            toRedraw.clear()

            val nanoTime = System.nanoTime()
            for (renderer in toRedrawVisible) {
                try {
                    renderer.update(nanoTime)
                } catch (e: CancellationException) {
                    // continue
                }
            }

            for (renderer in toRedrawVisible) {
                renderer.makeCurrent()
                renderer.drawFrame()
            }

            for (renderer in toRedrawVisible) {
                renderer.swapBuffers()
            }

            for (renderer in toRedrawVisible) {
                renderer.makeCurrent()
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
