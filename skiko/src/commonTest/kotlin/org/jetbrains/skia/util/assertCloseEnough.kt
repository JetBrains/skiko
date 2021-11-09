package org.jetbrains.skia.tests

import org.jetbrains.skia.Color4f
import org.jetbrains.skia.Matrix33
import org.jetbrains.skia.Point
import org.jetbrains.skia.Rect
import kotlin.math.abs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private const val EPSILON = 0.00001f

internal fun assertCloseEnough(expected: Float, actual: Float, epsilon: Float = EPSILON) {
    assertTrue(abs(expected - actual) < epsilon, message = "expected=$expected, actual=$actual, eps=$epsilon")
}

internal fun assertCloseEnough(expected: Point, actual: Point?, epsilon: Float = EPSILON) {
    assertCloseEnough(expected.x, actual!!.x, epsilon)
    assertCloseEnough(expected.y, actual.y, epsilon)
}

internal fun assertCloseEnough(expected: Matrix33, actual: Matrix33?, epsilon: Float = EPSILON) {
    expected.mat.zip(actual!!.mat).forEach { (a, b) -> assertCloseEnough(a, b, epsilon) }
}

internal fun assertCloseEnough(expected: Color4f, actual: Color4f?, epsilon: Float = EPSILON) {
    assertCloseEnough(expected.r, actual!!.r, epsilon)
    assertCloseEnough(expected.g, actual.g, epsilon)
    assertCloseEnough(expected.b, actual.b, epsilon)
    assertCloseEnough(expected.a, actual.a, epsilon)
}

internal fun assertCloseEnough(expected: Rect, actual: Rect?, epsilon: Float = EPSILON) {
    assertCloseEnough(expected.left, actual!!.left, epsilon)
    assertCloseEnough(expected.top, actual.top, epsilon)
    assertCloseEnough(expected.right, actual.right, epsilon)
    assertCloseEnough(expected.bottom, actual.bottom, epsilon)
}

private fun fail(message: String) {
    throw AssertionError(message)
}

internal fun assertContentCloseEnough(expected: FloatArray, actual: FloatArray?, epsilon: Float = EPSILON) {
    assertNotNull(actual)

    for (i in expected.indices) {
        if (abs(expected[i] - actual[i]) > epsilon) {
            fail("results differ at index$i, expected ${expected[i]}, got ${actual[i]}")
        }
    }
}

internal fun assertContentCloseEnough(expected: Array<Point>, actual: Array<Point>, epsilon: Float = EPSILON) {
    assertNotNull(actual)
    for (i in expected.indices) {
        try {
            assertCloseEnough(expected[i], actual[i], epsilon)
        } catch (e: AssertionError) {
            fail("results differ at index $i, ${e.message}")
        }
    }
}
