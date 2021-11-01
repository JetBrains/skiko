package org.jetbrains.skia

import org.jetbrains.skia.impl.Native.Companion.NullPointer
import org.jetbrains.skia.tests.assertCloseEnough
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class PathTest {

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
            assertNotEquals(NullPointer, p.approximateBytesUsed)
            assertEquals(PathSegmentMask.LINE, p.segmentMasks)
        }
    }

    @Test
    fun canGetBounds() {
        val path = Path().lineTo(40f, 40f).lineTo(40f, 0f).lineTo(0f, 40f).lineTo(0f, 0f).closePath()
        val bounds = Rect(0.0f, 0.0f, 40.0f, 40.0f)
        assertCloseEnough(bounds, path.bounds)
        assertCloseEnough(bounds, path.computeTightBounds())
    }
}