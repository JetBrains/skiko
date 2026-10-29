@file:OptIn(
    kotlinx.cinterop.ExperimentalForeignApi::class,
    org.jetbrains.skiko.ExperimentalSkikoApi::class,
    org.jetbrains.skiko.InternalSkikoApi::class,
)

package org.jetbrains.skia.gpu

import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.Surface
import org.jetbrains.skia.SurfaceProps
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GpuContextTest {
    @Test
    fun closeIsIdempotent() {
        val backend = FakeGpuContextBackend()
        val context = GpuContext(backend)

        context.close()
        context.close()

        assertEquals(1, backend.closeCount)
    }

    @Test
    fun closedContextRejectsOperations() {
        val context = GpuContext(FakeGpuContextBackend())
        context.close()

        assertFailsWith<IllegalStateException> {
            context.makeSurface(FakeGpuTexture())
        }
    }

    @Test
    fun submitsOutstandingWork() {
        val backend = FakeGpuContextBackend()
        val context = GpuContext(backend)

        context.submit()

        assertEquals(1, backend.submitCount)
    }

    private class FakeGpuTexture : GpuTexture {
        override val width = 1
        override val height = 1
        override fun close() = Unit
    }

    private class FakeGpuContextBackend : GpuContextBackend {
        var closeCount = 0

        override fun makeSurface(
            texture: GpuTexture,
            colorSpace: ColorSpace?,
            surfaceProps: SurfaceProps?,
        ): Surface {
            return Surface.makeRasterN32Premul(texture.width, texture.height)
        }

        var submitCount = 0

        override fun submit(syncCpu: Boolean) {
            submitCount++
        }

        override fun close() {
            closeCount++
        }
    }
}
