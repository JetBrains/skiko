@file:OptIn(ExperimentalSkikoApi::class)

package org.jetbrains.skiko.context

import kotlinx.cinterop.objcPtr
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.Surface
import org.jetbrains.skia.SurfaceColorFormat
import org.jetbrains.skia.SurfaceProps
import org.jetbrains.skia.gpu.graphite.BackendTexture
import org.jetbrains.skia.gpu.graphite.GraphiteContext
import org.jetbrains.skia.gpu.graphite.Recorder
import org.jetbrains.skia.gpu.graphite.makeFromBackendTexture
import org.jetbrains.skiko.ExperimentalSkikoApi
import org.jetbrains.skiko.LayerDrawScope
import org.jetbrains.skiko.RenderException
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.redrawer.MacOsMetalRedrawer

internal class MacOsGraphiteMetalContextHandler(layer: SkiaLayer) : GraphiteContextHandler(layer) {
    private val metalRedrawer: MacOsMetalRedrawer
        get() = layer.redrawer!! as MacOsMetalRedrawer

    var recorder: Recorder? = null
    override fun initContext(): Boolean {
        try {
            if (context == null) {
                context =
                    GraphiteContext.makeMetal(metalRedrawer.device.objcPtr(), metalRedrawer.queue.objcPtr())
                recorder = context!!.makeRecorder()
            }
        } catch (e: Exception) {
            println("${e.message}\nFailed to create Skia Ganesh Metal context!")
            return false
        }
        return true
    }

    override fun LayerDrawScope.initCanvas() {
        disposeCanvas()

        val w = scaledLayerWidth
        val h = scaledLayerHeight

        if (w > 0 && h > 0) {
            backendTexture = BackendTexture.wrapMetalTexture(metalRedrawer.getDrawableTexture(), w, h)

            surface = Surface.makeFromBackendTexture(
                recorder!!,
                backendTexture!!,
                SurfaceColorFormat.BGRA_8888,
                ColorSpace.sRGB,
                SurfaceProps(pixelGeometry = layer.pixelGeometry)
            ) ?: throw RenderException("Cannot create surface")

            canvas = surface!!.canvas
        } else {
            backendTexture = null
            surface = null
            canvas = null
        }
    }

    override fun flush(scope: LayerDrawScope) {
        // TODO: maybe make flush async as in JVM version.
        val recording = recorder!!.snap()
        context!!.insertRecording(recording)
        recording.close()
        context!!.submit()
        metalRedrawer.finishFrame()
    }

    override fun rendererInfo(): String {
        return "Graphite Native Metal: device ${metalRedrawer.device.name}"
    }
}