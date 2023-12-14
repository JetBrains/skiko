package org.jetbrains.skia.tests

import org.jetbrains.skia.Color4f
import org.jetbrains.skia.FontMetrics
import org.jetbrains.skia.Matrix33
import org.jetbrains.skia.Point
import org.jetbrains.skia.Rect
import org.jetbrains.skia.paragraph.LineMetrics
import org.jetbrains.skia.paragraph.Shadow
import org.jetbrains.skia.paragraph.TextBox
import org.jetbrains.skiko.KotlinBackend
import org.jetbrains.skiko.kotlinBackend
import kotlin.math.abs
import kotlin.test.assertTrue

private val EPSILON = if (kotlinBackend == KotlinBackend.JS) 0.00001f else 0.00000001f

private inline fun Float?.isCloseEnoughTo(b: Float?, epsilon: Float) =
    if (this == null) b == null else if (b == null) false else if (epsilon == 0f) this == b else abs(this - b) < epsilon

private inline fun Double?.isCloseEnoughTo(b: Double?, epsilon: Float) =
    if (this == null) b == null else if (b == null) false else if (epsilon == 0f) this == b else abs(this - b) < epsilon

private inline fun Point.isCloseEnoughTo(b: Point, epsilon: Float) =
    x.isCloseEnoughTo(b.x, epsilon) && y.isCloseEnoughTo(b.y, epsilon)

private inline fun Shadow.isCloseEnoughTo(b: Shadow, epsilon: Float) =
    (color == b.color)
        && (offsetX.isCloseEnoughTo(b.offsetX, epsilon))
        && (offsetY.isCloseEnoughTo(b.offsetY, epsilon))
        && (blurSigma == b.blurSigma)

private inline fun Color4f.isCloseEnoughTo(otherColor: Color4f, epsilon: Float) =
    r.isCloseEnoughTo(otherColor.r, epsilon) && g.isCloseEnoughTo(
        otherColor.g,
        epsilon
    ) && b.isCloseEnoughTo(otherColor.b, epsilon) && a.isCloseEnoughTo(otherColor.a, epsilon)

private inline fun FontMetrics.isCloseEnoughTo(b: FontMetrics, epsilon: Float) =
        top.isCloseEnoughTo(b.top, epsilon) &&
        ascent.isCloseEnoughTo(b.ascent, epsilon) &&
        descent.isCloseEnoughTo(b.descent, epsilon) &&
        bottom.isCloseEnoughTo(b.bottom, epsilon) &&
        leading.isCloseEnoughTo(b.leading, epsilon) &&
        avgCharWidth.isCloseEnoughTo(b.avgCharWidth, epsilon) &&
        maxCharWidth.isCloseEnoughTo(b.maxCharWidth, epsilon) &&
        xMin.isCloseEnoughTo(b.xMin, epsilon) &&
        xHeight.isCloseEnoughTo(b.xHeight, epsilon) &&
        capHeight.isCloseEnoughTo(b.capHeight, epsilon) &&
        underlineThickness.isCloseEnoughTo(b.underlineThickness, epsilon) &&
        underlinePosition.isCloseEnoughTo(b.underlinePosition, epsilon) &&
        strikeoutThickness.isCloseEnoughTo(b.strikeoutThickness, epsilon) &&
        strikeoutPosition.isCloseEnoughTo(b.strikeoutPosition, epsilon)

private inline fun LineMetrics.isCloseEnoughTo(b: LineMetrics, epsilon: Float): Boolean =
    startIndex == b.startIndex &&
    endIndex == b.endIndex &&
    endExcludingWhitespaces == b.endExcludingWhitespaces &&
    endIncludingNewline == b.endIncludingNewline &&
    isHardBreak == b.isHardBreak &&
    ascent.isCloseEnoughTo(b.ascent, epsilon) &&
    descent.isCloseEnoughTo(b.descent, epsilon) &&
    unscaledAscent.isCloseEnoughTo(b.unscaledAscent, epsilon) &&
    height.isCloseEnoughTo(b.height, epsilon) &&
    width.isCloseEnoughTo(b.width, epsilon) &&
    baseline.isCloseEnoughTo(b.baseline, epsilon) &&
    lineNumber == b.lineNumber

private inline fun Rect.isCloseEnoughTo(rect: Rect, epsilon: Float): Boolean =
    left.isCloseEnoughTo(rect.left, epsilon) && right.isCloseEnoughTo(rect.right, epsilon)
            && top.isCloseEnoughTo(rect.top, epsilon) && bottom.isCloseEnoughTo(rect.bottom, epsilon)

private inline fun TextBox.isCloseEnoughTo(textBox: TextBox, epsilon: Float = EPSILON): Boolean {
    return (direction == textBox.direction) && rect.isCloseEnoughTo(textBox.rect, epsilon)
}

internal fun assertCloseEnough(expected: Float, actual: Float, epsilon: Float = EPSILON) {
    assertTrue(expected.isCloseEnoughTo(actual, epsilon), message = "expected=$expected, actual=$actual, eps=$epsilon")
}

internal fun assertCloseEnough(expected: Point, actual: Point, epsilon: Float = EPSILON) {
    assertTrue(expected.isCloseEnoughTo(actual, epsilon), message = "expected=$expected, actual=$actual, eps=$epsilon")
}

internal fun assertCloseEnough(expected: TextBox, actual: TextBox, epsilon: Float = EPSILON) {
    assertTrue(expected.isCloseEnoughTo(actual, epsilon), message = "expected=$expected, actual=$actual, eps=$epsilon")
}

internal fun assertCloseEnough(expected: FontMetrics, actual: FontMetrics, epsilon: Float = EPSILON) {
    assertTrue(expected.isCloseEnoughTo(actual, epsilon), message = "expected=$expected, actual=$actual, eps=$epsilon")
}

internal fun assertCloseEnough(expected: Matrix33, actual: Matrix33, epsilon: Float = EPSILON) {
    assertTrue(
        expected.mat.zip(actual.mat).all { (a, b) -> a.isCloseEnoughTo(b, epsilon) },
        message = "expected=$expected, actual=$actual, eps=$epsilon"
    )
}

internal fun assertCloseEnough(expected: Color4f, actual: Color4f, epsilon: Float = EPSILON) {
    assertTrue(expected.isCloseEnoughTo(actual, epsilon), message = "expected=$expected, actual=$actual, eps=$epsilon")
}

internal fun assertCloseEnough(expected: Rect, actual: Rect, epsilon: Float = EPSILON) {
    assertTrue(expected.isCloseEnoughTo(actual, epsilon), message = "expected=$expected, actual=$actual, eps=$epsilon")
}

internal fun assertCloseEnough(expected: LineMetrics, actual: LineMetrics, epsilon: Float = EPSILON) {
    assertTrue(expected.isCloseEnoughTo(actual, epsilon), message = "expected=$expected, actual=$actual, eps=$epsilon")
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
            fail("results differ at index $count, expected $a, got $b")
        }
        count++
    }

    if (expected.hasNext()) {
        fail("expected $expected has more items than actual $actual (which has $count)")
    }

    if (actual.hasNext()) {
        fail("actual $actual has more items than actual $expected (which has $count)")
    }
}

internal fun assertContentCloseEnough(expected: FloatArray, actual: FloatArray, epsilon: Float = EPSILON) {
    assertContentEquivalent(expected.iterator(), actual.iterator()) { a, b -> a.isCloseEnoughTo(b, epsilon) }
}

internal fun assertContentCloseEnough(expected: Array<Point>, actual: Array<Point>, epsilon: Float = EPSILON) {
    assertContentEquivalent(expected.iterator(), actual.iterator()) { a, b -> a.isCloseEnoughTo(b, epsilon) }
}

internal fun assertContentCloseEnough(expected: List<Point>, actual: List<Point>, epsilon: Float = EPSILON) {
    assertContentEquivalent(expected.iterator(), actual.iterator()) { a, b -> a.isCloseEnoughTo(b, epsilon) }
}

internal fun assertContentCloseEnough(expected: Array<TextBox>, actual: Array<TextBox>, epsilon: Float = EPSILON) {
    assertContentEquivalent(expected.iterator(), actual.iterator()) { a, b -> a.isCloseEnoughTo(b, epsilon) }
}

internal fun assertContentCloseEnough(expected: Array<Shadow>, actual: Array<Shadow>, epsilon: Float = EPSILON) {
    assertContentEquivalent(expected.iterator(), actual.iterator()) { a, b -> a.isCloseEnoughTo(b, epsilon) }
}
