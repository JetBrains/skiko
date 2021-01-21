package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing
import org.jetbrains.skiko.FrameDispatcher
import org.jetbrains.skiko.HardwareLayer
import org.jetbrains.skiko.OpenGLApi
import javax.swing.SwingUtilities.convertPoint
import javax.swing.SwingUtilities.getRootPane

internal class MacOsRedrawer(
    private val layer: HardwareLayer
) : Redrawer {
    private val containerLayerPtr = initContainer(layer)
    private val drawLock = Any()

    private val drawLayer = object : AWTGLLayer(containerLayerPtr, setNeedsDisplayOnBoundsChange = true) {
        override fun draw() = synchronized(drawLock) {
            layer.draw()
        }
    }

    // use a separate layer for vsync, because with single layer we cannot asynchronously update layer
    // `update` is suspend, and runBlocking(Dispatchers.Swing) causes dead lock with AppKit Thread.
    // AWT has a method to avoid dead locks but it is internal (sun.lwawt.macosx.LWCToolkit.invokeAndWait)
    private val vsyncLayer = object : AWTGLLayer(containerLayerPtr, setNeedsDisplayOnBoundsChange = false) {
        @Volatile
        private var needDraw: CompletableDeferred<Unit>? = null

        init {
            setFrame(0, 0, 1, 1) // if frame has zero size then it will be not drawn at all
        }

        override fun draw() {
            // Clear layer with transparent color, so it will be not pink color.
            val opengl = OpenGLApi.instance
            opengl.glClearColor(0f, 0f, 0f, 0f)
            opengl.glClear(opengl.GL_COLOR_BUFFER_BIT)
            needDraw?.complete(Unit)
        }

        override fun canDraw(): Boolean {
            val canDraw = needDraw != null
            if (!canDraw) {
                isAsynchronous = false // stop asynchronous mode so we don't waste CPU cycles
            }
            return canDraw
        }

        suspend fun sync() {
            check(needDraw == null)
            needDraw = CompletableDeferred()

            // Use asynchronous mode instead of just setNeedsDisplay,
            // so Core Animation will wait for the next frame in vsync signal
            //
            // Asynchronous mode means that Core Animation will automatically
            // call canDraw/draw every vsync signal (~16.7ms on 60Hz monitor)
            //
            // Similar is implemented in Chromium:
            // https://chromium.googlesource.com/chromium/chromium/+/0489078bf98350b00876070cf2fdce230905f47e/content/browser/renderer_host/compositing_iosurface_layer_mac.mm#57
            if (!isAsynchronous) {
                isAsynchronous = true
                super.setNeedsDisplay()
            }

            needDraw!!.await()
            needDraw = null
        }
    }

    private val frameDispatcher = FrameDispatcher(Dispatchers.Swing) {
        layer.update(System.nanoTime())
        drawLayer.setNeedsDisplay()
        vsyncLayer.sync()
    }

    override fun dispose() = synchronized(drawLock) {
        frameDispatcher.cancel()
        vsyncLayer.dispose()
        drawLayer.dispose()
    }

    override fun syncSize() {
        val globalPosition = convertPoint(layer, layer.x, layer.y, getRootPane(layer))
        setContentScale(containerLayerPtr, layer.contentScale)
        setContentScale(drawLayer.ptr, layer.contentScale)
        drawLayer.setFrame(
            globalPosition.x,
            globalPosition.y,
            layer.width.coerceAtLeast(0),
            layer.height.coerceAtLeast(0)
        )
    }

    override fun needRedraw() {
        frameDispatcher.scheduleFrame()
    }
}

private open class AWTGLLayer(private val containerPtr: Long, setNeedsDisplayOnBoundsChange: Boolean) {
    @Suppress("LeakingThis")
    val ptr = initAWTGLLayer(containerPtr, this, setNeedsDisplayOnBoundsChange)

    fun setFrame(x: Int, y: Int, width: Int, height: Int) {
        setFrame(containerPtr, ptr, x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat())
    }

    // Called in AWT Thread
    open fun dispose() = disposeAWTGLLayer(ptr)

    var isAsynchronous: Boolean
        get() = isAsynchronous(ptr)
        set(value) = setAsynchronous(ptr, value)

    open fun setNeedsDisplay() = setNeedsDisplayOnMainThread(ptr)

    // Called in AppKit Thread
    protected open fun canDraw() = true

    // Called in AppKit Thread
    protected open fun draw() = Unit

    private external fun isAsynchronous(ptr: Long): Boolean
    private external fun setAsynchronous(ptr: Long, isAsynchronous: Boolean)
    private external fun setNeedsDisplayOnMainThread(nativePtr: Long)
    protected external fun setFrame(containerPtr: Long, ptr: Long, x: Float, y: Float, width: Float, height: Float)
}

private external fun initContainer(layer: HardwareLayer): Long
private external fun setContentScale(layerNativePtr: Long, contentScale: Float)
private external fun initAWTGLLayer(containerPtr: Long, layer: AWTGLLayer, setNeedsDisplayOnBoundsChange: Boolean): Long
private external fun disposeAWTGLLayer(ptr: Long)
