package org.jetbrains.skia

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PathBuilderTest {

    @Test
    fun testConstructors() {
        // Default constructor
        val builder1 = PathBuilder()
        assertNotNull(builder1)
        
        // Constructor with fillType
        val builder2 = PathBuilder(PathFillMode.EVEN_ODD)
        val path2 = builder2.detach()
        assertEquals(PathFillMode.EVEN_ODD, path2.fillMode)
        
        // Constructor from existing path
        val existingPath = PathBuilder().moveTo(10f, 10f).lineTo(20f, 20f).detach()
        val builder3 = PathBuilder(existingPath)
        assertNotNull(builder3)
    }

    @Test
    fun testSnapshotAndDetach() {
        val builder = PathBuilder().moveTo(0f, 0f).lineTo(10f, 10f)
        
        // snapshot() creates a copy without consuming builder
        val snapshot1 = builder.snapshot()
        assertNotNull(snapshot1)
        assertEquals(2, snapshot1.pointsCount)
        
        // Builder is still usable after snapshot
        val snapshot2 = builder.lineTo(20f, 20f).snapshot()
        assertEquals(3, snapshot2.pointsCount)
        
        // detach() returns path and consumes builder
        val detached = builder.detach()
        assertNotNull(detached)
        assertEquals(3, detached.pointsCount)
    }

    @Test
    fun testSetFillType() {
        val path = PathBuilder()
            .setFillType(PathFillMode.INVERSE_WINDING)
            .moveTo(0f, 0f)
            .detach()
        assertEquals(PathFillMode.INVERSE_WINDING, path.fillMode)
    }

    @Test
    fun testReset() {
        val builder = PathBuilder()
            .moveTo(10f, 10f)
            .lineTo(20f, 20f)
            .reset()
            .moveTo(5f, 5f)
        val path = builder.detach()
        assertEquals(1, path.pointsCount)
        assertEquals(Point(5f, 5f), path.getPoint(0))
    }

    @Test
    fun testIncReserve() {
        // Test that incReserve doesn't crash (performance optimization)
        val path = PathBuilder()
            .incReserve(100)
            .moveTo(0f, 0f)
            .lineTo(10f, 10f)
            .detach()
        assertEquals(2, path.pointsCount)
    }

    @Test
    fun testMoveToAndLineTo() {
        // Test moveTo(x, y)
        val path1 = PathBuilder()
            .moveTo(10f, 20f)
            .lineTo(30f, 40f)
            .detach()
        assertEquals(2, path1.pointsCount)
        assertEquals(Point(10f, 20f), path1.getPoint(0))
        assertEquals(Point(30f, 40f), path1.getPoint(1))
        
        // Test moveTo(Point) and lineTo(Point)
        val path2 = PathBuilder()
            .moveTo(Point(5f, 10f))
            .lineTo(Point(15f, 20f))
            .detach()
        assertEquals(2, path2.pointsCount)
        assertEquals(Point(5f, 10f), path2.getPoint(0))
    }

    @Test
    fun testRelativeMoveToAndLineTo() {
        val path = PathBuilder()
            .moveTo(10f, 10f)
            .rMoveTo(5f, 5f)  // Should be at (15, 15)
            .rLineTo(10f, 10f)  // Should be at (25, 25)
            .detach()
        assertEquals(2, path.pointsCount)
        assertEquals(Point(15f, 15f), path.getPoint(0))
        assertEquals(Point(25f, 25f), path.getPoint(1))
    }

    @Test
    fun testQuadTo() {
        // Test quadTo(x1, y1, x2, y2)
        val path1 = PathBuilder()
            .moveTo(0f, 0f)
            .quadTo(10f, 20f, 20f, 0f)
            .detach()
        assertEquals(3, path1.pointsCount)
        
        // Test quadTo(Point, Point)
        val path2 = PathBuilder()
            .moveTo(0f, 0f)
            .quadTo(Point(10f, 20f), Point(20f, 0f))
            .detach()
        assertEquals(3, path2.pointsCount)
        
        // Test rQuadTo
        val path3 = PathBuilder()
            .moveTo(0f, 0f)
            .rQuadTo(10f, 20f, 20f, 0f)
            .detach()
        assertEquals(3, path3.pointsCount)
    }

    @Test
    fun testConicTo() {
        // Test conicTo(x1, y1, x2, y2, w)
        val path1 = PathBuilder()
            .moveTo(0f, 0f)
            .conicTo(10f, 20f, 20f, 0f, 0.5f)
            .detach()
        assertEquals(3, path1.pointsCount)
        
        // Test conicTo(Point, Point, w)
        val path2 = PathBuilder()
            .moveTo(0f, 0f)
            .conicTo(Point(10f, 20f), Point(20f, 0f), 0.5f)
            .detach()
        assertEquals(3, path2.pointsCount)
        
        // Test rConicTo
        val path3 = PathBuilder()
            .moveTo(0f, 0f)
            .rConicTo(10f, 20f, 20f, 0f, 0.5f)
            .detach()
        assertEquals(3, path3.pointsCount)
    }

    @Test
    fun testCubicTo() {
        // Test cubicTo(x1, y1, x2, y2, x3, y3)
        val path1 = PathBuilder()
            .moveTo(0f, 0f)
            .cubicTo(10f, 20f, 15f, 30f, 20f, 0f)
            .detach()
        assertEquals(4, path1.pointsCount)
        
        // Test cubicTo(Point, Point, Point)
        val path2 = PathBuilder()
            .moveTo(0f, 0f)
            .cubicTo(Point(10f, 20f), Point(15f, 30f), Point(20f, 0f))
            .detach()
        assertEquals(4, path2.pointsCount)
        
        // Test rCubicTo
        val path3 = PathBuilder()
            .moveTo(0f, 0f)
            .rCubicTo(10f, 20f, 15f, 30f, 20f, 0f)
            .detach()
        assertEquals(4, path3.pointsCount)
    }

    @Test
    fun testArcTo() {
        // Test arcTo with Rect
        val path1 = PathBuilder()
            .moveTo(0f, 0f)
            .arcTo(Rect.makeXYWH(10f, 10f, 20f, 20f), 0f, 90f, false)
            .detach()
        assertTrue(path1.pointsCount > 0)
        
        // Test arcTo with coordinates
        val path2 = PathBuilder()
            .moveTo(0f, 0f)
            .arcTo(10f, 10f, 30f, 30f, 0f, 90f, false)
            .detach()
        assertTrue(path2.pointsCount > 0)
    }

    @Test
    fun testTangentArcTo() {
        // Test tangentArcTo with coordinates
        val path1 = PathBuilder()
            .moveTo(0f, 0f)
            .tangentArcTo(10f, 0f, 10f, 10f, 5f)
            .detach()
        assertTrue(path1.pointsCount > 0)
        
        // Test tangentArcTo with Points
        val path2 = PathBuilder()
            .moveTo(0f, 0f)
            .tangentArcTo(Point(10f, 0f), Point(10f, 10f), 5f)
            .detach()
        assertTrue(path2.pointsCount > 0)
    }

    @Test
    fun testEllipticalArcTo() {
        // Test ellipticalArcTo
        val path1 = PathBuilder()
            .moveTo(0f, 0f)
            .ellipticalArcTo(10f, 5f, 0f, PathEllipseArc.SMALLER, PathDirection.CLOCKWISE, 20f, 0f)
            .detach()
        assertTrue(path1.pointsCount > 0)
        
        // Test ellipticalArcTo with Points
        val path2 = PathBuilder()
            .moveTo(0f, 0f)
            .ellipticalArcTo(Point(10f, 5f), 0f, PathEllipseArc.SMALLER, PathDirection.CLOCKWISE, Point(20f, 0f))
            .detach()
        assertTrue(path2.pointsCount > 0)
        
        // Test rEllipticalArcTo
        val path3 = PathBuilder()
            .moveTo(0f, 0f)
            .rEllipticalArcTo(10f, 5f, 0f, PathEllipseArc.SMALLER, PathDirection.CLOCKWISE, 20f, 0f)
            .detach()
        assertTrue(path3.pointsCount > 0)
    }

    @Test
    fun testClosePath() {
        val path = PathBuilder()
            .moveTo(0f, 0f)
            .lineTo(10f, 10f)
            .lineTo(10f, 0f)
            .closePath()
            .detach()
        
        assertTrue(path.isLastContourClosed)
        assertEquals(3, path.pointsCount)
    }

    @Test
    fun testAddRect() {
        // Test addRect with Rect
        val path1 = PathBuilder()
            .addRect(Rect.makeXYWH(10f, 10f, 20f, 20f))
            .detach()
        assertTrue(path1.pointsCount >= 4)
        
        // Test addRect with coordinates
        val path2 = PathBuilder()
            .addRect(10f, 10f, 30f, 30f, PathDirection.COUNTER_CLOCKWISE, 1)
            .detach()
        assertTrue(path2.pointsCount >= 4)
    }

    @Test
    fun testAddOval() {
        // Test addOval with Rect
        val path1 = PathBuilder()
            .addOval(Rect.makeXYWH(10f, 10f, 20f, 20f))
            .detach()
        assertTrue(path1.pointsCount > 4)
        
        // Test addOval with coordinates
        val path2 = PathBuilder()
            .addOval(10f, 10f, 30f, 30f, PathDirection.CLOCKWISE, 2)
            .detach()
        assertTrue(path2.pointsCount > 4)
    }

    @Test
    fun testAddCircle() {
        val path = PathBuilder()
            .addCircle(50f, 50f, 25f, PathDirection.CLOCKWISE)
            .detach()
        assertTrue(path.pointsCount > 4)
    }

    @Test
    fun testAddArc() {
        // Test addArc with Rect
        val path1 = PathBuilder()
            .addArc(Rect.makeXYWH(10f, 10f, 20f, 20f), 0f, 90f)
            .detach()
        assertTrue(path1.pointsCount > 0)
        
        // Test addArc with coordinates
        val path2 = PathBuilder()
            .addArc(10f, 10f, 30f, 30f, 0f, 180f)
            .detach()
        assertTrue(path2.pointsCount > 0)
    }

    @Test
    fun testAddRRect() {
        // Test addRRect with RRect
        val rrect = RRect.makeXYWH(10f, 10f, 20f, 20f, 5f)
        val path1 = PathBuilder()
            .addRRect(rrect)
            .detach()
        assertTrue(path1.pointsCount > 4)
        
        // Test addRRect with coordinates and radii
        val radii = floatArrayOf(5f, 5f, 5f, 5f, 5f, 5f, 5f, 5f)
        val path2 = PathBuilder()
            .addRRect(10f, 10f, 30f, 30f, radii, PathDirection.CLOCKWISE)
            .detach()
        assertTrue(path2.pointsCount > 4)
    }

    @Test
    fun testAddPoly() {
        // Test addPoly with Array<Point>
        val points = arrayOf(Point(0f, 0f), Point(10f, 10f), Point(20f, 0f))
        val path1 = PathBuilder()
            .addPoly(points, true)
            .detach()
        assertEquals(3, path1.pointsCount)
        
        // Test addPoly with FloatArray
        val coords = floatArrayOf(0f, 0f, 10f, 10f, 20f, 0f)
        val path2 = PathBuilder()
            .addPoly(coords, false)
            .detach()
        assertEquals(3, path2.pointsCount)
    }

    @Test
    fun testAddPath() {
        val srcPath = PathBuilder()
            .moveTo(0f, 0f)
            .lineTo(10f, 10f)
            .detach()
        
        // Test addPath with mode=APPEND
        // Note: moveTo alone doesn't add a point until a drawing command follows
        val path1 = PathBuilder()
            .moveTo(20f, 20f)
            .addPath(srcPath, PathAddMode.APPEND)
            .detach()
        assertEquals(2, path1.pointsCount)
        
        // Test addPath with offset
        val path2 = PathBuilder()
            .addPath(srcPath, 5f, 5f, PathAddMode.APPEND)
            .detach()
        assertEquals(Point(5f, 5f), path2.getPoint(0))
        
        // Test addPath with matrix
        val matrix = Matrix33.makeTranslate(10f, 10f)
        val path3 = PathBuilder()
            .addPath(srcPath, matrix, PathAddMode.APPEND)
            .detach()
        assertEquals(Point(10f, 10f), path3.getPoint(0))
    }

    @Test
    fun testSetLastPt() {
        val path = PathBuilder()
            .moveTo(0f, 0f)
            .lineTo(10f, 10f)
            .setLastPt(15f, 15f)
            .detach()
        
        assertEquals(Point(15f, 15f), path.lastPt)
    }

    @Test
    fun testBuilderChaining() {
        // Verify that all methods return PathBuilder for chaining
        val path = PathBuilder()
            .setFillType(PathFillMode.WINDING)
            .incReserve(10)
            .moveTo(0f, 0f)
            .lineTo(10f, 0f)
            .lineTo(10f, 10f)
            .lineTo(0f, 10f)
            .closePath()
            .detach()
        
        assertEquals(4, path.pointsCount)
        assertTrue(path.isLastContourClosed)
    }

    @Test
    fun testComplexPath() {
        // Test a complex path that uses multiple operations
        val path = PathBuilder()
            .moveTo(0f, 0f)
            .lineTo(50f, 0f)
            .quadTo(75f, 25f, 50f, 50f)
            .cubicTo(40f, 60f, 30f, 60f, 20f, 50f)
            .arcTo(0f, 25f, 25f, 50f, 180f, 90f, false)
            .closePath()
            .addCircle(100f, 100f, 20f)
            .addRect(150f, 150f, 200f, 200f)
            .detach()
        
        assertTrue(path.pointsCount > 10)
        assertNotNull(path.bounds)
    }
}
