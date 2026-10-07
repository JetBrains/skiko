@file:OptIn(org.jetbrains.skiko.ExperimentalSkikoApi::class)

package org.jetbrains.skia.gpu

import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.Surface
import org.jetbrains.skia.SurfaceProps
import org.jetbrains.skia.gpu.graphite.BackendTexture
import org.jetbrains.skia.gpu.graphite.GraphiteContext
import org.jetbrains.skia.gpu.graphite.Recorder
import org.jetbrains.skia.gpu.graphite.wrapBackendTexture
import org.jetbrains.skia.impl.NativePointer

internal class GraphiteMetalContext(private val context: GraphiteContext, private val recorder: Recorder) : GpuContextBackend {
    override fun makeSurface(width: Int, height: Int, texturePtr: NativePointer, colorSpace: ColorSpace?, surfaceProps: SurfaceProps?): Surface? {
        val backendTexture = BackendTexture.makeMetal(width, height, texturePtr)
        val surface = Surface.wrapBackendTexture(recorder, backendTexture, colorSpace, surfaceProps)
        if (surface == null) {
            backendTexture.close()
        }
        return surface
    }

    override fun submit(syncCpu: Boolean) {
        val recording = recorder.snap()
        try {
            context.insertRecording(recording)
            context.submit(syncCpu)
        } finally {
            recording.close()
        }
    }

    override fun close() {
        recorder.close()
        context.close()
    }
}
