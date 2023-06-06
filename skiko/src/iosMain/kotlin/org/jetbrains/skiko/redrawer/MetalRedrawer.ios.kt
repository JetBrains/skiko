package org.jetbrains.skiko.redrawer

import kotlinx.cinterop.*
import org.jetbrains.skia.BackendRenderTarget
import org.jetbrains.skia.DirectContext
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.context.MetalContextHandler
import platform.Foundation.NSRunLoop
import platform.Foundation.NSSelectorFromString
import platform.Metal.MTLCreateSystemDefaultDevice
import platform.QuartzCore.*
import platform.darwin.NSObject

private enum class DrawSchedulingState {
    AVAILABLE_ON_NEXT_FRAME,
    AVAILABLE_ON_CURRENT_FRAME,
    SCHEDULED_ON_NEXT_FRAME
}

internal class MetalRedrawer(
    private val layer: SkiaLayer,
    private val metalLayer: CAMetalLayer
) : Redrawer {
    private val contextHandler = MetalContextHandler(layer)
    override val renderInfo: String get() = contextHandler.rendererInfo()

    private var isDisposed = false
    internal val device = MTLCreateSystemDefaultDevice() ?: throw IllegalStateException("Metal is not supported on this system")
    private val queue = device.newCommandQueue() ?: throw IllegalStateException("Couldn't create Metal command queue")
    private var currentDrawable: CAMetalDrawableProtocol? = null

    /*
     * Initial value is [DrawSchedulingState.AVAILABLE_ON_NEXT_FRAME] because voluntarily dispatching a frame
     * disregarding CADisplayLink timing (which is not accessible while it's paused) can cause frame drifting in worst
     * cases adding one frame latency due to presentation mechanism, if followed by steady draw dispatch
     * (which is often the case).
     * TODO: look closer to what happens after blank frames leave it in AVAILABLE_ON_CURRENT_FRAME. Touch driven events sequence negate that problem.
     */
    private var drawSchedulingState = DrawSchedulingState.AVAILABLE_ON_NEXT_FRAME

    /**
     * UITouch events are dispatched right before next CADisplayLink callback by iOS.
     * It's too late to encode any work for this frame after this happens.
     * Any work dispatched before the next CADisplayLink callback should be scheduled after that callback.
     */
    fun preventDrawDispatchDuringCurrentFrame() {
        if (drawSchedulingState == DrawSchedulingState.AVAILABLE_ON_CURRENT_FRAME) {
            drawSchedulingState = DrawSchedulingState.AVAILABLE_ON_NEXT_FRAME
        }
    }

    /**
     * Needs scheduling displayLink for forcing UITouch events to come at the fastest possible cadence.
     * Otherwise, touch events can come at rate lower than actual display refresh rate.
     */
    var needsProactiveDisplayLink = false
        set(value) {
            field = value

            if (value) {
                caDisplayLink.setPaused(false)
            }
        }

    private val frameListener: NSObject = FrameTickListener {
        when (drawSchedulingState) {
            DrawSchedulingState.AVAILABLE_ON_NEXT_FRAME -> {
                drawSchedulingState = DrawSchedulingState.AVAILABLE_ON_CURRENT_FRAME
            }

            DrawSchedulingState.SCHEDULED_ON_NEXT_FRAME -> {
                draw()

                drawSchedulingState = DrawSchedulingState.AVAILABLE_ON_NEXT_FRAME
            }

            DrawSchedulingState.AVAILABLE_ON_CURRENT_FRAME -> {
                // still available, do nothing
            }
        }

        if (!needsProactiveDisplayLink) {
            caDisplayLink.setPaused(true)
        }
    }

    private val caDisplayLink = CADisplayLink.displayLinkWithTarget(
        target = frameListener,
        selector = NSSelectorFromString(FrameTickListener::onDisplayLinkTick.name)
    )
    init {
        caDisplayLink.setPaused(true)
        caDisplayLink.addToRunLoop(NSRunLoop.mainRunLoop, NSRunLoop.mainRunLoop.currentMode)

        metalLayer.device = device as objcnames.protocols.MTLDeviceProtocol?
    }

    fun makeContext() = DirectContext.makeMetal(device.objcPtr(), queue.objcPtr())

    fun makeRenderTarget(width: Int, height: Int): BackendRenderTarget {
        currentDrawable = metalLayer.nextDrawable()!!
        return BackendRenderTarget.makeMetal(width, height, currentDrawable!!.texture.objcPtr())
    }

    override fun dispose() {
        if (!isDisposed) {
            caDisplayLink.invalidate()
            contextHandler.dispose()
            isDisposed = true
        }
    }

    // TODO: inverse this logic and move it to view
    override fun syncSize() {
        val osView = layer.view!!

        osView.window?.screen?.maximumFramesPerSecond?.let {
            caDisplayLink.preferredFramesPerSecond = it
        }
    }

    override fun needRedraw() {
        check(!isDisposed) { "MetalRedrawer is disposed" }

        drawImmediatelyIfPossible()

        if (drawSchedulingState == DrawSchedulingState.SCHEDULED_ON_NEXT_FRAME) {
            caDisplayLink.setPaused(false)
        }
    }

    override fun redrawImmediately() {
        check(!isDisposed) { "MetalRedrawer is disposed" }

        draw()
    }

    /*
     * Dispatch redraw immediately during current frame if possible and updates [drawSchedulingState] to relevant value
     */
    private fun drawImmediatelyIfPossible() {
        when (drawSchedulingState) {
            DrawSchedulingState.AVAILABLE_ON_NEXT_FRAME -> {
                drawSchedulingState = DrawSchedulingState.SCHEDULED_ON_NEXT_FRAME
            }

            DrawSchedulingState.AVAILABLE_ON_CURRENT_FRAME -> {
                draw()

                drawSchedulingState = DrawSchedulingState.AVAILABLE_ON_NEXT_FRAME
            }

            DrawSchedulingState.SCHEDULED_ON_NEXT_FRAME -> {
                // already scheduled, do nothing
            }
        }
    }

    private fun draw() {
        // TODO: maybe make flush async as in JVM version.
        autoreleasepool { //todo measure performance without autoreleasepool
            if (!isDisposed) {
                contextHandler.draw()
            }
        }
    }

    fun finishFrame() {
        autoreleasepool {
            currentDrawable?.let {
                val commandBuffer = queue.commandBuffer()!!
                commandBuffer.label = "Present"
                commandBuffer.presentDrawable(it)
                commandBuffer.commit()
                currentDrawable = null
            }
        }
    }
}

private class FrameTickListener(val onFrameTick: () -> Unit) : NSObject() {
    @ObjCAction
    fun onDisplayLinkTick() {
        onFrameTick()
    }
}
