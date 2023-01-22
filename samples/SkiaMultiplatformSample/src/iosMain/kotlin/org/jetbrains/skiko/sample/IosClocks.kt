package org.jetbrains.skiko.sample

import org.jetbrains.skia.*
import org.jetbrains.skiko.*


class IosClocks(val layer: SkiaLayer) : SkikoView {

//    val backendTexture = GrBackendTexture.Companion.createFromMetalTexture(TODO("todo ios metal texture"))

    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        canvas.drawCircle(50f, 50f, 50f, Paint().apply {
            color = CLR
            blendMode = BLEND
        })
        canvas.drawRect(Rect(50f, 0f, 200f, 200f), Paint().apply {
            color = 0xccff0000.toInt()
        })
//        canvas.drawCircle(50f, 50f, 50f, Paint().apply {
//            color = CLR
//            blendMode = BLEND
//        })
        canvas.resetMatrix()
    }

}
