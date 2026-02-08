package org.jetbrains.skia

import org.jetbrains.skia.impl.use
import org.jetbrains.skia.util.assertContentDifferent
import org.jetbrains.skiko.tests.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BlenderTest {

    private val originalBytes: ByteArray by lazy {
        renderAndReturnBytes(blender = null)
    }

    private fun renderAndReturnBytes(blender: Blender? = null): ByteArray {
        return Surface.makeRasterN32Premul(20, 20).use {
            val paint = Paint().apply {
                setStroke(true)
                strokeWidth = 2f
            }

            val region = Region().apply {
                op(IRect(3, 3, 18, 18), Region.Op.UNION)
            }

            paint.blender = blender
            it.canvas.drawRegion(region, paint)

            val image = it.makeImageSnapshot()
            Bitmap.makeFromImage(image).readPixels()!!
        }
    }

    private fun blenderTest(blender: () -> Blender) = runTest {
        val modifiedPixels = renderAndReturnBytes(blender = blender())
        assertEquals(originalBytes.size, modifiedPixels.size)

        // we don't check the actual content of the pixels, we only assume they're different when ImageFilter applied
        assertContentDifferent(
            array1 = originalBytes,
            array2 = modifiedPixels,
            message = "pixels with applied Blender should be different"
        )
    }

    @Test
    fun makeForBlender() = blenderTest {
        val runtimeEffect = RuntimeEffect.makeForBlender("""
        half4 main(half4 src, half4 dst) { return half4(0, 1, 0, 1); }
    """.trimIndent())
        runtimeEffect.makeBlender(null)
    }

//    @Test
//    fun arithmetic() = blenderTest {
//        Blender.makeArithmetic(
//            k1 = 0.5f, k2 = 0.5f, k3 = 0.5f, k4 = 0.5f, enforcePMColor = true
//        )
//    }
}
