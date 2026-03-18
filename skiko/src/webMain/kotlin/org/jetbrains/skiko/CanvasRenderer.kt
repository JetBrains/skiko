package org.jetbrains.skiko

import org.jetbrains.skia.*
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skiko.w3c.HTMLCanvasElement
import org.jetbrains.skiko.w3c.window
import org.jetbrains.skiko.wasm.ContextAttributes

/**
 * CanvasRenderer takes an [HTMLCanvasElement] instance and initializes
 * skiko's [Canvas] used for drawing (see [initCanvas]).
 *
 * After initialization [needRedraw] can be used to schedule a call to [drawFrame].
 * [drawFrame] has to be implemented to perform the actual drawing on [canvas].
 *
 * The renderer uses an adaptive frame scheduling strategy to mitigate the effects
 * of browser throttling (e.g. Chrome's Energy Saver mode, which reduces
 * requestAnimationFrame to ~30Hz). When throttling is detected, a setTimeout-based
 * fallback ensures rendering continues at a higher frame rate when the display
 * can still support it.
 */
internal abstract class CanvasRenderer(
    private val contextPointer: NativePointer,
    val width: Int,
    val height: Int,
) {
    private val context: DirectContext
    private var surface: Surface? = null
    private var renderTarget: BackendRenderTarget? = null

    /**
     * An instance of skiko [Canvas] used for drawing.
     * Created in [initCanvas].
     */
    protected var canvas: Canvas? = null
        private set

    init {
        GL.makeContextCurrent(contextPointer)
        context = DirectContext.makeGL()
        initCanvas()
    }

    fun initCanvas() {
        disposeCanvas()

        renderTarget = BackendRenderTarget.makeGL(width, height, 1, 8, 0, 0x8058)
        surface = Surface.makeFromBackendRenderTarget(
            context,
            renderTarget!!,
            SurfaceOrigin.BOTTOM_LEFT,
            SurfaceColorFormat.RGBA_8888,
            ColorSpace.sRGB,
            SurfaceProps()
        ) ?: throw RenderException("Cannot create surface")
        canvas = surface!!.canvas
    }

    private fun disposeCanvas() {
        surface?.close()
        surface = null
        renderTarget?.close()
        renderTarget = null
    }

    /**
     * This function should implement the actual drawing on the canvas.
     *
     * @param currentTimestamp - in milliseconds
     */
    abstract fun drawFrame(currentTimestamp: Double)

    private var redrawScheduled = false
    private var timeoutHandle = -1

    // Throttle detection state
    private var lastRafTimestamp = -1.0
    private var slowFrameCount = 0
    private var isRafThrottled = false

    /**
     * Schedules a call to [drawFrame] to the appropriate moment.
     *
     * Uses a dual-scheduling strategy: always schedules via requestAnimationFrame
     * for proper vsync alignment, but when rAF throttling is detected (e.g. Chrome
     * Energy Saver), also schedules a setTimeout fallback. Whichever fires first
     * wins; the other becomes a no-op via the [redrawScheduled] flag.
     */
    fun needRedraw() {
        if (redrawScheduled) {
            return
        }
        redrawScheduled = true

        // Always schedule via rAF for proper vsync alignment and throttle detection
        window.requestAnimationFrame { timestamp ->
            updateThrottleDetection(timestamp)
            if (redrawScheduled) {
                cancelTimeoutFallback()
                executeFrame(timestamp)
            }
        }

        // When throttled, also schedule a setTimeout fallback.
        // If the display is still at a higher refresh rate than rAF provides,
        // this frame will be composited at the next vsync, reducing input latency.
        if (isRafThrottled) {
            timeoutHandle = window.setTimeout({
                if (redrawScheduled) {
                    executeFrame(window.performance.now())
                }
            }, FALLBACK_FRAME_DELAY_MS)
        }
    }

    private fun executeFrame(timestamp: Double) {
        redrawScheduled = false
        GL.makeContextCurrent(contextPointer)
        // `clear` and `resetMatrix` make canvas not accumulate previous effects
        canvas?.clear(Color.WHITE)
        canvas?.resetMatrix()
        drawFrame(timestamp)
        surface?.flushAndSubmit()
        context.flush()
    }

    private fun cancelTimeoutFallback() {
        if (timeoutHandle != -1) {
            window.clearTimeout(timeoutHandle)
            timeoutHandle = -1
        }
    }

    /**
     * Detects rAF throttling by measuring intervals between consecutive rAF callbacks.
     * If [SLOW_FRAME_DETECTION_COUNT] consecutive frames exceed [SLOW_FRAME_THRESHOLD_MS],
     * we consider rAF throttled and enable the setTimeout fallback.
     */
    private fun updateThrottleDetection(timestamp: Double) {
        if (lastRafTimestamp > 0.0) {
            val delta = timestamp - lastRafTimestamp
            if (delta > SLOW_FRAME_THRESHOLD_MS) {
                slowFrameCount++
                if (slowFrameCount >= SLOW_FRAME_DETECTION_COUNT) {
                    isRafThrottled = true
                }
            } else {
                slowFrameCount = 0
                isRafThrottled = false
            }
        }
        lastRafTimestamp = timestamp
    }

    companion object {
        /**
         * If a rAF interval exceeds this value in ms, the frame is considered "slow".
         * Normal 60Hz = ~16.7ms. We use 20ms as threshold to account for jitter
         * while still catching 30Hz throttling (~33ms).
         */
        private const val SLOW_FRAME_THRESHOLD_MS = 20.0

        /** Number of consecutive slow frames needed to confirm throttling. */
        private const val SLOW_FRAME_DETECTION_COUNT = 3

        /**
         * Delay for the setTimeout fallback in ms.
         * Set to half a normal 60Hz frame (~8ms) so the frame is ready
         * well before the next vsync, without excessive CPU usage.
         */
        private const val FALLBACK_FRAME_DELAY_MS = 8
    }
}

internal external interface GLInterface {
    fun createContext(context: HTMLCanvasElement, contextAttributes: ContextAttributes): NativePointer
    fun makeContextCurrent(contextPointer: NativePointer): Boolean;
}

internal expect val GL: GLInterface