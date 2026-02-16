package org.jetbrains.skia

import org.jetbrains.skia.tests.assertCloseEnough
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PathTest {

    @Test
    fun storageTest() {
        val subpath: Path = PathBuilder()
            .lineTo(40f, 40f)
            .lineTo(40f, 0f)
            .lineTo(0f, 40f)
            .lineTo(0f, 0f)
            .closePath()
            .detach()
        for (p in arrayOf(
            PathBuilder().addPath(subpath).detach(),
            PathBuilder().incReserve(10).addPath(subpath).closePath().detach()
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
            // lastPt is now read-only, test setLastPt through PathBuilder
            val pModified = PathBuilder(p).setLastPt(p5.x, p5.y).detach()
            assertEquals(p5, pModified.getPoint(4))
            assertEquals(p5, pModified.lastPt)
            assertEquals(5, pModified.getPoints(null, 0))
            var pts = arrayOfNulls<Point?>(5)
            pModified.getPoints(pts, 5)
            assertContentEquals(arrayOf(p0, p1, p2, p3, p5), pts)
            pts = arrayOfNulls(3)
            pModified.getPoints(pts, 3)
            assertContentEquals(arrayOf(p0, p1, p2), pts)
            pts = arrayOfNulls(5)
            pModified.getPoints(pts, 3)
            assertContentEquals(arrayOf(p0, p1, p2, null, null), pts)
            pts = arrayOfNulls(10)
            pModified.getPoints(pts, 10)
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
            assertTrue(p.approximateBytesUsed > 0, "approximateBytesUsed should return positive size")
            assertEquals(PathSegmentMask.LINE, p.segmentMasks)
        }
    }

    @Test
    fun emptyPath() {
        val emptyPath = Path()

        assertEquals(0, emptyPath.pointsCount)
        assertNull(emptyPath.lastPt)
    }

    @Test
    fun canGetBounds() {
        val path = PathBuilder().lineTo(40f, 40f).lineTo(40f, 0f).lineTo(0f, 40f).lineTo(0f, 0f).closePath().detach()
        val bounds = Rect(0.0f, 0.0f, 40.0f, 40.0f)
        assertCloseEnough(bounds, path.bounds)
        assertCloseEnough(bounds, path.computeTightBounds())
    }

    @Test
    fun rawTest() {
        val pts = arrayOf(Point(0f, 0f), Point(10f, 10f), Point(20f, 0f))
        val verbs = arrayOf(PathVerb.MOVE, PathVerb.LINE, PathVerb.LINE)
        val path = Path.Raw(pts, verbs)
        
        assertEquals(3, path.pointsCount)
        assertEquals(3, path.verbsCount)
        assertCloseEnough(Rect(0f, 0f, 20f, 10f), path.bounds)
    }

    @Test
    fun rectTest() {
        val rect = Rect(10f, 20f, 30f, 40f)
        val path = Path.Rect(rect)
        
        assertEquals(4, path.pointsCount) // 4 corners
        assertEquals(rect, path.isRect)
        assertCloseEnough(rect, path.bounds)
    }

    @Test
    fun ovalTest() {
        val rect = Rect(10f, 20f, 30f, 40f)
        val path = Path.Oval(rect)
        
        assertEquals(rect, path.isOval)
        assertCloseEnough(rect, path.bounds)
    }

    @Test
    fun circleTest() {
        val centerX = 50f
        val centerY = 50f
        val radius = 25f
        val path = Path.Circle(centerX, centerY, radius)
        
        val expectedBounds = Rect(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
        assertCloseEnough(expectedBounds, path.bounds)
        assertEquals(expectedBounds, path.isOval)
    }

    @Test
    fun rrectTest() {
        val rect = Rect(10f, 20f, 110f, 120f)
        val rx = 10f
        val ry = 10f
        val path = Path.RRect(rect, rx, ry)
        
        assertCloseEnough(rect, path.bounds)
        val rrect = path.isRRect
        assertEquals(rect, rrect?.let { Rect(it.left, it.top, it.right, it.bottom) })
    }

    @Test
    fun polygonTest() {
        val pts = arrayOf(Point(0f, 0f), Point(10f, 0f), Point(10f, 10f), Point(0f, 10f))
        val pathClosed = Path.Polygon(pts, true)
        val pathOpen = Path.Polygon(pts, false)
        
        assertEquals(4, pathClosed.pointsCount)
        assertEquals(4, pathOpen.pointsCount)
        assertEquals(true, pathClosed.isLastContourClosed)
        assertEquals(false, pathOpen.isLastContourClosed)
    }

    @Test
    fun lineTest() {
        val p0 = Point(10f, 20f)
        val p1 = Point(30f, 40f)
        val path = Path.Line(p0, p1)
        
        assertEquals(2, path.pointsCount)
        val line = path.asLine
        assertEquals(2, line?.size)
        assertEquals(p0, line?.get(0))
        assertEquals(p1, line?.get(1))
    }
}