package org.jetbrains.skiko

import kotlinx.browser.document
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Color
import org.jetbrains.skiko.tests.runTest
import org.w3c.dom.HTMLCanvasElement
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalWasmJsInterop::class)
private fun windowRequestAnimationFrame(callback: (Double) -> Unit): Int =
    js("window.requestAnimationFrame(callback)")

class SkiaLayerTest {

    private suspend fun awaitAnimationFrame(): Double = suspendCoroutine { cont ->
        windowRequestAnimationFrame { cont.resume(it) }
    }

    private fun createCanvas(width: Int = 100, height: Int = 100): HTMLCanvasElement {
        val canvas = document.createElement("canvas") as HTMLCanvasElement
        canvas.width = width
        canvas.height = height
        val body = document.body
        if (body != null) {
            body.appendChild(canvas)
        } else {
            document.documentElement?.appendChild(canvas)
        }
        return canvas
    }

    @Test
    fun resizeKeepsRendering() = runTest {
        val canvas = createCanvas(100, 100)
        val layer = SkiaLayer()
        var renderCount = 0
        layer.renderDelegate = object : SkikoRenderDelegate {
            override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
                renderCount++
                canvas.clear(Color.RED)
            }
        }

        layer.attachTo(canvas)
        assertEquals(100, layer.state?.width)
        assertEquals(100, layer.state?.height)

        layer.needRender()
        awaitAnimationFrame()
        assertTrue(renderCount > 0, "Should have rendered at least 1 frame")

        // 1. Explicit resize
        canvas.width = 200
        canvas.height = 150
        layer.resize(200, 150)
        assertEquals(200, layer.state?.width)
        assertEquals(150, layer.state?.height)

        val countBeforeResizeDraw = renderCount
        layer.needRender()
        awaitAnimationFrame()
        assertTrue(renderCount > countBeforeResizeDraw, "Should render frame after resize")

        // 2. Re-attach to same canvas
        canvas.width = 300
        canvas.height = 250
        layer.attachTo(canvas)
        assertEquals(300, layer.state?.width)
        assertEquals(250, layer.state?.height)

        val countBeforeReattachDraw = renderCount
        layer.needRender()
        awaitAnimationFrame()
        assertTrue(renderCount > countBeforeReattachDraw, "Should render frame after re-attach to same canvas")

        layer.detach()
        canvas.remove()
    }

    @Test
    fun reattachToSameCanvasDoesNotCreateNewRendererContext() = runTest {
        val canvas = createCanvas(100, 100)
        val layer = SkiaLayer()

        layer.attachTo(canvas)
        val initialState = layer.state
        assertNotNull(initialState)

        // Re-attach to the same canvas
        layer.attachTo(canvas)
        val stateAfterReattach = layer.state

        assertSame(initialState, stateAfterReattach, "CanvasRenderer instance must be preserved on same-canvas re-attach")

        layer.detach()
        canvas.remove()
    }

    @Test
    fun detachIsSafeWithPendingFrame() = runTest {
        val canvas = createCanvas(100, 100)
        val layer = SkiaLayer()
        var renderCalledAfterDetach = false

        layer.attachTo(canvas)
        layer.renderDelegate = object : SkikoRenderDelegate {
            override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
                if (layer.state == null) {
                    renderCalledAfterDetach = true
                }
            }
        }

        layer.needRender()
        layer.detach()
        assertNull(layer.state)

        awaitAnimationFrame()
        assertTrue(!renderCalledAfterDetach, "onRender must not be called after detach")

        canvas.remove()
    }

    @Test
    fun detachAndAttachToDifferentCanvas() = runTest {
        val canvas1 = createCanvas(100, 100)
        val canvas2 = createCanvas(200, 200)
        val layer = SkiaLayer()

        var renderedWidth = 0
        var renderedHeight = 0
        layer.renderDelegate = object : SkikoRenderDelegate {
            override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
                renderedWidth = width
                renderedHeight = height
            }
        }

        layer.attachTo(canvas1)
        val state1 = layer.state
        assertNotNull(state1)

        layer.needRender()
        awaitAnimationFrame()
        assertEquals(100, renderedWidth)
        assertEquals(100, renderedHeight)

        layer.detach()
        assertNull(layer.state)
        assertTrue(state1.isDisposed, "Previous renderer must be disposed on detach")

        // Attach to different canvas
        layer.attachTo(canvas2)
        val state2 = layer.state
        assertNotNull(state2)
        assertTrue(state2 !== state1, "New renderer instance should be created for different canvas")
        assertEquals(200, state2.width)
        assertEquals(200, state2.height)

        layer.needRender()
        awaitAnimationFrame()
        assertEquals(200, renderedWidth)
        assertEquals(200, renderedHeight)

        layer.detach()
        canvas1.remove()
        canvas2.remove()
    }
}
