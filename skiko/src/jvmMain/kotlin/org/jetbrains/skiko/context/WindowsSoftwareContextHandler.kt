package org.jetbrains.skiko.context

import org.jetbrains.skia.Surface
import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.redrawer.WindowsSoftwareRedrawer
import java.lang.ref.Reference

internal class WindowsSoftwareContextHandler(layer: SkiaLayer) : ContextHandler(layer) {
    override val bleachConstant = -1
    var isInited = false

    val softwareRedrawer: WindowsSoftwareRedrawer
        get() = layer.redrawer!! as WindowsSoftwareRedrawer

    private var currentWidth = 0
    private var currentHeight = 0
    private fun isSizeChanged(width: Int, height: Int): Boolean {
        if (width != currentWidth || height != currentHeight) {
            currentWidth = width
            currentHeight = height
            return true
        }
        return false
    }

    override fun initContext(): Boolean {
        if (!isInited) {
            if (System.getProperty("skiko.hardwareInfo.enabled") == "true") {
                println(rendererInfo())
            }
            isInited = true
        }
        return isInited
    }

    override fun initCanvas() {
        val scale = layer.contentScale
        val w = (layer.width * scale).toInt().coerceAtLeast(0)
        val h = (layer.height * scale).toInt().coerceAtLeast(0)
        if (isSizeChanged(w, h)) {
            disposeCanvas()
            softwareRedrawer.resize(w, h)
            surface = softwareRedrawer.getSurface()
            canvas = surface!!.canvas
        }
    }

    override fun flush() {
        softwareRedrawer.finishFrame()
    }
}