package org.jetbrains.skia

import org.jetbrains.skia.tests.assertCloseEnough
import kotlin.test.Test
import kotlin.test.assertEquals

class TransferFunctionTest {

    @Test
    fun getPresets() {
        TransferFunction.sRGB.type
        TransferFunction.gamma2Dot2.type
        TransferFunction.linear.type
        TransferFunction.rec2020.type
        TransferFunction.pq.type
        TransferFunction.hlg.type
    }

    @Test
    fun linearPresetCoefficients() {
        val tf = TransferFunction.linear
        assertCloseEnough(1f, tf.g)
        assertCloseEnough(1f, tf.a)
        assertCloseEnough(0f, tf.b)
        assertCloseEnough(0f, tf.c)
        assertCloseEnough(0f, tf.d)
        assertCloseEnough(0f, tf.e)
        assertCloseEnough(0f, tf.f)
    }

    @Test
    fun gamma2Dot2PresetCoefficients() {
        val tf = TransferFunction.gamma2Dot2
        assertCloseEnough(2.2f, tf.g)
        assertCloseEnough(1f, tf.a)
        assertCloseEnough(0f, tf.b)
        assertCloseEnough(0f, tf.c)
        assertCloseEnough(0f, tf.d)
        assertCloseEnough(0f, tf.e)
        assertCloseEnough(0f, tf.f)
    }

    @Test
    fun presetTypes() {
        assertEquals(TransferFunctionType.SRGB_ISH, TransferFunction.sRGB.type)
        assertEquals(TransferFunctionType.SRGB_ISH, TransferFunction.gamma2Dot2.type)
        assertEquals(TransferFunctionType.SRGB_ISH, TransferFunction.linear.type)
        assertEquals(TransferFunctionType.SRGB_ISH, TransferFunction.rec2020.type)
        assertEquals(TransferFunctionType.PQ_ISH, TransferFunction.pq.type)
        assertEquals(TransferFunctionType.HLG_ISH, TransferFunction.hlg.type)
    }

    @Test
    fun madeTypes() {
        assertEquals(TransferFunctionType.PQ_ISH, TransferFunction.makePQish(-1f, 2f, 0.1f, 10f, -10f, 4f).type)
        assertEquals(TransferFunctionType.HLG_ISH, TransferFunction.makeScaledHLGish(3f, 2f, 4f, 10f, 0.1f, 0.5f).type)
    }

    @Test
    fun eval() {
        assertCloseEnough(0.1f, TransferFunction.linear.eval(0.1f), 0.0001f)
        assertCloseEnough(0.214f, TransferFunction.sRGB.eval(0.5f), 0.0001f)
        assertCloseEnough(0.2176f, TransferFunction.gamma2Dot2.eval(0.5f), 0.0001f)
    }

    @Test
    fun invertAndEval() {
        assertCloseEnough(0.1f, TransferFunction.linear.invert()!!.eval(0.1f), 0.0001f)
        assertCloseEnough(0.5f, TransferFunction.sRGB.invert()!!.eval(0.214f), 0.0001f)
        assertCloseEnough(0.5f, TransferFunction.gamma2Dot2.invert()!!.eval(0.2176f), 0.0001f)
    }

}
