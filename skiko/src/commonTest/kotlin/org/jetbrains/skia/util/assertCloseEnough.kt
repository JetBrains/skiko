package org.jetbrains.skia.tests

import org.jetbrains.skia.Color4f
import org.jetbrains.skia.Matrix33
import org.jetbrains.skia.Point
import kotlin.math.abs
import kotlin.test.assertTrue

private const val EPSILON = 0.00001f

internal fun assertCloseEnough(expected: Float, actual: Float, epsilon: Float = EPSILON) {
    assertTrue(abs(expected - actual) < epsilon)
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