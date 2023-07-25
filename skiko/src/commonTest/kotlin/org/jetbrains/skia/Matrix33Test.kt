package org.jetbrains.skia

import org.jetbrains.skia.tests.assertContentCloseEnough
import kotlin.test.Test

class Matrix33Test {

    @Test
    fun getToXYZD50Presets() {
        Matrix33.sRGBToXYZD50
        Matrix33.adobeRGBToXYZD50
        Matrix33.displayP3ToXYZD50
        Matrix33.rec2020ToXYZD50
        Matrix33.xyzD50ToXYZD50
    }

    @Test
    fun xyzD50ToXYZD50PresetCoefficients() {
        assertContentCloseEnough(floatArrayOf(1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f), Matrix33.xyzD50ToXYZD50.mat)
    }

    @Test
    fun makeXYZToXYZD50() {
        val matD50 = Matrix33.makeXYZToXYZD50(0.34567f, 0.3585f)!!.mat
        assertContentCloseEnough(floatArrayOf(1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f), matD50, 0.001f)

        val matD65 = Matrix33.makeXYZToXYZD50(0.31271f, 0.32902f)!!.mat
        val expD65 = floatArrayOf(1.0478f, 0.0229f, -0.0502f, 0.0295f, 0.9905f, -0.0171f, -0.0093f, 0.0151f, 0.7517f)
        assertContentCloseEnough(expD65, matD65, 0.001f)
    }

    @Test
    fun makePrimariesToXYZD50() {
        val matSRGB = Matrix33.makePrimariesToXYZD50(0.64f, 0.33f, 0.3f, 0.6f, 0.15f, 0.06f, 0.31271f, 0.32902f)!!.mat
        val expSRGB = floatArrayOf(0.4360f, 0.3851f, 0.1431f, 0.2224f, 0.7169f, 0.0606f, 0.0139f, 0.0971f, 0.7139f)
        assertContentCloseEnough(expSRGB, matSRGB, 0.001f)
    }

}
