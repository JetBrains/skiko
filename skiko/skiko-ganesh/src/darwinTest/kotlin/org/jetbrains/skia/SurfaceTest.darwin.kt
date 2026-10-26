package org.jetbrains.skia

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.objcPtr
import org.jetbrains.skia.gpu.ganesh.makeFromMTKView
import platform.CoreGraphics.CGRectMake
import platform.MetalKit.MTKView
import kotlin.test.Test
import kotlin.test.assertFailsWith

@OptIn(ExperimentalForeignApi::class)
class SurfaceDarwinTest {
    @Test
    fun mtkViewRejectsUnknownColorFormat() {
        withTestMetalContext { device, context ->
            val view = MTKView(frame = CGRectMake(0.0, 0.0, 8.0, 8.0), device = device)
            assertFailsWith<IllegalArgumentException> {
                Surface.makeFromMTKView(
                    context, view.objcPtr(), SurfaceOrigin.TOP_LEFT, 1,
                    SurfaceColorFormat.UNKNOWN, null, null
                )
            }
        }
    }
}
