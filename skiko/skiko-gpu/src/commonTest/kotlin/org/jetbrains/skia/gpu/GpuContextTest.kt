@file:OptIn(
    kotlinx.cinterop.ExperimentalForeignApi::class,
    org.jetbrains.skiko.ExperimentalSkikoApi::class,
    org.jetbrains.skiko.InternalSkikoApi::class,
)

package org.jetbrains.skia.gpu

import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.Surface
import org.jetbrains.skia.SurfaceProps
import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.impl.NativePointer
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
            context.makeSurface(1, 1, Native.NullPointer)
        }
    }

    @Test
    fun submitsOutstandingWork() {
        val backend = FakeGpuContextBackend()
        val context = GpuContext(backend)

        context.submit()

        assertEquals(1, backend.submitCount)
    }

    private class FakeGpuContextBackend : GpuContextBackend {
        var closeCount = 0

        override fun makeSurface(
            width: Int,
            height: Int,
            texturePtr: NativePointer,
            colorSpace: ColorSpace?,
            surfaceProps: SurfaceProps?,
        ): Surface {
            return Surface.makeRasterN32Premul(width, height)
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
