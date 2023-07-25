package org.jetbrains.skia

import kotlin.test.*

class ColorSpaceTest {

    @Test
    fun getPresets() {
        ColorSpace.sRGB
        ColorSpace.sRGBLinear
        ColorSpace.displayP3
    }

    @Test
    fun isGammaCloseToSRGB() {
        assertTrue(ColorSpace.sRGB.isGammaCloseToSRGB)
        assertTrue(ColorSpace.makeRGB(TransferFunction.sRGB, Matrix33.displayP3ToXYZD50)!!.isGammaCloseToSRGB)
        assertFalse(ColorSpace.sRGBLinear.isGammaCloseToSRGB)
        assertFalse(ColorSpace.makeRGB(TransferFunction.gamma2Dot2, Matrix33.adobeRGBToXYZD50)!!.isGammaCloseToSRGB)
        assertFalse(ColorSpace.makeRGB(TransferFunction.hlg, Matrix33.rec2020ToXYZD50)!!.isGammaCloseToSRGB)
    }

    @Test
    fun isGammaLinear() {
        assertTrue(ColorSpace.sRGBLinear.isGammaLinear)
        assertTrue(ColorSpace.makeRGB(TransferFunction.linear, Matrix33.adobeRGBToXYZD50)!!.isGammaLinear)
        assertFalse(ColorSpace.sRGB.isGammaLinear)
        assertFalse(ColorSpace.makeRGB(TransferFunction.gamma2Dot2, Matrix33.displayP3ToXYZD50)!!.isGammaLinear)
        assertFalse(ColorSpace.makeRGB(TransferFunction.pq, Matrix33.rec2020ToXYZD50)!!.isGammaLinear)
    }

    @Test
    fun isSRGB() {
        assertTrue(ColorSpace.sRGB.isSRGB)
        assertTrue(ColorSpace.makeRGB(TransferFunction.sRGB, Matrix33.sRGBToXYZD50)!!.isSRGB)
        assertFalse(ColorSpace.sRGBLinear.isSRGB)
        assertFalse(ColorSpace.makeRGB(TransferFunction.sRGB, Matrix33.adobeRGBToXYZD50)!!.isSRGB)
        assertFalse(ColorSpace.makeRGB(TransferFunction.gamma2Dot2, Matrix33.sRGBToXYZD50)!!.isSRGB)
        assertFalse(ColorSpace.makeRGB(TransferFunction.gamma2Dot2, Matrix33.adobeRGBToXYZD50)!!.isSRGB)
    }

    @Test
    fun getTransferFunction() {
        assertEquals(TransferFunction.sRGB, ColorSpace.sRGB.transferFunction)
        assertEquals(TransferFunction.linear, ColorSpace.sRGBLinear.transferFunction)
        assertEquals(TransferFunction.sRGB, ColorSpace.makeRGB(TransferFunction.sRGB, Matrix33.displayP3ToXYZD50)!!.transferFunction)
        assertEquals(TransferFunction.linear, ColorSpace.makeRGB(TransferFunction.linear, Matrix33.adobeRGBToXYZD50)!!.transferFunction)
        assertEquals(TransferFunction.gamma2Dot2, ColorSpace.makeRGB(TransferFunction.gamma2Dot2, Matrix33.rec2020ToXYZD50)!!.transferFunction)
    }

    @Test
    fun getToXYZD50() {
        assertEquals(Matrix33.sRGBToXYZD50, ColorSpace.sRGB.toXYZD50)
        assertEquals(Matrix33.displayP3ToXYZD50, ColorSpace.displayP3.toXYZD50)
        assertEquals(Matrix33.sRGBToXYZD50, ColorSpace.makeRGB(TransferFunction.rec2020, Matrix33.sRGBToXYZD50)!!.toXYZD50)
        assertEquals(Matrix33.adobeRGBToXYZD50, ColorSpace.makeRGB(TransferFunction.linear, Matrix33.adobeRGBToXYZD50)!!.toXYZD50)
        assertEquals(Matrix33.rec2020ToXYZD50, ColorSpace.makeRGB(TransferFunction.gamma2Dot2, Matrix33.rec2020ToXYZD50)!!.toXYZD50)
    }

    @Test
    fun convert() {
        val cs = ColorSpace.sRGB
        val color = cs.convert(ColorSpace.sRGBLinear, Color4f(1f, 0f, 0f, 1f))
        assertNotEquals(color.r, 0f, 0.0001f)
    }

}
