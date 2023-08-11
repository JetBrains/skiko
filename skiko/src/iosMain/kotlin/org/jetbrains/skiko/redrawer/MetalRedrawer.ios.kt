package org.jetbrains.skiko.redrawer

import kotlinx.cinterop.*
import org.jetbrains.skia.*
import org.jetbrains.skiko.Logger
import platform.Foundation.NSRunLoop
import platform.Foundation.NSSelectorFromString
import platform.QuartzCore.*
import platform.darwin.*
import kotlin.math.roundToInt
import kotlin.native.ref.WeakReference

internal class MetalRedrawer(
    private val metalLayer: CAMetalLayer,
    private val drawCallback: (Surface) -> Unit,

    // Used for tests, access to NSRunLoop crashes in test environment
    addDisplayLinkToRunLoop: ((CADisplayLink) -> Unit)? = null,
    private val disposeCallback: (MetalRedrawer) -> Unit = { }
) {
    // Workaround for KN compiler bug
    // Type mismatch: inferred type is objcnames.protocols.MTLDeviceProtocol but platform.Metal.MTLDeviceProtocol was expected
    @Suppress("USELESS_CAST")
    private val device = metalLayer.device as platform.Metal.MTLDeviceProtocol?
        ?: throw IllegalStateException("CAMetalLayer.device can not be null")
    private val queue = device.newCommandQueue() ?: throw IllegalStateException("Couldn't create Metal command queue")
    private val context = DirectContext.makeMetal(device.objcPtr(), queue.objcPtr())

    // Semaphore for preventing command buffers count more than swapchain size to be scheduled/executed at the same time
    private val inflightSemaphore = dispatch_semaphore_create(metalLayer.maximumDrawableCount.toLong())

    internal var maximumFramesPerSecond: NSInteger
        get() = caDisplayLink?.preferredFramesPerSecond ?: 0
        set(value) {
            caDisplayLink?.preferredFramesPerSecond = value
        }

    /*
     * Indicates that scene is invalidated and next display link callback will draw
     */
    private var hasScheduledDrawOnNextVSync = false

    /**
     * Needs scheduling displayLink for forcing UITouch events to come at the fastest possible cadence.
     * Otherwise, touch events can come at rate lower than actual display refresh rate.
     */
    var needsProactiveDisplayLink = false
        set(value) {
            field = value

            if (value) {
                caDisplayLink?.setPaused(false)
            }
        }

    internal var caDisplayLink: CADisplayLink? = CADisplayLink.displayLinkWithTarget(
        target = DisplayLinkProxy {
            this.handleDisplayLinkTick()
        },
        selector = NSSelectorFromString(DisplayLinkProxy::handleDisplayLinkTick.name)
    )

    init {
        val caDisplayLink = caDisplayLink ?: throw IllegalStateException("caDisplayLink is null during redrawer init")
        caDisplayLink.setPaused(true)
        if (addDisplayLinkToRunLoop == null) {
            caDisplayLink.addToRunLoop(NSRunLoop.mainRunLoop, NSRunLoop.mainRunLoop.currentMode)
        } else {
            addDisplayLinkToRunLoop.invoke(caDisplayLink)
        }
    }

    internal fun dispose() {
        check(caDisplayLink != null, { "MetalRedrawer.dispose() was called more than once" } )

        disposeCallback(this)
        caDisplayLink?.invalidate()
        caDisplayLink = null

        context.flush()
        context.close()
    }

    internal fun needRedraw() {
        hasScheduledDrawOnNextVSync = true

        // If caDisplayLink is proactive (touches tracking), this does nothing (already unpaused)
        caDisplayLink?.setPaused(false)
    }

    private fun handleDisplayLinkTick() {
        if (hasScheduledDrawOnNextVSync) {
            hasScheduledDrawOnNextVSync = false

            draw()
        }

        if (!needsProactiveDisplayLink) {
            caDisplayLink?.setPaused(true)
        }
    }

    private fun draw() {
        if (caDisplayLink == null) {
            return
        }

        autoreleasepool {
            val (width, height) = metalLayer.drawableSize.useContents {
                width.roundToInt() to height.roundToInt()
            }

            if (width <= 0 || height <= 0) {
                return@autoreleasepool
            }

            dispatch_semaphore_wait(inflightSemaphore, DISPATCH_TIME_FOREVER)

            val metalDrawable = metalLayer.nextDrawable()

            if (metalDrawable == null) {
                Logger.warn { "'metalLayer.nextDrawable()' returned null. 'metalLayer.allowsNextDrawableTimeout' should be set to false. Skipping the frame." }
                dispatch_semaphore_signal(inflightSemaphore)
                return@autoreleasepool
            }

            val renderTarget = BackendRenderTarget.makeMetal(width, height, metalDrawable.texture.objcPtr())

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
                // TODO: manually release metalDrawable when K/N API arrives
                dispatch_semaphore_signal(inflightSemaphore)
                return@autoreleasepool
            }

            surface.canvas.clear(Color.WHITE)
            drawCallback(surface)
            surface.flushAndSubmit()

            val commandBuffer = queue.commandBuffer()!!
            commandBuffer.label = "Present"
            commandBuffer.presentDrawable(metalDrawable)
            commandBuffer.addCompletedHandler {
                // Signal work finish, allow a new command buffer to be scheduled
                dispatch_semaphore_signal(inflightSemaphore)
            }
            commandBuffer.commit()

            surface.close()
            renderTarget.close()
            // TODO manually release metalDrawable when K/N API arrives
        }
    }
}

private class DisplayLinkProxy(
    private val callback: () -> Unit
) : NSObject() {
    @ObjCAction
    fun handleDisplayLinkTick() {
        callback()
    }
}
