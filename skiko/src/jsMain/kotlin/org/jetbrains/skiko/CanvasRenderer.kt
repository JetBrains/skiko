package org.jetbrains.skiko

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

abstract class CanvasRenderer constructor(htmlCanvas: HTMLCanvasElement, val width: Int, val height: Int) {
    constructor(canvas: HTMLCanvasElement): this(canvas, canvas.width, canvas.height)
    private val contextPointer = CreateWebGLContext(htmlCanvas)

    init {
        GL.makeContextCurrent(contextPointer)
    }
    private val context = DirectContext.makeGL()
    private val surface = Surface.makeFromBackendRenderTarget(
        context,
        BackendRenderTarget.makeGL(
            width, height, 1, 8, 0, 0x8058
        ),
        SurfaceOrigin.BOTTOM_LEFT,
        SurfaceColorFormat.RGBA_8888,
        ColorSpace.sRGB
    )
    val canvas: Canvas
        get() = surface.canvas

    abstract fun drawFrame()

    fun needRedraw() {
        GL.makeContextCurrent(contextPointer)
        canvas.clear(-1)
        drawFrame()
        surface.flushAndSubmit()
        context.flush()
    }
}
