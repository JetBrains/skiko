package org.jetbrains.skiko.wasm.api

import kotlinx.browser.window
import org.jetbrains.skia.BackendRenderTarget
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.DirectContext
import org.jetbrains.skia.Surface
import org.jetbrains.skia.SurfaceColorFormat
import org.jetbrains.skia.SurfaceOrigin
import org.jetbrains.skiko.wasm.CreateWebGLContext
import org.jetbrains.skiko.wasm.GL
import org.w3c.dom.HTMLCanvasElement

fun Surface.Companion.createFromGL(width: Int, height: Int) = makeFromBackendRenderTarget(
    DirectContext.makeGL(),
    BackendRenderTarget.makeGL(
        width, height, 1, 8, 0, 0x8058
    ),
    SurfaceOrigin.BOTTOM_LEFT,
    SurfaceColorFormat.RGBA_8888,
    ColorSpace.sRGB
)

abstract class CanvasRenderer constructor(htmlCanvas: HTMLCanvasElement, val width: Int, val height: Int) {
    constructor(canvas: HTMLCanvasElement): this(canvas, canvas.width, canvas.height)
    private val contextPointer = CreateWebGLContext(htmlCanvas)

    init {
        GL.makeContextCurrent(contextPointer)
    }

    private val surface = Surface.createFromGL(width, height)
    val canvas: Canvas
        get() = surface.canvas

    abstract fun drawFrame(currentTimestamp: Double)

    fun draw() {
        window.requestAnimationFrame { timestamp ->
            GL.makeContextCurrent(contextPointer)
            drawFrame(timestamp)
            surface.flushAndSubmit()
        }
    }
}