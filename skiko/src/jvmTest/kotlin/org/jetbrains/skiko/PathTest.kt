package org.jetbrains.skiko

import org.jetbrains.skia.Path
import org.jetbrains.skia.PathDirection
import org.jetbrains.skia.PathFillMode
import org.jetbrains.skia.PathSegmentMask
import org.jetbrains.skia.PathVerb
import org.jetbrains.skia.Point
import org.jetbrains.skia.RRect
import org.jetbrains.skia.Rect
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PathTest {

    @Test
    fun isShapeTest() {
            for (dir in PathDirection.values()) {
                for (start in 0..3) {
                    Path().addRect(Rect.makeLTRB(0f, 0f, 40f, 20f), dir, start).use { p ->
                        assertEquals(Rect.makeLTRB(0f, 0f, 40f, 20f), p.isRect)
                        assertNull(p.isOval)
                        assertNull(p.isRRect)
                    }
                }
            }
            for (dir in PathDirection.values()) {
                for (start in 0..3) {
                    Path().addOval(Rect.makeLTRB(0f, 0f, 40f, 20f), dir, start).use { p ->
                        assertNull(p.isRect)
                        assertEquals(Rect.makeLTRB(0f, 0f, 40f, 20f), p.isOval)
                        assertNull(p.isRRect)
                    }
                }
            }
            for (dir in PathDirection.values()) {
                Path().addCircle(20f, 20f, 20f, dir).use { p ->
                    assertNull(p.isRect)
                    assertEquals(Rect.makeLTRB(0f, 0f, 40f, 40f), p.isOval)
                    assertNull(p.isRRect)
                }
            }
            for (dir in PathDirection.values()) {
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
    fun storageTest() {
        val subpath: Path = Path().lineTo(40f, 40f).lineTo(40f, 0f).lineTo(0f, 40f).lineTo(0f, 0f).closePath()
        for (p in arrayOf(
            Path().addPath(subpath),
            Path().incReserve(10).addPath(subpath).closePath()
        )) {
            val p0 = Point(0f, 0f)
            val p1 = Point(40f, 40f)
            val p2 = Point(40f, 0f)
            val p3 = Point(0f, 40f)
            val p4 = Point(0f, 0f)
            val p5 = Point(10f, 10f)
            assertEquals(5, p.pointsCount)
            assertEquals(p0, p.getPoint(0))
            assertEquals(p1, p.getPoint(1))
            assertEquals(p2, p.getPoint(2))
            assertEquals(p3, p.getPoint(3))
            assertEquals(p4, p.getPoint(4))
            assertEquals(p4, p.lastPt)
            p.lastPt = p5
            assertEquals(p5, p.getPoint(4))
            assertEquals(p5, p.lastPt)
            assertEquals(5, p.getPoints(null, 0))
            var pts = arrayOfNulls<Point?>(5)
            p.getPoints(pts, 5)
            assertContentEquals(arrayOf(p0, p1, p2, p3, p5), pts)
            pts = arrayOfNulls(3)
            p.getPoints(pts, 3)
            assertContentEquals(arrayOf(p0, p1, p2), pts)
            pts = arrayOfNulls(5)
            p.getPoints(pts, 3)
            assertContentEquals(arrayOf(p0, p1, p2, null, null), pts)
            pts = arrayOfNulls(10)
            p.getPoints(pts, 10)
            assertContentEquals(arrayOf(p0, p1, p2, p3, p5, null, null, null, null, null), pts)
            assertEquals(6, p.verbsCount)
            assertEquals(6, p.getVerbs(null, 0))
            var verbs = arrayOfNulls<PathVerb?>(6)
            p.getVerbs(verbs, 6)
            assertContentEquals(
                arrayOf(
                    PathVerb.MOVE,
                    PathVerb.LINE,
                    PathVerb.LINE,
                    PathVerb.LINE,
                    PathVerb.LINE,
                    PathVerb.CLOSE
                ), verbs
            )
            verbs = arrayOfNulls(3)
            p.getVerbs(verbs, 3)
            assertContentEquals(arrayOf(PathVerb.MOVE, PathVerb.LINE, PathVerb.LINE), verbs)
            verbs = arrayOfNulls(6)
            p.getVerbs(verbs, 3)
            assertContentEquals(arrayOf(PathVerb.MOVE, PathVerb.LINE, PathVerb.LINE, null, null, null), verbs)
            verbs = arrayOfNulls(10)
            p.getVerbs(verbs, 10)
            assertContentEquals(
                arrayOf(
                    PathVerb.MOVE,
                    PathVerb.LINE,
                    PathVerb.LINE,
                    PathVerb.LINE,
                    PathVerb.LINE,
                    PathVerb.CLOSE,
                    null,
                    null,
                    null,
                    null
                ), verbs
            )
            assertNotEquals(0L, p.approximateBytesUsed)
            assertEquals(PathSegmentMask.LINE, p.segmentMasks)
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
        assertContentEquals(
            arrayOf(Point(0f, 20f), Point(6.666667f, 13.333334f)),
            Path.convertConicToQuads(Point(0f, 20f), Point(20f, 0f), Point(40f, 20f), 0.5f, 1)
        )
        assertContentEquals(
            arrayOf(
                Point(0f, 20f),
                Point(3.0940108f, 16.905989f),
                Point(8.452994f, 15.119661f),
                Point(13.811978f, 13.333334f)
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
}