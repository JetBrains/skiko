package org.jetbrains.skiko.sample.js

import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.skia.*
import org.jetbrains.skiko.wasm.GetWebGLContextK
import org.jetbrains.skiko.wasm.onWasmReady
import org.w3c.dom.HTMLCanvasElement


fun main(args: Array<String>) {
    onWasmReady {
        val paint = Paint()
        paint.setColor4f(Color4f(0f, 1f, 0f, 1.0f))
        paint.setMode(PaintMode.FILL)
        paint.setAntiAlias(true)

        val canvas = document.getElementById("c") as HTMLCanvasElement
        canvas.getContext("webgl")

        GetWebGLContextK(canvas)

        val grContext = DirectContext.makeGL()

        val surface = Surface.makeFromBackendRenderTarget(
            grContext,
            BackendRenderTarget.makeGL(
                canvas.width, canvas.height, 1, 8, 0, 0x8058
            ),
            SurfaceOrigin.BOTTOM_LEFT,
            SurfaceColorFormat.RGBA_8888,
            ColorSpace.sRGB
        )

        fun draw() {
            surface.canvas.drawCircle(50f, 50f, 25f, paint)
            paint.setColor4f(Color4f(1f, 1f, 0f, 1.0f))
            surface.canvas.drawCircle(100f, 100f, 10f, paint)
            surface.canvas.drawCircle(200f, 200f, 30f, paint)
            window.requestAnimationFrame { draw() }
            surface.flushAndSubmit()
        }

        draw()
    }
}


external fun GetWebGLContext(canvas: HTMLCanvasElement, attrs: dynamic): dynamic
