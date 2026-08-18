package org.jetbrains.skia

import org.jetbrains.skia.tests.assertCloseEnough
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MatrixTest {
    @Test
    fun constructorsRequireTheExpectedNumberOfElements() {
        assertInvalidSize("Matrix22", expectedSize = 4) { Matrix22(it) }
        assertInvalidSize("Matrix33", expectedSize = 9) { Matrix33(it) }
        assertInvalidSize("Matrix44", expectedSize = 16) { Matrix44(it) }
    }

    @Test
    fun identities() {
        assertContentEquals(
            floatArrayOf(
                1f, 0f,
                0f, 1f,
            ),
            Matrix22.IDENTITY.mat,
        )
        assertContentEquals(
            floatArrayOf(
                1f, 0f, 0f,
                0f, 1f, 0f,
                0f, 0f, 1f,
            ),
            Matrix33.IDENTITY.mat,
        )
        assertContentEquals(
            floatArrayOf(
                1f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f,
                0f, 0f, 1f, 0f,
                0f, 0f, 0f, 1f,
            ),
            Matrix44.IDENTITY.mat,
        )
    }

    @Test
    fun convertsBetweenMatrix33AndMatrix44() {
        val matrix33 = Matrix33(
            1f, 2f, 3f,
            4f, 5f, 6f,
            7f, 8f, 9f,
        )

        val matrix44 = matrix33.asMatrix44()

        assertContentEquals(
            floatArrayOf(
                1f, 2f, 0f, 3f,
                4f, 5f, 0f, 6f,
                0f, 0f, 1f, 0f,
                7f, 8f, 0f, 9f,
            ),
            matrix44.mat,
        )
        assertCloseEnough(matrix33, matrix44.asMatrix33())
    }

    private fun assertInvalidSize(
        matrixName: String,
        expectedSize: Int,
        createMatrix: (FloatArray) -> Unit,
    ) {
        val actualSize = expectedSize - 1
        val exception = assertFailsWith<IllegalArgumentException>(matrixName) {
            createMatrix(FloatArray(actualSize))
        }

        assertEquals("Expected $expectedSize elements, got $actualSize", exception.message)
    }
}
