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

internal class GraphiteGpuContextBackend private constructor(
    private val context: GraphiteContext,
    private val recorder: Recorder,
) : GpuContextBackend {
    companion object {
        fun makeMetal(
            devicePtr: NativePointer,
            queuePtr: NativePointer,
        ) = create(GraphiteContext.makeMetal(devicePtr, queuePtr))

        private fun create(context: GraphiteContext): GraphiteGpuContextBackend {
            val recorder = try {
                context.makeRecorder()
            } catch (error: Throwable) {
                context.close()
                throw error
            }
            return GraphiteGpuContextBackend(context, recorder)
        }
    }

    override fun makeSurface(
        texture: GpuTexture,
        colorSpace: ColorSpace?,
        surfaceProps: SurfaceProps?,
    ): Surface? {
        val backendTexture = when (texture) {
            is MetalGpuTexture -> BackendTexture.makeMetal(
                texture.width,
                texture.height,
                texture.texturePtr,
            )

        }
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
