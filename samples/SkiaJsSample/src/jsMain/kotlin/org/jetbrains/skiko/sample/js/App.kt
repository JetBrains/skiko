package org.jetbrains.skiko.sample.js

import kotlinx.browser.document
import org.jetbrains.skia.*
import org.jetbrains.skiko.wasm.onWasmReady
import org.w3c.dom.HTMLCanvasElement


fun main(args: Array<String>) {
    onWasmReady {
        val paint = Paint()
        paint.setColor(200)

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
                canvas.width, canvas.height, 0x80A9, 0x0D57, 0, 0
            ),
            SurfaceOrigin.TOP_LEFT, SurfaceColorFormat.RGBA_8888,
            ColorSpace.sRGB
        )

        console.log(surface)
    }
}


external fun GetWebGLContext(canvas: HTMLCanvasElement, attrs: dynamic): dynamic
