package org.jetbrains.skiko

import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Color
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Surface
import org.jetbrains.skiko.tests.runTest
import org.w3c.dom.ErrorEvent
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.PromiseRejectionEvent
import org.w3c.dom.events.Event
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class SkiaLayerTest {

    @Test
    fun explicitResizeRecreatesSurface() = runTest {
        withLayer(100, 100) { canvas, layer ->
            val renderDelegate = TestRenderDelegate(fillColor = Color.RED)
            layer.renderDelegate = renderDelegate

            layer.attachTo(canvas)
            val activeContextsAfterAttach = createdGlContexts(canvas)

            assertTrue(activeContextsAfterAttach > 0, "the WebGL spy must observe the first attach")
            assertEquals(100, layer.state?.width)
            assertEquals(100, layer.state?.height)

            layer.requestAndAwaitRender()
            assertEquals(1, renderDelegate.renders, "exactly one frame per needRender()")
            renderDelegate.assertRendered(100, 100, Color.RED, "initial frame")

            canvas.width = 200
            canvas.height = 150
            layer.resize(200, 150)
            assertEquals(200, layer.state?.width)
            assertEquals(150, layer.state?.height)

            layer.requestAndAwaitRender()
            assertEquals(2, renderDelegate.renders, "resize must not skip or duplicate frames")
            renderDelegate.assertRendered(200, 150, Color.RED, "frame after growing resize")

            // Shrinking must recreate the surface as well.
            canvas.width = 60
            canvas.height = 40
            layer.resize(60, 40)
            layer.requestAndAwaitRender()
            assertEquals(3, renderDelegate.renders, "resize must not skip or duplicate frames")
            renderDelegate.assertRendered(60, 40, Color.RED, "frame after shrinking resize")

            // Resizing to the same size is a no-op in CanvasRenderer.resize, but must keep
            // rendering: the canvas drawing buffer is still cleared by the attribute write.
            layer.resize(60, 40)
            layer.requestAndAwaitRender()
            assertEquals(4, renderDelegate.renders, "resize must not skip or duplicate frames")
            renderDelegate.assertRendered(60, 40, Color.RED, "frame after same-size resize")

            assertEquals(
                activeContextsAfterAttach, createdGlContexts(canvas),
                "resize must reuse the existing WebGL context"
            )
        }
    }

    @Test
    fun reattachToSameCanvasReusesWebGlContext() = runTest {
        withLayer(100, 100) { canvas, layer ->
            val renderDelegate = TestRenderDelegate(fillColor = Color.RED)
            layer.renderDelegate = renderDelegate

            layer.attachTo(canvas)
            val initialState = assertNotNull(layer.state)
            val activeContextsAfterAttach = createdGlContexts(canvas)
            assertTrue(activeContextsAfterAttach > 0, "the WebGL spy must observe the first attach")

            layer.requestAndAwaitRender()
            assertEquals(1, renderDelegate.renders, "resize must not skip or duplicate frames")

            // Simulate Compose's behaviour: it calls attachTo() on every resize;
            // each call used to create another emscripten context handle + DirectContext over the same WebGLRenderingContext,
            // and eventually the orphaned one corrupted the live context's GL state cache when the GC
            // finally destroyed it (CMP-8615).
            repeat(100) { i ->
                val newSize = 120 + i * 10
                canvas.width = newSize
                canvas.height = newSize
                layer.attachTo(canvas)

                assertEquals(
                    activeContextsAfterAttach, createdGlContexts(canvas),
                    "same-canvas re-attach must not create another WebGL context / DirectContext"
                )
                assertSame(
                    initialState, layer.state,
                    "same-canvas re-attach must reuse the CanvasRenderer"
                )
                assertEquals(newSize, layer.state?.width)
                assertEquals(newSize, layer.state?.height)

                layer.requestAndAwaitRender()
                assertEquals(2 + i, renderDelegate.renders, "the rendering is expected to continue after re-attach")
                renderDelegate.assertRendered(newSize, newSize, Color.RED, "frame after re-attach #$i")
            }
        }
    }

    @Test
    fun detachAndAttachToDifferentCanvas() = runTest {
        withCanvas(100, 100) { canvas1 ->
            withCanvas(200, 200) { canvas2 ->
                val layer = SkiaLayer()
                try {
                    val renderDelegate = TestRenderDelegate(fillColor = Color.RED)
                    layer.renderDelegate = renderDelegate

                    layer.attachTo(canvas1)
                    val state1 = assertNotNull(layer.state)
                    layer.requestAndAwaitRender()
                    renderDelegate.assertRendered(100, 100, Color.RED, "frame on the first canvas")

                    layer.detach()
                    assertNull(layer.state)
                    assertTrue(state1.isDisposed, "previous renderer must be disposed on detach")

                    layer.attachTo(canvas2)
                    val state2 = assertNotNull(layer.state)
                    assertNotSame(state1, state2, "a different canvas needs a new renderer")
                    assertFalse(state2.isDisposed)
                    assertEquals(200, state2.width)
                    assertEquals(200, state2.height)

                    layer.requestAndAwaitRender()
                    renderDelegate.assertRendered(200, 200, Color.RED, "frame on the second canvas")
                } finally {
                    layer.detach()
                }
            }
        }
    }

    /**
     * "null function" trap on fast window resize: use-after-free of the native `SkCanvas`.
     *
     * `Surface.canvas` is an unmanaged view - the native `SkCanvas` is owned by the `Surface` and
     * dies with it. `initCanvas()` closes the old Surface *before* creating the new one and only
     * assigns `canvas` at the very end:
     *
     *     disposeCanvas()                       // SkCanvas freed; `canvas` still points at it
     *     renderTarget = BackendRenderTarget.makeGL(width, height, ...)
     *     surface = Surface.makeFromBackendRenderTarget(...) ?: throw RenderException(...)
     *     canvas = surface!!.canvas             // skipped when the line above throws
     *
     * so a `resize()` whose surface creation fails leaves the renderer holding a `Canvas` over
     * freed memory, with `surface == null` but `canvas != null`, and `width`/`height` already
     * committed to a size nothing is backing.
     *
     * No reentrancy is needed for this to crash: JS is run-to-completion, so the resize event and
     * the following `requestAnimationFrame` are separate tasks. The next frame - any later frame -
     * does `canvas?.clear(Color.WHITE)` and then hands the dangling canvas to the delegate, where
     * Compose's `RenderNode.drawInto` reaches `canvas->drawDrawable()` and traps on the freed
     * vtable. Freed Skia memory is not scrubbed, which is why the earlier `clear`/`resetMatrix`
     * can still appear to succeed and only a later virtual call reads a zeroed slot.
     *
     * Surface creation failing is reachable during a fast drag in two ways, neither guarded:
     * `resize()` accepts non-positive sizes, and `GL.makeContextCurrent()`'s `Boolean` result is
     * ignored, so a lost or non-current WebGL context is never noticed.
     */
    @Test
    fun failedResizeMustNotLeaveADanglingCanvas() = runTest {
        withLayer(100, 100) { htmlCanvas, layer ->
            val renderDelegate = TestRenderDelegate(fillColor = Color.RED)
            layer.renderDelegate = renderDelegate

            layer.attachTo(htmlCanvas)
            layer.requestAndAwaitRender()
            renderDelegate.assertRendered(100, 100, Color.RED, "initial frame")

            // Far beyond GL_MAX_RENDERBUFFER_SIZE: Surface.makeFromBackendRenderTarget returns null.
            // The HTML canvas attributes are deliberately left alone - allocating a 1M x 1M
            // drawing buffer would lose the WebGL context and mask what is under test.
            val tooBig = 1 shl 20
            val failure = runCatching { layer.resize(tooBig, tooBig) }.exceptionOrNull()
            assertNull(
                failure,
                "a size the GPU cannot back must be skipped, not thrown out of resize(): the throw " +
                        "leaves CanvasRenderer.canvas pointing at the already freed SkCanvas"
            )
            assertEquals(
                100, layer.state?.width,
                "a resize that could not be applied must not commit its size: the reported size " +
                        "has to keep matching the surface that actually exists"
            )
            assertEquals(100, layer.state?.height, "a resize that could not be applied must not commit its size")

            // Today this frame dereferences the freed SkCanvas in `canvas?.clear(Color.WHITE)`.
            var surfaceClosed: Boolean? = null
            layer.renderDelegate = object : SkikoRenderDelegate {
                override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
                    surfaceClosed = (canvas._owner as Surface).isClosed
                    canvas.clear(Color.RED)
                }
            }
            layer.requestAndAwaitRender()
            assertNotEquals(
                true, surfaceClosed,
                "drawFrame() must never receive a Canvas owned by a closed Surface"
            )

            // Zero-sized containers happen while resizing, and must not break the renderer either.
            htmlCanvas.width = 0
            htmlCanvas.height = 0
            assertNull(runCatching { layer.resize(0, 0) }.exceptionOrNull(), "resize(0, 0) must not throw")
            layer.requestAndAwaitRender()

            // ... and the layer has to recover once a usable size arrives.
            val recovered = TestRenderDelegate(fillColor = Color.GREEN)
            layer.renderDelegate = recovered
            htmlCanvas.width = 120
            htmlCanvas.height = 90
            layer.resize(120, 90)
            layer.requestAndAwaitRender()
            recovered.assertRendered(120, 90, Color.GREEN, "frame after recovering from a failed resize")
        }
    }

    @Test
    fun twoLayersOnTwoCanvasesDoNotInterfere() = runTest {
        withCanvas(100, 100) { canvasRed ->
            withCanvas(100, 100) { canvasBlue ->
                val layerRed = SkiaLayer()
                val layerBlur = SkiaLayer()
                try {
                    val renderDelegateRed = TestRenderDelegate(fillColor = Color.RED)
                    val renderDelegateBlue = TestRenderDelegate(fillColor = Color.BLUE)
                    layerRed.renderDelegate = renderDelegateRed
                    layerBlur.renderDelegate = renderDelegateBlue

                    layerRed.attachTo(canvasRed)
                    layerBlur.attachTo(canvasBlue)

                    layerBlur.needRender()
                    layerRed.needRender()
                    awaitAnimationFrame()

                    renderDelegateRed.assertRendered(100, 100, Color.RED, "red layer, initial frame")
                    renderDelegateBlue.assertRendered(100, 100, Color.BLUE, "blue layer, initial frame")
                    assertNoGlError(canvasRed, "after the initial frames")
                    assertNoGlError(canvasBlue, "after the initial frames")

                    // check that layer blue resize makes its context current to not interfere with layer red.
                    layerRed.requestAndAwaitRender()
                    canvasBlue.width = 250
                    canvasBlue.height = 180
                    layerBlur.resize(250, 180)

                    layerBlur.needRender()
                    layerRed.needRender()
                    awaitAnimationFrame()

                    renderDelegateBlue.assertRendered(250, 180, Color.BLUE, "blue layer after resize")
                    renderDelegateRed.assertRendered(100, 100, Color.RED, "red layer must be unaffected by blue layer resize")

                    // red layer context is current here, and blue layer dispose() must not interfere with the red one.
                    layerBlur.detach()
                    layerRed.requestAndAwaitRender()

                    renderDelegateRed.assertRendered(100, 100, Color.RED, "red layer after blue layer was detached")
                    assertNoGlError(canvasRed, "disposing another layer must not affect this one")
                    assertNoGlError(canvasBlue, "no errors after disposing")
                } finally {
                    layerRed.detach()
                    layerBlur.detach()
                }
            }
        }
    }
}

@OptIn(ExperimentalWasmJsInterop::class)
private fun windowRequestAnimationFrame(callback: (Double) -> Unit): Int =
    js("window.requestAnimationFrame(callback)")

/**
 * Counts `canvas.getContext(...)` calls and remembers the returned `WebGLRenderingContext`.
 * Must be installed before the first `attachTo`.
 */
@OptIn(ExperimentalWasmJsInterop::class)
//language=js
private fun installWebGlSpy(canvas: HTMLCanvasElement): Unit = js(
    """(function() {
    var spy = { calls: 0, gl: null };
    canvas.__skikoSpy = spy;
    var original = canvas.getContext;
    canvas.getContext = function() {
        var gl = original.apply(this, arguments);
        if (gl) {
            spy.calls++;
            spy.gl = gl;
        }
        return gl;
    };
})()"""
)

/**
 * Return the number of `getContext` calls that returned a context for this canvas.
 */
@OptIn(ExperimentalWasmJsInterop::class)
private fun createdGlContexts(canvas: HTMLCanvasElement): Int =
    js("canvas.__skikoSpy ? canvas.__skikoSpy.calls : -1")

/**
 * https://developer.mozilla.org/en-US/docs/Web/API/WebGLRenderingContext/getError
 */
@OptIn(ExperimentalWasmJsInterop::class)
private fun glError(canvas: HTMLCanvasElement): Int =
    js("canvas.__skikoSpy && canvas.__skikoSpy.gl ? canvas.__skikoSpy.gl.getError() : -1")

/**
 * Best-effort: Skia calls `glGetError` internally, so it may consume a flag before we look here.
 * Good at catching errors, not proof of their absence - the pixel probes carry that weight.
 */
private fun assertNoGlError(canvas: HTMLCanvasElement, message: String) {
    assertEquals(0, glError(canvas), "$message: unexpected WebGL error")
}

private suspend fun awaitAnimationFrame(): Double = suspendCoroutine { cont ->
    windowRequestAnimationFrame { cont.resume(it) }
}

/**
 * Creates a canvas (with the WebGL spy installed) and guarantees cleanup: a leaked
 * canvas keeps its WebGL context alive, and browsers drop the oldest context once the
 * per-page limit (~16) is reached, which would break unrelated tests.
 */
private suspend fun withCanvas(
    width: Int,
    height: Int,
    block: suspend (HTMLCanvasElement) -> Unit
) {
    val canvas = document.createElement("canvas") as HTMLCanvasElement
    canvas.width = width
    canvas.height = height
    document.body!!.appendChild(canvas)

    installWebGlSpy(canvas)
    try {
        block(canvas)
    } finally {
        canvas.remove()
    }
}

private suspend fun withLayer(
    width: Int,
    height: Int,
    block: suspend (HTMLCanvasElement, SkiaLayer) -> Unit
) = withCanvas(width, height) { canvas ->
    val layer = SkiaLayer()
    try {
        block(canvas, layer)
    } finally {
        layer.detach()
    }
}

/**
 * A delegate that fills the surface and probes the pixel at the far corner of the
 * requested size. The probe fails (or reads an unexpected color) if the Skia surface is
 * smaller than [SkiaLayer]'s reported size - which is what happens if `resize` updates
 * the dimensions without recreating the render target and surface.
 */
private class TestRenderDelegate(private val fillColor: Int) : SkikoRenderDelegate {
    var renders = 0
        private set
    var lastWidth = -1
        private set
    var lastHeight = -1
        private set
    var cornerReadSucceeded = false
        private set
    var cornerColor = 0
        private set

    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        renders++
        lastWidth = width
        lastHeight = height
        canvas.clear(fillColor)

        // Draw actual geometry: `clear` alone is a glClear and never binds a shader program.
        val paint = Paint()
        paint.color = fillColor
        canvas.drawCircle(
            width / 2f,
            height / 2f,
            maxOf(width, height).toFloat(),
            paint
        )
        paint.close()

        val pixel = Bitmap()
        pixel.allocN32Pixels(1, 1)
        cornerReadSucceeded = canvas.readPixels(pixel, width - 1, height - 1)
        cornerColor = if (cornerReadSucceeded) pixel.getColor(0, 0) else 0
        pixel.close()
    }

    fun assertRendered(expectedWidth: Int, expectedHeight: Int, expectedColor: Int, message: String) {
        assertEquals(expectedWidth, lastWidth, "$message: reported width")
        assertEquals(expectedHeight, lastHeight, "$message: reported height")
        assertTrue(
            cornerReadSucceeded,
            "$message: surface must contain the pixel at (${expectedWidth - 1}, ${expectedHeight - 1}); " +
                    "a failed read means the surface is still the old size"
        )
        assertEquals(
            expectedColor, cornerColor,
            "$message: unexpected color at the far corner of the surface"
        )
    }

    fun resetAsserts() {
        lastWidth = -1
        lastHeight = -1
        cornerReadSucceeded = false
        cornerColor = 0
    }
}

private suspend fun SkiaLayer.requestAndAwaitRender() {
    needRender()
    awaitAnimationFrame()
}