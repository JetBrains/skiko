package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.jetbrains.skia.BackendRenderTarget
import org.jetbrains.skia.DirectContext
import org.jetbrains.skiko.*
import org.jetbrains.skiko.context.MetalContextHandler
import javax.swing.SwingUtilities.*

internal class MetalRedrawer(
    private val layer: SkiaLayer,
    analytics: SkiaLayerAnalytics,
    private val properties: SkiaLayerProperties
) : AWTRedrawer(layer, analytics, GraphicsApi.METAL) {
    private val contextHandler = MetalContextHandler(layer)
    override val renderInfo: String get() = contextHandler.rendererInfo()

    companion object {
        init {
            Library.load()
        }
    }
    private var drawLock = Any()

    private var device: Long
        get() {
            check(field != 0L) { "Device is not initialized" }
            return field
        }
    val adapterName: String
    val adapterMemorySize: Long

    init {
        val adapter = chooseAdapter(properties.adapterPriority.ordinal)
        adapterName = getAdapterName(adapter)
        adapterMemorySize = getAdapterMemorySize(adapter)
        onDeviceChosen(adapterName)
        device = layer.backedLayer.useDrawingSurfacePlatformInfo {
            createMetalDevice(layer.windowHandle, layer.transparency, adapter, it)
        }
    }

    private val windowHandle = layer.windowHandle

    init {
        setVSyncEnabled(device, properties.isVsyncEnabled)
    }

    private val frameDispatcher = FrameDispatcher(MainUIDispatcher) {
        if (layer.isShowing) {
            update(System.nanoTime())
            draw()
        }
    }

    init {
        onContextInit()
    }

    override fun dispose() = synchronized(drawLock) {
        frameDispatcher.cancel()
        contextHandler.dispose()
        disposeDevice(device)
        device = 0
        super.dispose()
    }

    override fun needRedraw() {
        check(!isDisposed) { "MetalRedrawer is disposed" }
        frameDispatcher.scheduleFrame()
    }

    override fun redrawImmediately() {
        check(!isDisposed) { "MetalRedrawer is disposed" }
        inDrawScope {
            setVSyncEnabled(device, enabled = false)
            update(System.nanoTime())
            if (!isDisposed) { // Redrawer may be disposed in user code, during `update`
                performDraw()
                setVSyncEnabled(device, properties.isVsyncEnabled)
            }
        }
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
        inDrawScope {
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

    override fun setVisible(isVisible: Boolean) {
        setLayerVisible(device, isVisible)
    }

    fun makeContext() = DirectContext(
        makeMetalContext(device)
    )

    fun makeRenderTarget(width: Int, height: Int) = BackendRenderTarget(
        makeMetalRenderTarget(device, width, height)
    )

    fun finishFrame() = finishFrame(device)

    private external fun chooseAdapter(adapterPriority: Int): Long
    private external fun createMetalDevice(window:Long, transparency: Boolean, adapter: Long, platformInfo: Long): Long
    private external fun makeMetalContext(device: Long): Long
    private external fun makeMetalRenderTarget(device: Long, width: Int, height: Int): Long
    private external fun disposeDevice(device: Long)
    private external fun finishFrame(device: Long)
    private external fun resizeLayers(device: Long, x: Int, y: Int, width: Int, height: Int)
    private external fun setLayerVisible(device: Long, isVisible: Boolean)
    private external fun setContentScale(device: Long, contentScale: Float)
    private external fun setVSyncEnabled(device: Long, enabled: Boolean)
    private external fun isOccluded(window: Long): Boolean
    private external fun getAdapterName(adapter: Long): String
    private external fun getAdapterMemorySize(adapter: Long): Long
    private external fun startRendering(): Long
    private external fun endRendering(handle: Long)
}
