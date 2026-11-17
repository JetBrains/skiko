package org.jetbrains.skia

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ColorTypeTest {
    @Test
    fun opacityDependsOnAlphaStorage() {
        assertTrue(ColorType.RGB_565.isAlwaysOpaque)
        assertTrue(ColorType.RGB_888X.isAlwaysOpaque)
        assertFalse(ColorType.RGBA_8888.isAlwaysOpaque)
        assertFalse(ColorType.ALPHA_8.isAlwaysOpaque)
    }
}
