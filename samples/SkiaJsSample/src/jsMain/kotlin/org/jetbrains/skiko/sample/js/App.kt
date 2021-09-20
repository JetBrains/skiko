package org.jetbrains.skiko.sample.js

import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.skia.*
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

        val webGlContext = GetWebGLContext(
            canvas, js("{}")
        )

        console.log(webGlContext)
        console.log(canvas)

        val grContext = DirectContext.makeGL()

        console.log(grContext)

        val surface = Surface.makeFromBackendRenderTarget(
            grContext,
            BackendRenderTarget.makeGL(
                canvas.width, canvas.height, 1, 8, 0, 0x8058
            ),
            SurfaceOrigin.BOTTOM_LEFT,
            SurfaceColorFormat.RGBA_8888,
            ColorSpace.sRGB
        )

        console.log(surface)
        console.log(surface.canvas)


        fun draw() {
            surface.canvas.drawCircle(50f, 50f, 25f, paint)
            window.requestAnimationFrame { draw() }
            surface.flushAndSubmit()
        }

        draw()
    }
}


external fun GetWebGLContext(canvas: HTMLCanvasElement, attrs: dynamic): dynamic
