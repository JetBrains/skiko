package org.jetbrains.skia

import org.jetbrains.skia.impl.use
import org.jetbrains.skia.tests.assertContentCloseEnough
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.jetbrains.skiko.tests.runTest
import kotlin.test.assertContentEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull


class PathTests {
    @Test
    fun iterTest() = runTest {
        Path().moveTo(10f, 10f).lineTo(20f, 0f).lineTo(20f, 20f).closePath().use { p ->
            p.iterator().use { i ->
                assertTrue(i.hasNext())

                var s = i.next()!!
                assertEquals(PathVerb.MOVE, s.verb)
                assertEquals(Point(10f, 10f), s.p0)
                assertTrue(s.isClosedContour, "segment[0] is a closed contour")
                assertTrue(i.hasNext())

                s = i.next()!!
                assertEquals(PathVerb.LINE, s.verb)
                assertEquals(Point(10f, 10f), s.p0)
                assertEquals(Point(20f, 0f), s.p1)
                assertFalse(s.isCloseLine, "segment[1] is a closed line")
                assertTrue(i.hasNext())

                s = i.next()!!
                assertEquals(PathVerb.LINE, s.verb)
                assertEquals(Point(20f, 0f), s.p0)
                assertEquals(Point(20f, 20f), s.p1)
                assertFalse(s.isCloseLine, "segment[2] is a closed line")
                assertTrue(i.hasNext())

                s = i.next()!!
                assertEquals(PathVerb.LINE, s.verb)
                assertEquals(Point(20f, 20f), s.p0)
                assertEquals(Point(10f, 10f), s.p1)
                assertTrue(s.isCloseLine, "segment[3] is a closed line")
                assertTrue(i.hasNext())

                s = i.next()!!
                assertEquals(PathVerb.CLOSE, s.verb)
                assertEquals(Point(10f, 10f), s.p0)
                assertFalse(i.hasNext())
                assertFailsWith<NoSuchElementException> {
                    i.next()
                }
            }
        }
    }

    @Test
    fun convexityTest() = runTest {
        Path().lineTo(40f, 20f).lineTo(0f, 40f).lineTo(0f, 0f).closePath().use { p -> assertTrue(p.isConvex) }
        Path().lineTo(40f, 40f).lineTo(40f, 0f).lineTo(0f, 40f).lineTo(0f, 0f).closePath().use { p ->
            assertFalse(p.isConvex)
        }
    }


    @Test
    fun isShapeTest() {
        for (dir in PathDirection.entries) {
            for (start in 0..3) {
                Path().addRect(Rect.makeLTRB(0f, 0f, 40f, 20f), dir, start).use { p ->
                    assertEquals(Rect.makeLTRB(0f, 0f, 40f, 20f), p.isRect)
                    assertNull(p.isOval)
                    assertNull(p.isRRect)
                }
            }
        }
        for (dir in PathDirection.entries) {
            for (start in 0..3) {
                Path().addOval(Rect.makeLTRB(0f, 0f, 40f, 20f), dir, start).use { p ->
                    assertNull(p.isRect)
                    assertEquals(Rect.makeLTRB(0f, 0f, 40f, 20f), p.isOval)
                    assertNull(p.isRRect)
                }
            }
        }
        for (dir in PathDirection.entries) {
            Path().addCircle(20f, 20f, 20f, dir).use { p ->
                assertNull(p.isRect)
                assertEquals(Rect.makeLTRB(0f, 0f, 40f, 40f), p.isOval)
                assertNull(p.isRRect)
            }
        }
        for (dir in PathDirection.entries) {
            for (start in 0..7) {
                Path().addRRect(RRect.makeLTRB(0f, 0f, 40f, 20f, 5f), dir, start).use { p ->
                    assertNull(p.isRect)
                    assertNull(p.isOval)
                    assertEquals(RRect.makeLTRB(0f, 0f, 40f, 20f, 5f), p.isRRect)
                }
                Path().addRRect(RRect.makeLTRB(0f, 0f, 40f, 20f, 0f), dir, start).use { p ->
                    assertEquals(Rect.makeLTRB(0f, 0f, 40f, 20f), p.isRect)
                    assertNull(p.isOval)
                    assertNull(p.isRRect)
                }
                Path().addRRect(RRect.makeLTRB(0f, 0f, 40f, 20f, 20f, 10f), dir, start).use { p ->
                    assertNull(p.isRect)
                    assertEquals(Rect.makeLTRB(0f, 0f, 40f, 20f), p.isOval)
                    assertNull(p.isRRect)
                }
            }
        }
        Path().lineTo(40f, 40f).lineTo(40f, 0f).lineTo(0f, 40f).lineTo(0f, 0f).closePath().use { p ->
            assertNull(p.isRect)
            assertNull(p.isOval)
            assertNull(p.isRRect)
        }
    }

    @Test
    fun checksTest() {
        Path().lineTo(40f, 40f).lineTo(40f, 0f).lineTo(0f, 40f).lineTo(0f, 0f).closePath().use { p ->
            assertFalse(p.isEmpty)
            p.reset()
            assertTrue(p.isEmpty)
        }
        Path().lineTo(40f, 40f).lineTo(40f, 0f).lineTo(0f, 40f).lineTo(0f, 0f).closePath().use { p ->
            assertFalse(p.isEmpty)
            p.rewind()
            assertTrue(p.isEmpty)
        }
        Path().lineTo(40f, 40f).lineTo(40f, 0f).lineTo(0f, 40f).lineTo(0f, 0f).use { p ->
            assertFalse(p.isLastContourClosed)
            p.closePath()
            assertTrue(p.isLastContourClosed)
            p.moveTo(100f, 100f).lineTo(140f, 140f).lineTo(140f, 100f).lineTo(100f, 140f)
            assertFalse(p.isLastContourClosed)
            p.closePath()
            assertTrue(p.isLastContourClosed)
        }
        Path().lineTo(40f, 40f).lineTo(40f, 0f).lineTo(0f, 40f).lineTo(0f, 0f).use { p -> assertTrue(p.isFinite) }
        Path().lineTo(40f, 40f).lineTo(Float.POSITIVE_INFINITY, 0f).lineTo(0f, 40f).lineTo(0f, 0f).closePath().use { p ->
            assertEquals(
                false,
                p.isFinite
            )
        }
        Path().lineTo(40f, 40f).lineTo(40f, 0f).lineTo(0f, 40f).lineTo(0f, 0f).use { p ->
            assertFalse(p.isVolatile)
            p.setVolatile(true)
            assertTrue(p.isVolatile)
            p.setVolatile(false)
            assertFalse(p.isVolatile)
        }
        Path().lineTo(40f, 40f).lineTo(40f, 0f).lineTo(0f, 40f).lineTo(0f, 0f).closePath().use { p ->
            assertNull(p.asLine)
        }
        Path().moveTo(20f, 20f).lineTo(40f, 40f).use { p ->
            assertContentEquals(
                arrayOf(
                    Point(20f, 20f),
                    Point(40f, 40f)
                ), p.asLine
            )
        }
    }

    @Test
    fun swapTest() {
        Path().lineTo(40f, 40f).lineTo(40f, 0f).lineTo(0f, 40f).lineTo(0f, 0f).closePath().use { p1 ->
            Path().lineTo(0f, 0f).lineTo(20f, 20f).use { p2 ->
                p1.swap(p2)
                assertEquals(Path().lineTo(0f, 0f).lineTo(20f, 20f), p1)
                assertEquals(Path().lineTo(40f, 40f).lineTo(40f, 0f).lineTo(0f, 40f).lineTo(0f, 0f).closePath(), p2)
            }
        }
    }

    @Test
    fun containsTest() {
        Path().addRRect(RRect.makeLTRB(10f, 20f, 54f, 120f, 10f, 20f)).use { p ->
            assertTrue(p.conservativelyContainsRect(Rect.makeLTRB(10f, 40f, 54f, 80f)))
            assertTrue(p.conservativelyContainsRect(Rect.makeLTRB(25f, 20f, 39f, 120f)))
            assertTrue(p.conservativelyContainsRect(Rect.makeLTRB(15f, 25f, 49f, 115f)))
            assertTrue(p.conservativelyContainsRect(Rect.makeLTRB(13f, 27f, 51f, 113f)))
            assertFalse(p.conservativelyContainsRect(Rect.makeLTRB(0f, 40f, 60f, 80f)))
            assertTrue(p.contains(30f, 70f))
            assertFalse(p.contains(0f, 0f))
        }
    }

    @Test
    fun utilsTest() {
        assertFalse(Path.isLineDegenerate(Point(0f, 0f), Point(10f, 0f), false))
        assertTrue(Path.isLineDegenerate(Point(0f, 0f), Point(0f, 0f), true))
        assertTrue(Path.isLineDegenerate(Point(0f, 0f), Point(0f, 0f), false))
        assertFalse(Path.isLineDegenerate(Point(0f, 0f), Point(0f, 1e-13f), true))
        assertFalse(Path.isQuadDegenerate(Point(0f, 0f), Point(10f, 0f), Point(0f, 0f), false))
        assertTrue(Path.isQuadDegenerate(Point(0f, 0f), Point(0f, 0f), Point(0f, 0f), false))
        assertFalse(Path.isCubicDegenerate(Point(0f, 0f), Point(10f, 0f), Point(0f, 0f), Point(0f, 0f), false))
        assertTrue(Path.isCubicDegenerate(Point(0f, 0f), Point(0f, 0f), Point(0f, 0f), Point(0f, 0f), false))
        assertContentCloseEnough(
            arrayOf(Point(0f, 20f), Point(6.666667f, 13.333334f)),
            Path.convertConicToQuads(Point(0f, 20f), Point(20f, 0f), Point(40f, 20f), 0.5f, 1)
        )
        assertContentCloseEnough(
            arrayOf(
                Point(0f, 20f),
                Point(3.0940108f, 16.90599f),
                Point(8.4529950f, 15.119661f),
                Point(13.811979f, 13.333334f)
            ),
            Path.convertConicToQuads(Point(0f, 20f), Point(20f, 0f), Point(40f, 20f), 0.5f, 2)
        )
        Path().lineTo(40f, 40f).use { p ->
            val g1 = p.generationId
            p.lineTo(10f, 40f)
            val g2 = p.generationId
            assertNotEquals(g1, g2)
            p.fillMode = PathFillMode.EVEN_ODD
            val g3 = p.generationId
            assertEquals(g2, g3)
        }
    }

    @Test
    fun serializeTest() {
        Path().lineTo(40f, 40f).lineTo(40f, 0f).lineTo(0f, 40f).lineTo(0f, 0f).closePath().use { p ->
            val p2: Path = Path.makeFromBytes(p.serializeToBytes())
            assertEquals(p, p2)
        }
    }

    @Test
    fun svgTest() {
        Path.makeFromSVGString("M0 0 L10 10 Z").use {  }
    }
}