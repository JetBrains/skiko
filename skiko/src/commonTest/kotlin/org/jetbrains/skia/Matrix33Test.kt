package org.jetbrains.skia

import org.jetbrains.skia.tests.assertCloseEnough
import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals

class Matrix33Test {
    @Test
    fun translation() {
        assertMatrixEquals(
            floatArrayOf(
                1f, 0f, 3f,
                0f, 1f, -4f,
                0f, 0f, 1f,
            ),
            Matrix33.makeTranslate(3f, -4f),
        )
    }

    @Test
    fun scale() {
        assertMatrixEquals(
            floatArrayOf(
                2f, 0f, 0f,
                0f, 2f, 0f,
                0f, 0f, 1f,
            ),
            Matrix33.makeScale(2f),
        )
        assertMatrixEquals(
            floatArrayOf(
                2f, 0f, 0f,
                0f, -3f, 0f,
                0f, 0f, 1f,
            ),
            Matrix33.makeScale(2f, -3f),
        )
    }

    @Test
    fun skew() {
        assertMatrixEquals(
            floatArrayOf(
                1f, 2f, 0f,
                -3f, 1f, 0f,
                0f, 0f, 1f,
            ),
            Matrix33.makeSkew(2f, -3f),
        )
    }

    @Test
    fun rotationAroundOrigin() {
        assertMatrixEquals(
            floatArrayOf(
                0f, -1f, 0f,
                1f, 0f, 0f,
                0f, 0f, 1f,
            ),
            Matrix33.makeRotate(90f),
        )
        assertMatrixEquals(Matrix33.IDENTITY.mat, Matrix33.makeRotate(360f))
    }

    @Test
    fun rotationAroundPivot() {
        val expected = floatArrayOf(
            0f, -1f, 30f,
            1f, 0f, 10f,
            0f, 0f, 1f,
        )

        assertMatrixEquals(expected, Matrix33.makeRotate(90f, 10f, 20f))
        assertMatrixEquals(expected, Matrix33.makeRotate(90f, Point(10f, 20f)))
    }

    @Test
    fun preScale() {
        val matrix = Matrix33(
            1f, 2f, 3f,
            4f, 5f, 6f,
            7f, 8f, 9f,
        )

        assertMatrixEquals(
            floatArrayOf(
                2f, -6f, 3f,
                8f, -15f, 6f,
                14f, -24f, 9f,
            ),
            matrix.makePreScale(2f, -3f),
        )
    }

    @Test
    fun concat() {
        val left = Matrix33(
            1f, 2f, 3f,
            4f, 5f, 6f,
            7f, 8f, 9f,
        )
        val right = Matrix33(
            9f, 8f, 7f,
            6f, 5f, 4f,
            3f, 2f, 1f,
        )

        assertMatrixEquals(
            floatArrayOf(
                30f, 24f, 18f,
                84f, 69f, 54f,
                138f, 114f, 90f,
            ),
            left.makeConcat(right),
        )
    }

    @Test
    fun floatToRadians() {
        assertEquals(0.0, 0f.toRadians(), 0.0)
        assertEquals(PI / 2.0, 90f.toRadians(), 1e-12)
        assertEquals(-PI, (-180f).toRadians(), 1e-12)
        assertEquals(2.0 * PI, 360f.toRadians(), 1e-12)
    }

    private fun assertMatrixEquals(expected: FloatArray, actual: Matrix33) {
        assertCloseEnough(Matrix33(expected), actual)
    }
}
