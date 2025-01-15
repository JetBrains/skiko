package org.jetbrains.skiko.node

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Matrix44
import org.jetbrains.skia.Point

class RenderNode {
//    var matrix: Matrix44

    fun beginRecording(): Canvas = TODO()
    fun endRecording() {}

    fun draw(canvas: Canvas) {}

//    setUseCompositingLayer(true, layerPaint)
//    setHasOverlappingRendering(true)
//    setOutline
//    setRenderEffect
//    setClipToBounds
//    setClipToOutline
}
