package org.jetbrains.skiko.redrawer

import org.jetbrains.skia.*
import org.jetbrains.skiko.Logger
import org.jetbrains.skiko.internal.fastForEach
import org.robovm.apple.coreanimation.CADisplayLink
import org.robovm.apple.coreanimation.CAMetalLayer
import org.robovm.apple.foundation.*
import org.robovm.apple.metal.MTLCommandBuffer
import org.robovm.apple.uikit.UIApplication
import org.robovm.apple.uikit.UIApplicationState
import org.robovm.objc.annotation.NativeClass
import org.robovm.objc.annotation.Property
import org.robovm.rt.bro.annotation.Pointer
import java.util.concurrent.Semaphore
import kotlin.math.roundToInt

private class DisplayLinkConditions(
    val setPausedCallback: (Boolean) -> Unit
) {
    /**
     * see [MetalRedrawer.needsProactiveDisplayLink]
     */
    var needsToBeProactive: Boolean = false
        set(value) {
            field = value

            update()
        }

    /**
     * Indicates that scene is invalidated and next display link callback will draw
     */
    var needsRedrawOnNextVsync: Boolean = false
        set(value) {
            field = value

            update()
        }

    /**
     * Indicates that application is running foreground now
     */
    var isApplicationActive: Boolean = false
        set(value) {
            field = value

            update()
        }

    private fun update() {
        val isUnpaused = isApplicationActive && (needsToBeProactive || needsRedrawOnNextVsync)
        setPausedCallback(!isUnpaused)
    }
}

private class ApplicationStateListener(
    /**
     * Callback which will be called with `true` when the app becomes active, and `false` when the app goes background
     */
    private val callback: (Boolean) -> Unit
) {
    private val notificationCenter = NSNotificationCenter.getDefaultCenter()

    private val foregroundObserver: NSObject = notificationCenter.addObserver(
        UIApplication.WillEnterForegroundNotification(),
        null,
        NSOperationQueue.getMainQueue()
    ) { callback(true) }

    private val backgroundObserver: NSObject = notificationCenter.addObserver(
        UIApplication.DidEnterBackgroundNotification(),
        null,
        NSOperationQueue.getMainQueue()
    ) { callback(false) }

    /**
     * Deregister from [NSNotificationCenter]
     */
    fun dispose() {
        notificationCenter.removeObserver(foregroundObserver)
        notificationCenter.removeObserver(backgroundObserver)
    }
}

internal class MetalRedrawer(
    private val metalLayer: CAMetalLayer,
    private val drawCallback: (Surface) -> Unit,

    // Used for tests, access to NSRunLoop crashes in test environment
    addDisplayLinkToRunLoop: ((CADisplayLink) -> Unit)? = null,
    private val disposeCallback: (MetalRedrawer) -> Unit = { }
) {
    private val device = metalLayer.device
        ?: throw IllegalStateException("CAMetalLayer.device can not be null")
    private val queue = device.newCommandQueue() ?: throw IllegalStateException("Couldn't create Metal command queue")
    private val context = DirectContext.makeMetal(device.handle, queue.handle)
    private val inflightCommandBuffers = mutableListOf<MTLCommandBuffer>()

    // Semaphore for preventing command buffers count more than swapchain size to be scheduled/executed at the same time
    private val inflightSemaphore = Semaphore(metalLayer.maximumDrawableCount.toInt())

    internal var maximumFramesPerSecond: Long
        get() = caDisplayLink?.preferredFramesPerSecond ?: 0
        set(value) {
            caDisplayLink?.preferredFramesPerSecond = value
        }

    /**
     * Needs scheduling displayLink for forcing UITouch events to come at the fastest possible cadence.
     * Otherwise, touch events can come at rate lower than actual display refresh rate.
     */
    var needsProactiveDisplayLink: Boolean
        get() = displayLinkConditions.needsToBeProactive
        set(value) {
            displayLinkConditions.needsToBeProactive = value
        }

    internal var caDisplayLink: CADisplayLink? = CADisplayLink(
        CADisplayLink.OnUpdateListener { handleDisplayLinkTick() }
    )

    private val displayLinkConditions = DisplayLinkConditions { paused ->
        caDisplayLink?.isPaused = paused
    }

    private val applicationStateListener = ApplicationStateListener { isApplicationActive ->
        displayLinkConditions.isApplicationActive = isApplicationActive
        if (!isApplicationActive) {
            // If application goes background, synchronously schedule all inflightCommandBuffers, as per
            // https://developer.apple.com/documentation/metal/gpu_devices_and_work_submission/preparing_your_metal_app_to_run_in_the_background?language=objc
            inflightCommandBuffers.fastForEach { commandBuffer ->
                // Will immediately return for MTLCommandBuffer's which are not in `Commited` status
                commandBuffer.waitUntilCompleted()
            }
        }
    }

    init {
        val caDisplayLink = caDisplayLink ?: throw IllegalStateException("caDisplayLink is null during redrawer init")

        // UIApplication can be in UIApplicationStateInactive state (during app launch before it gives control back to run loop)
        // and won't receive UIApplicationWillEnterForegroundNotification
        // so we compare the state with UIApplicationStateBackground instead of UIApplicationStateActive
        displayLinkConditions.isApplicationActive = UIApplication.getSharedApplication().applicationState != UIApplicationState.Background

        if (addDisplayLinkToRunLoop == null) {
            caDisplayLink.addToRunLoop(NSRunLoop.getMain(), NSRunLoop.getMain().currentMode)
        } else {
            addDisplayLinkToRunLoop.invoke(caDisplayLink)
        }
    }

    internal fun dispose() {
        check(caDisplayLink != null) { "MetalRedrawer.dispose() was called more than once" }

        disposeCallback(this)

        applicationStateListener.dispose()

        caDisplayLink?.invalidate()
        caDisplayLink = null

        context.flush()
        context.close()
    }

    internal fun needRender() {
        displayLinkConditions.needsRedrawOnNextVsync = true
    }

    private fun handleDisplayLinkTick() {
        if (displayLinkConditions.needsRedrawOnNextVsync) {
            displayLinkConditions.needsRedrawOnNextVsync = false

            draw()
        }
    }

    private fun draw() {
        if (caDisplayLink == null) {
            Logger.warn { "caDisplayLink callback called after it was invalidated " }
            return
        }

        NSAutoreleasePool().use {
            val width = metalLayer.drawableSize.width.roundToInt()
            val height = metalLayer.drawableSize.height.roundToInt()

            if (width <= 0 || height <= 0) {
                return@use
            }

            inflightSemaphore.acquireUninterruptibly()

            val metalDrawable = metalLayer.nextDrawable()

            if (metalDrawable == null) {
                Logger.warn { "'metalLayer.nextDrawable()' returned null. 'metalLayer.allowsNextDrawableTimeout' should be set to false. Skipping the frame." }
                inflightSemaphore.release()
                return@use
            }

            // FIXME: this is dirty workaround to get texture raw pointer
            // as metalDrawable.texture seems to be not returning proper ObjC protocol/Class at lease at Sim
            // that cause ClassCastException (to be investigated)
            @NativeClass
            class WORKAROUND(handle: Long): NSObject(handle) {
                init { retain() }
                @Property(selector = "texture")
                @Pointer
                external fun getTexture(): Long
            }
            val metalDrawableShadow = WORKAROUND((metalDrawable as NSObject).handle)
            val renderTarget = BackendRenderTarget.makeMetal(width, height, metalDrawableShadow.getTexture()) //metalDrawable.texture.handle)

            val surface = Surface.makeFromBackendRenderTarget(
                context,
                renderTarget,
                SurfaceOrigin.TOP_LEFT,
                SurfaceColorFormat.BGRA_8888,
                ColorSpace.sRGB,
                SurfaceProps(pixelGeometry = PixelGeometry.UNKNOWN)
            )

            if (surface == null) {
                Logger.warn { "'Surface.makeFromBackendRenderTarget' returned null. Skipping the frame." }
                renderTarget.close()
                inflightSemaphore.release()
                return@use
            }

            surface.canvas.clear(Color.WHITE)
            drawCallback(surface)
            surface.flushAndSubmit()

            val commandBuffer = queue.commandBuffer!!
            commandBuffer.label = "Present"
            commandBuffer.presentDrawable(metalDrawable)
            commandBuffer.addCompletedHandler {
                // Signal work finish, allow a new command buffer to be scheduled
                inflightSemaphore.release()
            }
            commandBuffer.commit()

            surface.close()
            renderTarget.close()

            // Track current inflight command buffers to synchronously wait for their schedule in case app goes background
            if (inflightCommandBuffers.size == metalLayer.maximumDrawableCount.toInt()) {
                inflightCommandBuffers.removeAt(0)
            }

            inflightCommandBuffers.add(commandBuffer)
        }
    }
}
