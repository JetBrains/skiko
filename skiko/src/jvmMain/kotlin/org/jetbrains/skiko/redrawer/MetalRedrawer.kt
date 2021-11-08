package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext
import org.jetbrains.skia.BackendRenderTarget
import org.jetbrains.skia.DirectContext
import org.jetbrains.skia.impl.Library
import org.jetbrains.skiko.*
import org.jetbrains.skiko.context.MetalContextHandler
import org.jetbrains.skiko.context.OpenGLContextHandler
import javax.swing.SwingUtilities.*

internal class MetalRedrawer(
    private val layer: SkiaLayer,
    private val properties: SkiaLayerProperties
) : Redrawer {
    private val contextHandler = MetalContextHandler(layer)
    override val renderInfo: String get() = contextHandler.rendererInfo()

    companion object {
        init {
            Library.load()
        }
    }
    private var isDisposed = false
    private var drawLock = Any()
    private val device = layer.backedLayer.useDrawingSurfacePlatformInfo {
        createMetalDevice(layer.windowHandle, layer.transparency, getAdapterPriority(), it)
    }
    private val windowHandle = layer.windowHandle

    init {
        setVSyncEnabled(device, properties.isVsyncEnabled)
    }

    private val frameDispatcher = FrameDispatcher(Dispatchers.Swing) {
        if (layer.isShowing) {
            update(System.nanoTime())
            draw()
        }
    }

    override fun dispose() = synchronized(drawLock) {
        frameDispatcher.cancel()
        contextHandler.dispose()
        disposeDevice(device)
        isDisposed = true
    }

    override fun needRedraw() {
        check(!isDisposed) { "MetalRedrawer is disposed" }
        frameDispatcher.scheduleFrame()
    }

    override fun redrawImmediately() {
        check(!isDisposed) { "MetalRedrawer is disposed" }
        setVSyncEnabled(device, enabled = false)
        update(System.nanoTime())
        layer.inDrawScope(::performDraw)
        setVSyncEnabled(device, properties.isVsyncEnabled)
    }

    private fun update(nanoTime: Long) {
        layer.update(nanoTime)
    }

    private suspend fun draw() {
        // 2,3 GHz 8-Core Intel Core i9
        //
        // Test1. 8 windows, multiple clocks, 800x600
        //
        // Executors.newSingleThreadExecutor().asCoroutineDispatcher(): 20 FPS, 130% CPU
        // Dispatchers.IO: 58 FPS, 460% CPU
        //
        // Test2. 60 windows, single clock, 800x600
        //
        // Executors.newSingleThreadExecutor().asCoroutineDispatcher(): 50 FPS, 150% CPU
        // Dispatchers.IO: 50 FPS, 200% CPU
        layer.inDrawScope {
            withContext(Dispatchers.IO) {
                performDraw()
            }
        }
        if (isDisposed) throw CancellationException()

        // When window is not visible - it doesn't make sense to redraw fast to avoid battery drain.
        // In theory, we could be more precise, and just suspend rendering in
        // `NSWindowDidChangeOcclusionStateNotification`, but current approach seems to work as well in practise.
        if (isOccluded(windowHandle))
            delay(300)
    }

    private fun performDraw() = synchronized(drawLock) {
        if (!isDisposed) {
            val handle = startRendering()
            try {
                contextHandler.draw()
            } finally {
                endRendering(handle)
            }
        }
    }

    override fun syncSize() = synchronized(drawLock) {
        check(isEventDispatchThread()) { "Method should be called from AWT event dispatch thread" }
        val rootPane = getRootPane(layer)
        val globalPosition = convertPoint(layer, layer.x, layer.y, rootPane)
        setContentScale(device, layer.contentScale)
        resizeLayers(
            device,
            globalPosition.x,
            rootPane.height - globalPosition.y - layer.height,
            layer.width.coerceAtLeast(0),
            layer.height.coerceAtLeast(0)
        )
    }

    fun makeContext() = DirectContext(
        makeMetalContext(device)
    )

    fun makeRenderTarget(width: Int, height: Int) = BackendRenderTarget(
        makeMetalRenderTarget(device, width, height)
    )

    fun finishFrame() = finishFrame(device)

    fun getAdapterPriority(): Int {
        val adapterPriority = GpuPriority.parse(System.getProperty("skiko.metal.gpu.priority"))
        return when (adapterPriority) {
            GpuPriority.Auto -> 0
            GpuPriority.Integrated -> 1
            GpuPriority.Discrete -> 2
            else -> 0
        }
    }

    fun getAdapterName(): String = getAdapterName(device)
    fun getAdapterMemorySize(): Long = getAdapterMemorySize(device)

    private external fun createMetalDevice(window:Long, transparency: Boolean, adapterPriority: Int, platformInfo: Long): Long
    private external fun makeMetalContext(device: Long): Long
    private external fun makeMetalRenderTarget(device: Long, width: Int, height: Int): Long
    private external fun disposeDevice(device: Long)
    private external fun finishFrame(device: Long)
    private external fun resizeLayers(device: Long, x: Int, y: Int, width: Int, height: Int)
    private external fun setContentScale(device: Long, contentScale: Float)
    private external fun setVSyncEnabled(device: Long, enabled: Boolean)
    private external fun isOccluded(window: Long): Boolean
    private external fun getAdapterName(device: Long): String
    private external fun getAdapterMemorySize(device: Long): Long
    private external fun startRendering(): Long
    private external fun endRendering(handle: Long)
}
