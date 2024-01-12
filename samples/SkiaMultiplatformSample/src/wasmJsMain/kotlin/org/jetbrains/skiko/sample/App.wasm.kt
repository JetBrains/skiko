@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
package org.jetbrains.skiko.sample

import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Color
import org.jetbrains.skiko.GenericSkikoView
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.currentNanoTime
import org.jetbrains.skiko.makeGLContextCurrent
import org.jetbrains.skiko.wasm.GL
import org.jetbrains.skiko.wasm.createWebGLContext
import org.w3c.dom.HTMLCanvasElement
import kotlin.time.measureTime

fun main() {
    runApp()
}

@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
internal fun runApp() {
    val skiaLayer = SkiaLayer()
    // onContentScaleChanged = { scale -> println(scale) }
    val game = JsClocks(skiaLayer)
    skiaLayer.skikoView = GenericSkikoView(skiaLayer, game)
    val canvas = document.getElementById("SkikoTarget") as HTMLCanvasElement
    canvas.setAttribute("tabindex", "0")
    skiaLayer.attachTo(canvas)
//    skiaLayer.needRedraw()


//    val glCtx = createWebGLContext(canvas as org.jetbrains.skiko.w3c.HTMLCanvasElement)
//    GL.makeContextCurrent(glCtx)

//    val skikoCanvas = skiaLayer.state!!.canvas!!
    loopGame(game, skiaLayer)
}

private fun loopGame(game: JsClocks, skiaLayer: SkiaLayer) {
    var time = 0L
    val canvas = skiaLayer.state!!.canvas!!

    measureTime {
        repeat(20) {
            makeGLContextCurrent(skiaLayer.state!!.contextPointer)
            canvas.clear(Color.WHITE)
            canvas.resetMatrix()

            // this line takes 99.9% of time, other lines time can be neglected
            game.onRender(canvas, skiaLayer.state!!.width, skiaLayer.state!!.width, time)

            skiaLayer.state!!.surface?.flushAndSubmit()
            skiaLayer.state!!.context.flush()
            time += 16
        }
    }.inWholeMilliseconds.let {
        window.alert("Finished in $it ms")
    }
}