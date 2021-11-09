package org.jetbrains.skia.tests

import org.jetbrains.skia.Color4f
import org.jetbrains.skia.Matrix33
import org.jetbrains.skia.Point
import org.jetbrains.skia.Rect
import kotlin.math.abs
import kotlin.test.assertTrue

private const val EPSILON = 0.00001f

private inline fun Float.isCloseEnoughTo(b: Float, epsilon: Float) = abs(this - b) < epsilon
private inline fun Point.isCloseEnoughTo(b: Point, epsilon: Float) = x.isCloseEnoughTo(b.x, epsilon) && y.isCloseEnoughTo(b.y, epsilon)
private inline fun Color4f.isCloseEnoughTo(otherColor: Color4f, epsilon: Float) =
    r.isCloseEnoughTo(otherColor.r, epsilon) && g.isCloseEnoughTo(otherColor.g, epsilon) && b.isCloseEnoughTo(otherColor.b, epsilon) && a.isCloseEnoughTo(otherColor.a, epsilon)

internal fun assertCloseEnough(expected: Float, actual: Float, epsilon: Float = EPSILON) {
    assertTrue(expected.isCloseEnoughTo(actual, epsilon), message = "expected=$expected, actual=$actual, eps=$epsilon")
}

internal fun assertCloseEnough(expected: Point, actual: Point, epsilon: Float = EPSILON) {
    assertTrue(expected.isCloseEnoughTo(actual, epsilon), message = "expected=$expected, actual=$actual, eps=$epsilon")
}

internal fun assertCloseEnough(expected: Matrix33, actual: Matrix33, epsilon: Float = EPSILON) {
    assertTrue(expected.mat.zip(actual.mat).all { (a, b) -> a.isCloseEnoughTo(b) }
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

internal fun <T> assertContentEquivalent(expected: Iterator<T>, actual: Iterator<T>, eq: (a: T, b: T) -> Boolean) {
    var count = 0

    while (expected.hasNext() && actual.hasNext()) {
        val a = expected.next()
        val b = actual.next()
        if (!eq(a, b)) {
            fail("results differ at index$count, expected ${a}, got ${b}")
        }
        count++
    }

    if (expected.hasNext()) {
        fail("expected $expected has more items than actual $actual (which has $count)")
    }

    if (actual.hasNext()) {
        fail("actual $actual has more items than actual $expected (which has $expected)")
    }
}

internal fun assertContentCloseEnough(expected: FloatArray, actual: FloatArray, epsilon: Float = EPSILON) {
    assertContentEquivalent(expected.iterator(), actual.iterator()) { a, b -> a.isCloseEnoughTo(b, epsilon)  }
}

internal fun assertContentCloseEnough(expected: Array<Point>, actual: Array<Point>, epsilon: Float = EPSILON) {
    assertContentEquivalent(expected.iterator(), actual.iterator()) { a, b ->
        assertCloseEnough(a, b, epsilon)
        true
    }
}
