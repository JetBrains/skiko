package org.jetbrains.skia

import org.jetbrains.skia.gpu.ganesh.makeFromImage
import org.jetbrains.skia.gpu.ganesh.makeRenderTarget
import org.jetbrains.skia.gpu.ganesh.recordingContext
import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.Arch
import org.jetbrains.skiko.KotlinBackend
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostArch
import org.jetbrains.skiko.hostOs
import org.jetbrains.skiko.kotlinBackend
import org.jetbrains.skiko.tests.TestGlContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotNull

class GaneshSurfaceTest {
    @Test
    fun canMakeRenderTarget() {
        if (!TestGlContext.isAvailable()) return

        if (hostOs == OS.Linux && kotlinBackend == KotlinBackend.Native && hostArch == Arch.Arm64) {
            // TODO: fix test on Linux arm64 using EGL
            return
        }
        val pixels = TestGlContext.run {
            DirectContext.makeGL().useContext { ctx ->
                val imageInfo = ImageInfo.makeN32Premul(16, 16)
                val surface = Surface.makeRenderTarget(ctx, budgeted = false, imageInfo)
                val surfaceContext = surface.recordingContext
                assertNotNull(surfaceContext)
                assertFails { surfaceContext.close() }

                surface.canvas.drawRect(
                    r = Rect(4f, 4f, 12f, 12f),
                    paint = Paint().apply { color = Color.RED }
                )
                Bitmap.makeFromImage(surface.makeImageSnapshot(), ctx)
            }
        }

        assertEquals(Color.RED, pixels.getColor(8, 8))
    }
}
