package org.jetbrains.skia

import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.tests.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ShadowUtilsTest {
    @Test
    fun tonalBlackShadowsPreserveAlpha() {
        val ambient = Color.makeARGB(64, 0, 0, 0)
        val spot = Color.makeARGB(128, 0, 0, 0)
        assertEquals(ambient, ShadowUtils.computeTonalAmbientColor(ambient, spot))
        assertEquals(spot, ShadowUtils.computeTonalSpotColor(ambient, spot))
    }

    @Test
    fun drawShadowOnRasterCanvas() = runTest {
        Bitmap().use { bitmap ->
            assertTrue(bitmap.allocPixels(ImageInfo.makeN32Premul(64, 64)))
            Canvas(bitmap).use { canvas ->
                canvas.clear(Color.TRANSPARENT)
                PathBuilder().use { builder ->
                    builder.addRect(Rect(20f, 20f, 40f, 40f)).detach().use { path ->
                        ShadowUtils.drawShadow(
                            canvas, path,
                            zPlaneParams = Point3(0f, 0f, 8f),
                            lightPos = Point3(30f, 30f, 100f),
                            lightRadius = 10f,
                            ambientColor = Color.BLACK,
                            spotColor = Color.BLACK,
                            transparentOccluder = true,
                            geometricOnly = true
                        )
                    }
                }
                assertTrue((0 until 64).any { y ->
                    (0 until 64).any { x -> Color.getA(bitmap.getColor(x, y)) > 0 }
                })
                assertTrue(Color.getA(bitmap.getColor(30, 30)) > 0)
                assertEquals(Color.TRANSPARENT, bitmap.getColor(0, 0))
            }
        }
    }
}
