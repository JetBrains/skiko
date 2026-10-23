package org.jetbrains.skia

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ColorSpaceTest {
    @Test
    fun gammaProperties() {
        assertTrue(ColorSpace.sRGB.isGammaCloseToSRGB)
        assertFalse(ColorSpace.sRGB.isGammaLinear)
        assertTrue(ColorSpace.sRGBLinear.isGammaLinear)
        assertFalse(ColorSpace.sRGBLinear.isGammaCloseToSRGB)
    }
}
