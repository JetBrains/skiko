package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad

/**
 * PathBuilder is used to create immutable Path objects.
 * 
 * PathBuilder follows the builder pattern - all mutation methods return the builder instance
 * for method chaining. Once path construction is complete, call [detach] to get an immutable
 * Path object, or [snapshot] to get a copy while keeping the builder usable.
 * 
 * This class contains all path construction methods that were previously mutation methods
 * on the Path class. Path objects created from PathBuilder are immutable.
 */
class PathBuilder internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        init {
            staticLoad()
        }
    }

    private object _FinalizerHolder {
        val PTR = PathBuilder_nGetFinalizer()
    }

    /**
     * Constructs an empty PathBuilder with WINDING fill type.
     */
    constructor() : this(PathBuilder_nMake())

    /**
     * Constructs a PathBuilder with the specified fill type.
     * 
     * @param fillType the fill type for paths created from this builder
     */
    constructor(fillType: PathFillMode) : this(PathBuilder_nMakeWithFillType(fillType.ordinal))

    /**
     * Constructs a PathBuilder by copying an existing Path.
     * 
     * @param path the path to copy into this builder
     */
    constructor(path: Path) : this(
        PathBuilder_nMakeFromPath(getPtr(path)).also {
            reachabilityBarrier(path)
        }
    )

    /**
     * Returns an immutable Path representing the current state of this builder.
     * The builder remains valid and can continue to be used.
     * 
     * @return a new immutable Path
     */
    fun snapshot(): Path {
        return try {
            Stats.onNativeCall()
            Path(PathBuilder_nSnapshot(_ptr))
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Returns an immutable Path representing the current state of this builder.
     * The builder is reset to empty after this call.
     * 
     * @return a new immutable Path
     */
    fun detach(): Path {
        return try {
            Stats.onNativeCall()
            Path(PathBuilder_nDetach(_ptr))
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Sets the fill type for paths created from this builder.
     * 
     * @param fillType the fill type to set
     * @return this builder for chaining
     */
    fun setFillType(fillType: PathFillMode): PathBuilder {
        return try {
            Stats.onNativeCall()
            PathBuilder_nSetFillType(_ptr, fillType.ordinal)
            this
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Resets the builder to empty, removing all verbs, points, and weights.
     * Fill type is reset to WINDING.
     * 
     * @return this builder for chaining
     */
    fun reset(): PathBuilder {
        return try {
            Stats.onNativeCall()
            PathBuilder_nReset(_ptr)
            this
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Hints to reserve additional space for approximately [extraPtCount] points and verbs.
     * May improve performance by reducing allocations.
     * 
     * @param extraPtCount number of additional points to reserve
     * @return this builder for chaining
     */
    fun incReserve(extraPtCount: Int): PathBuilder {
        return try {
            Stats.onNativeCall()
            PathBuilder_nIncReserve(_ptr, extraPtCount)
            this
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Adds beginning of contour at point (x, y).
     * 
     * @param x x-axis value of contour start
     * @param y y-axis value of contour start
     * @return this builder for chaining
     */
    fun moveTo(x: Float, y: Float): PathBuilder {
        return try {
            Stats.onNativeCall()
            PathBuilder_nMoveTo(_ptr, x, y)
            this
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Adds beginning of contour at point p.
     * 
     * @param p contour start
     * @return this builder for chaining
     */
    fun moveTo(p: Point): PathBuilder {
        return moveTo(p.x, p.y)
    }

    /**
     * Adds beginning of contour relative to last point.
     * If PathBuilder is empty, starts contour at (dx, dy).
     * Otherwise, start contour at last point offset by (dx, dy).
     * 
     * @param dx offset from last point to contour start on x-axis
     * @param dy offset from last point to contour start on y-axis
     * @return this builder for chaining
     */
    fun rMoveTo(dx: Float, dy: Float): PathBuilder {
        return try {
            Stats.onNativeCall()
            PathBuilder_nRMoveTo(_ptr, dx, dy)
            this
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Adds line from last point to (x, y). If PathBuilder is empty, or last verb is
     * CLOSE, last point is set to (0, 0) before adding line.
     * 
     * @param x end of added line on x-axis
     * @param y end of added line on y-axis
     * @return this builder for chaining
     */
    fun lineTo(x: Float, y: Float): PathBuilder {
        return try {
            Stats.onNativeCall()
            PathBuilder_nLineTo(_ptr, x, y)
            this
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Adds line from last point to point p.
     * 
     * @param p end point of added line
     * @return this builder for chaining
     */
    fun lineTo(p: Point): PathBuilder {
        return lineTo(p.x, p.y)
    }

    /**
     * Adds line from last point to vector (dx, dy). If PathBuilder is empty, or last verb is
     * CLOSE, last point is set to (0, 0) before adding line.
     * Line end is last point plus vector (dx, dy).
     * 
     * @param dx offset from last point to line end on x-axis
     * @param dy offset from last point to line end on y-axis
     * @return this builder for chaining
     */
    fun rLineTo(dx: Float, dy: Float): PathBuilder {
        return try {
            Stats.onNativeCall()
            PathBuilder_nRLineTo(_ptr, dx, dy)
            this
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Adds quad from last point towards (x1, y1), to (x2, y2).
     * 
     * @param x1 control point of quad on x-axis
     * @param y1 control point of quad on y-axis
     * @param x2 end point of quad on x-axis
     * @param y2 end point of quad on y-axis
     * @return this builder for chaining
     */
    fun quadTo(x1: Float, y1: Float, x2: Float, y2: Float): PathBuilder {
        return try {
            Stats.onNativeCall()
            PathBuilder_nQuadTo(_ptr, x1, y1, x2, y2)
            this
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Adds quad from last point towards point p1, to point p2.
     * 
     * @param p1 control point of added quad
     * @param p2 end point of added quad
     * @return this builder for chaining
     */
    fun quadTo(p1: Point, p2: Point): PathBuilder {
        return quadTo(p1.x, p1.y, p2.x, p2.y)
    }

    /**
     * Adds quad from last point towards vector (dx1, dy1), to vector (dx2, dy2).
     * Quad control is last point plus vector (dx1, dy1).
     * Quad end is last point plus vector (dx2, dy2).
     * 
     * @param dx1 offset from last point to quad control on x-axis
     * @param dy1 offset from last point to quad control on y-axis
     * @param dx2 offset from last point to quad end on x-axis
     * @param dy2 offset from last point to quad end on y-axis
     * @return this builder for chaining
     */
    fun rQuadTo(dx1: Float, dy1: Float, dx2: Float, dy2: Float): PathBuilder {
        return try {
            Stats.onNativeCall()
            PathBuilder_nRQuadTo(_ptr, dx1, dy1, dx2, dy2)
            this
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Adds conic from last point towards (x1, y1), to (x2, y2), weighted by w.
     * 
     * @param x1 control point of conic on x-axis
     * @param y1 control point of conic on y-axis
     * @param x2 end point of conic on x-axis
     * @param y2 end point of conic on y-axis
     * @param w weight of added conic
     * @return this builder for chaining
     */
    fun conicTo(x1: Float, y1: Float, x2: Float, y2: Float, w: Float): PathBuilder {
        return try {
            Stats.onNativeCall()
            PathBuilder_nConicTo(_ptr, x1, y1, x2, y2, w)
            this
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Adds conic from last point towards point p1, to point p2, weighted by w.
     * 
     * @param p1 control point of added conic
     * @param p2 end point of added conic
     * @param w weight of added conic
     * @return this builder for chaining
     */
    fun conicTo(p1: Point, p2: Point, w: Float): PathBuilder {
        return conicTo(p1.x, p1.y, p2.x, p2.y, w)
    }

    /**
     * Adds conic from last point towards vector (dx1, dy1), to vector (dx2, dy2), weighted by w.
     * Control is last point plus vector (dx1, dy1).
     * End is last point plus vector (dx2, dy2).
     * 
     * @param dx1 offset from last point to conic control on x-axis
     * @param dy1 offset from last point to conic control on y-axis
     * @param dx2 offset from last point to conic end on x-axis
     * @param dy2 offset from last point to conic end on y-axis
     * @param w weight of added conic
     * @return this builder for chaining
     */
    fun rConicTo(dx1: Float, dy1: Float, dx2: Float, dy2: Float, w: Float): PathBuilder {
        return try {
            Stats.onNativeCall()
            PathBuilder_nRConicTo(_ptr, dx1, dy1, dx2, dy2, w)
            this
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Adds cubic from last point towards (x1, y1), then towards (x2, y2), ending at (x3, y3).
     * 
     * @param x1 first control point of cubic on x-axis
     * @param y1 first control point of cubic on y-axis
     * @param x2 second control point of cubic on x-axis
     * @param y2 second control point of cubic on y-axis
     * @param x3 end point of cubic on x-axis
     * @param y3 end point of cubic on y-axis
     * @return this builder for chaining
     */
    fun cubicTo(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float): PathBuilder {
        return try {
            Stats.onNativeCall()
            PathBuilder_nCubicTo(_ptr, x1, y1, x2, y2, x3, y3)
            this
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Adds cubic from last point towards point p1, then towards point p2, ending at point p3.
     * 
     * @param p1 first control point of cubic
     * @param p2 second control point of cubic
     * @param p3 end point of cubic
     * @return this builder for chaining
     */
    fun cubicTo(p1: Point, p2: Point, p3: Point): PathBuilder {
        return cubicTo(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y)
    }

    /**
     * Adds cubic from last point towards vector (dx1, dy1), then towards (dx2, dy2), to (dx3, dy3).
     * 
     * @param dx1 offset from last point to first cubic control on x-axis
     * @param dy1 offset from last point to first cubic control on y-axis
     * @param dx2 offset from last point to second cubic control on x-axis
     * @param dy2 offset from last point to second cubic control on y-axis
     * @param dx3 offset from last point to cubic end on x-axis
     * @param dy3 offset from last point to cubic end on y-axis
     * @return this builder for chaining
     */
    fun rCubicTo(dx1: Float, dy1: Float, dx2: Float, dy2: Float, dx3: Float, dy3: Float): PathBuilder {
        return try {
            Stats.onNativeCall()
            PathBuilder_nRCubicTo(_ptr, dx1, dy1, dx2, dy2, dx3, dy3)
            this
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Appends arc to path. Arc is part of ellipse bounded by oval, from startAngle through sweepAngle.
     * 
     * @param oval bounds of ellipse containing arc
     * @param startAngle starting angle of arc in degrees
     * @param sweepAngle sweep, in degrees. Positive is clockwise
     * @param forceMoveTo true to start a new contour with arc
     * @return this builder for chaining
     */
    fun arcTo(oval: Rect, startAngle: Float, sweepAngle: Float, forceMoveTo: Boolean): PathBuilder {
        return arcTo(oval.left, oval.top, oval.right, oval.bottom, startAngle, sweepAngle, forceMoveTo)
    }

    /**
     * Appends arc to path. Arc is part of ellipse bounded by rectangle.
     * 
     * @param left left edge of oval bounding ellipse
     * @param top top edge of oval bounding ellipse
     * @param right right edge of oval bounding ellipse
     * @param bottom bottom edge of oval bounding ellipse
     * @param startAngle starting angle of arc in degrees
     * @param sweepAngle sweep, in degrees. Positive is clockwise
     * @param forceMoveTo true to start a new contour with arc
     * @return this builder for chaining
     */
    fun arcTo(left: Float, top: Float, right: Float, bottom: Float, startAngle: Float, sweepAngle: Float, forceMoveTo: Boolean): PathBuilder {
        return try {
            Stats.onNativeCall()
            PathBuilder_nArcTo(_ptr, left, top, right, bottom, startAngle, sweepAngle, forceMoveTo)
            this
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Appends arc to path, after appending line if needed. Arc is contained by tangent from
     * last point to (x1, y1), and tangent from (x1, y1) to (x2, y2).
     * 
     * @param x1 x-axis value common to pair of tangents
     * @param y1 y-axis value common to pair of tangents
     * @param x2 x-axis value end of second tangent
     * @param y2 y-axis value end of second tangent
     * @param radius distance from arc to circle center
     * @return this builder for chaining
     */
    fun tangentArcTo(x1: Float, y1: Float, x2: Float, y2: Float, radius: Float): PathBuilder {
        return try {
            Stats.onNativeCall()
            PathBuilder_nTangentArcTo(_ptr, x1, y1, x2, y2, radius)
            this
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Appends arc to path, after appending line if needed.
     * 
     * @param p1 point common to pair of tangents
     * @param p2 end of second tangent
     * @param radius distance from arc to circle center
     * @return this builder for chaining
     */
    fun tangentArcTo(p1: Point, p2: Point, radius: Float): PathBuilder {
        return tangentArcTo(p1.x, p1.y, p2.x, p2.y, radius)
    }

    /**
     * Appends arc to path. Arc is implemented by one or more conics weighted to describe
     * part of oval with radii (rx, ry) rotated by xAxisRotate degrees.
     * 
     * @param rx radius on x-axis before x-axis rotation
     * @param ry radius on y-axis before x-axis rotation
     * @param xAxisRotate x-axis rotation in degrees; positive values are clockwise
     * @param arc chooses smaller or larger arc
     * @param direction chooses clockwise or counterclockwise arc
     * @param x end of arc on x-axis
     * @param y end of arc on y-axis
     * @return this builder for chaining
     */
    fun ellipticalArcTo(
        rx: Float,
        ry: Float,
        xAxisRotate: Float,
        arc: PathEllipseArc,
        direction: PathDirection,
        x: Float,
        y: Float
    ): PathBuilder {
        return try {
            Stats.onNativeCall()
            PathBuilder_nEllipticalArcTo(_ptr, rx, ry, xAxisRotate, arc.ordinal, direction.ordinal, x, y)
            this
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Appends arc to path using point parameters.
     * 
     * @param r radii on axes before x-axis rotation
     * @param xAxisRotate x-axis rotation in degrees
     * @param arc chooses smaller or larger arc
     * @param direction chooses clockwise or counterclockwise arc
     * @param xy end of arc
     * @return this builder for chaining
     */
    fun ellipticalArcTo(r: Point, xAxisRotate: Float, arc: PathEllipseArc, direction: PathDirection, xy: Point): PathBuilder {
        return ellipticalArcTo(r.x, r.y, xAxisRotate, arc, direction, xy.x, xy.y)
    }

    /**
     * Appends arc to path, relative to last point.
     * 
     * @param rx radius before x-axis rotation
     * @param ry radius before x-axis rotation
     * @param xAxisRotate x-axis rotation in degrees
     * @param arc chooses smaller or larger arc
     * @param direction chooses clockwise or counterclockwise arc
     * @param dx x-axis offset end of arc from last point
     * @param dy y-axis offset end of arc from last point
     * @return this builder for chaining
     */
    fun rEllipticalArcTo(
        rx: Float,
        ry: Float,
        xAxisRotate: Float,
        arc: PathEllipseArc,
        direction: PathDirection,
        dx: Float,
        dy: Float
    ): PathBuilder {
        return try {
            Stats.onNativeCall()
            PathBuilder_nREllipticalArcTo(_ptr, rx, ry, xAxisRotate, arc.ordinal, direction.ordinal, dx, dy)
            this
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Appends CLOSE verb to path. A closed contour connects the first and last point with line.
     * 
     * @return this builder for chaining
     */
    fun closePath(): PathBuilder {
        return try {
            Stats.onNativeCall()
            PathBuilder_nClosePath(_ptr)
            this
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Adds rectangle to path, appending MOVE, three LINE, and CLOSE verbs.
     * 
     * @param rect rectangle to add as a closed contour
     * @param dir direction to wind added contour
     * @param start initial corner: 0=top-left, 1=top-right, 2=bottom-right, 3=bottom-left
     * @return this builder for chaining
     */
    fun addRect(rect: Rect, dir: PathDirection = PathDirection.CLOCKWISE, start: Int = 0): PathBuilder {
        return addRect(rect.left, rect.top, rect.right, rect.bottom, dir, start)
    }

    /**
     * Adds rectangle to path, appending MOVE, three LINE, and CLOSE verbs.
     * 
     * @param left left edge of rectangle
     * @param top top edge of rectangle
     * @param right right edge of rectangle
     * @param bottom bottom edge of rectangle
     * @param dir direction to wind added contour
     * @param start initial corner: 0=top-left, 1=top-right, 2=bottom-right, 3=bottom-left
     * @return this builder for chaining
     */
    fun addRect(left: Float, top: Float, right: Float, bottom: Float, dir: PathDirection = PathDirection.CLOCKWISE, start: Int = 0): PathBuilder {
        return try {
            Stats.onNativeCall()
            PathBuilder_nAddRect(_ptr, left, top, right, bottom, dir.ordinal, start)
            this
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Adds oval to path, appending MOVE, four CONIC, and CLOSE verbs.
     * 
     * @param oval bounds of ellipse
     * @param dir direction to wind ellipse
     * @param start index of initial point: 0=top, 1=right, 2=bottom, 3=left
     * @return this builder for chaining
     */
    fun addOval(oval: Rect, dir: PathDirection = PathDirection.CLOCKWISE, start: Int = 1): PathBuilder {
        return addOval(oval.left, oval.top, oval.right, oval.bottom, dir, start)
    }

    /**
     * Adds oval to path, appending MOVE, four CONIC, and CLOSE verbs.
     * 
     * @param left left edge of oval
     * @param top top edge of oval
     * @param right right edge of oval
     * @param bottom bottom edge of oval
     * @param dir direction to wind ellipse
     * @param start index of initial point: 0=top, 1=right, 2=bottom, 3=left
     * @return this builder for chaining
     */
    fun addOval(left: Float, top: Float, right: Float, bottom: Float, dir: PathDirection = PathDirection.CLOCKWISE, start: Int = 1): PathBuilder {
        return try {
            Stats.onNativeCall()
            PathBuilder_nAddOval(_ptr, left, top, right, bottom, dir.ordinal, start)
            this
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Adds circle centered at (x, y) of size radius to path.
     * 
     * @param x center of circle on x-axis
     * @param y center of circle on y-axis
     * @param radius distance from center to edge
     * @param dir direction to wind circle
     * @return this builder for chaining
     */
    fun addCircle(x: Float, y: Float, radius: Float, dir: PathDirection = PathDirection.CLOCKWISE): PathBuilder {
        return try {
            Stats.onNativeCall()
            PathBuilder_nAddCircle(_ptr, x, y, radius, dir.ordinal)
            this
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Appends arc to path, as the start of new contour.
     * 
     * @param oval bounds of ellipse containing arc
     * @param startAngle starting angle of arc in degrees
     * @param sweepAngle sweep, in degrees. Positive is clockwise
     * @return this builder for chaining
     */
    fun addArc(oval: Rect, startAngle: Float, sweepAngle: Float): PathBuilder {
        return addArc(oval.left, oval.top, oval.right, oval.bottom, startAngle, sweepAngle)
    }

    /**
     * Appends arc to path, as the start of new contour.
     * 
     * @param left left edge of oval bounding ellipse
     * @param top top edge of oval bounding ellipse
     * @param right right edge of oval bounding ellipse
     * @param bottom bottom edge of oval bounding ellipse
     * @param startAngle starting angle of arc in degrees
     * @param sweepAngle sweep, in degrees. Positive is clockwise
     * @return this builder for chaining
     */
    fun addArc(left: Float, top: Float, right: Float, bottom: Float, startAngle: Float, sweepAngle: Float): PathBuilder {
        return try {
            Stats.onNativeCall()
            PathBuilder_nAddArc(_ptr, left, top, right, bottom, startAngle, sweepAngle)
            this
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Adds rounded rectangle to path, creating a new closed contour.
     * 
     * @param rrect bounds and radii of rounded rectangle
     * @param dir direction to wind rounded rectangle
     * @param start index of initial point
     * @return this builder for chaining
     */
    fun addRRect(rrect: RRect, dir: PathDirection = PathDirection.CLOCKWISE, start: Int = 6): PathBuilder {
        return addRRect(rrect.left, rrect.top, rrect.right, rrect.bottom, rrect.radii, dir, start)
    }

    /**
     * Adds rounded rectangle to path, creating a new closed contour.
     * 
     * @param left left edge of rounded rectangle
     * @param top top edge of rounded rectangle
     * @param right right edge of rounded rectangle
     * @param bottom bottom edge of rounded rectangle
     * @param radii array of 8 radius values, 2 for each corner
     * @param dir direction to wind rounded rectangle
     * @param start index of initial point
     * @return this builder for chaining
     */
    fun addRRect(left: Float, top: Float, right: Float, bottom: Float, radii: FloatArray, dir: PathDirection = PathDirection.CLOCKWISE, start: Int = 6): PathBuilder {
        return try {
            Stats.onNativeCall()
            interopScope {
                PathBuilder_nAddRRect(_ptr, left, top, right, bottom, toInterop(radii), radii.size, dir.ordinal, start)
            }
            this
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Adds contour created from line array.
     * 
     * @param pts array of points
     * @param close true to add line connecting contour end and start
     * @return this builder for chaining
     */
    fun addPoly(pts: Array<Point>, close: Boolean): PathBuilder {
        val flat = FloatArray(pts.size * 2)
        for (i in pts.indices) {
            flat[i * 2] = pts[i].x
            flat[i * 2 + 1] = pts[i].y
        }
        return addPoly(flat, close)
    }

    /**
     * Adds contour created from line array.
     * 
     * @param coords flat array of point coordinates (x, y pairs)
     * @param close true to add line connecting contour end and start
     * @return this builder for chaining
     */
    fun addPoly(coords: FloatArray, close: Boolean): PathBuilder {
        return try {
            Stats.onNativeCall()
            interopScope {
                PathBuilder_nAddPoly(_ptr, toInterop(coords), coords.size / 2, close)
            }
            this
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Appends src path to this builder.
     * 
     * @param src path to add
     * @param mode how to append the path (APPEND or EXTEND)
     * @return this builder for chaining
     */
    fun addPath(src: Path, mode: PathAddMode = PathAddMode.APPEND): PathBuilder {
        return try {
            Stats.onNativeCall()
            PathBuilder_nAddPath(_ptr, getPtr(src), mode.ordinal)
            this
        } finally {
            reachabilityBarrier(src)
        }
    }

    /**
     * Appends src path offset by (dx, dy).
     * 
     * @param src path to add
     * @param dx offset on x-axis
     * @param dy offset on y-axis
     * @param mode how to append the path (APPEND or EXTEND)
     * @return this builder for chaining
     */
    fun addPath(src: Path, dx: Float, dy: Float, mode: PathAddMode = PathAddMode.APPEND): PathBuilder {
        return try {
            Stats.onNativeCall()
            PathBuilder_nAddPathOffset(_ptr, getPtr(src), dx, dy, mode.ordinal)
            this
        } finally {
            reachabilityBarrier(src)
        }
    }

    /**
     * Appends src path transformed by matrix.
     * 
     * @param src path to add
     * @param matrix transform to apply
     * @param mode how to append the path (APPEND or EXTEND)
     * @return this builder for chaining
     */
    fun addPath(src: Path, matrix: Matrix33, mode: PathAddMode = PathAddMode.APPEND): PathBuilder {
        return try {
            Stats.onNativeCall()
            interopScope {
                PathBuilder_nAddPathTransform(_ptr, getPtr(src), toInterop(matrix.mat), mode.ordinal)
            }
            this
        } finally {
            reachabilityBarrier(src)
            reachabilityBarrier(matrix)
        }
    }

    /**
     * Sets last point to (x, y).
     * 
     * @param x x-coordinate
     * @param y y-coordinate
     * @return this builder for chaining
     */
    fun setLastPt(x: Float, y: Float): PathBuilder {
        return try {
            Stats.onNativeCall()
            PathBuilder_nSetLastPt(_ptr, x, y)
            this
        } finally {
            reachabilityBarrier(this)
        }
    }
}

@ExternalSymbolName("org_jetbrains_skia_PathBuilder__1nGetFinalizer")
private external fun PathBuilder_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PathBuilder__1nMake")
private external fun PathBuilder_nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PathBuilder__1nMakeWithFillType")
private external fun PathBuilder_nMakeWithFillType(fillType: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PathBuilder__1nMakeFromPath")
private external fun PathBuilder_nMakeFromPath(pathPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PathBuilder__1nSnapshot")
private external fun PathBuilder_nSnapshot(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PathBuilder__1nDetach")
private external fun PathBuilder_nDetach(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PathBuilder__1nSetFillType")
private external fun PathBuilder_nSetFillType(ptr: NativePointer, fillType: Int)

@ExternalSymbolName("org_jetbrains_skia_PathBuilder__1nReset")
private external fun PathBuilder_nReset(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_PathBuilder__1nIncReserve")
private external fun PathBuilder_nIncReserve(ptr: NativePointer, extraPtCount: Int)

@ExternalSymbolName("org_jetbrains_skia_PathBuilder__1nMoveTo")
private external fun PathBuilder_nMoveTo(ptr: NativePointer, x: Float, y: Float)

@ExternalSymbolName("org_jetbrains_skia_PathBuilder__1nRMoveTo")
private external fun PathBuilder_nRMoveTo(ptr: NativePointer, dx: Float, dy: Float)

@ExternalSymbolName("org_jetbrains_skia_PathBuilder__1nLineTo")
private external fun PathBuilder_nLineTo(ptr: NativePointer, x: Float, y: Float)

@ExternalSymbolName("org_jetbrains_skia_PathBuilder__1nRLineTo")
private external fun PathBuilder_nRLineTo(ptr: NativePointer, dx: Float, dy: Float)

@ExternalSymbolName("org_jetbrains_skia_PathBuilder__1nQuadTo")
private external fun PathBuilder_nQuadTo(ptr: NativePointer, x1: Float, y1: Float, x2: Float, y2: Float)

@ExternalSymbolName("org_jetbrains_skia_PathBuilder__1nRQuadTo")
private external fun PathBuilder_nRQuadTo(ptr: NativePointer, dx1: Float, dy1: Float, dx2: Float, dy2: Float)

@ExternalSymbolName("org_jetbrains_skia_PathBuilder__1nConicTo")
private external fun PathBuilder_nConicTo(ptr: NativePointer, x1: Float, y1: Float, x2: Float, y2: Float, w: Float)

@ExternalSymbolName("org_jetbrains_skia_PathBuilder__1nRConicTo")
private external fun PathBuilder_nRConicTo(ptr: NativePointer, dx1: Float, dy1: Float, dx2: Float, dy2: Float, w: Float)

@ExternalSymbolName("org_jetbrains_skia_PathBuilder__1nCubicTo")
private external fun PathBuilder_nCubicTo(ptr: NativePointer, x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float)

@ExternalSymbolName("org_jetbrains_skia_PathBuilder__1nRCubicTo")
private external fun PathBuilder_nRCubicTo(ptr: NativePointer, dx1: Float, dy1: Float, dx2: Float, dy2: Float, dx3: Float, dy3: Float)

@ExternalSymbolName("org_jetbrains_skia_PathBuilder__1nArcTo")
private external fun PathBuilder_nArcTo(ptr: NativePointer, left: Float, top: Float, right: Float, bottom: Float, startAngle: Float, sweepAngle: Float, forceMoveTo: Boolean)

@ExternalSymbolName("org_jetbrains_skia_PathBuilder__1nTangentArcTo")
private external fun PathBuilder_nTangentArcTo(ptr: NativePointer, x1: Float, y1: Float, x2: Float, y2: Float, radius: Float)

@ExternalSymbolName("org_jetbrains_skia_PathBuilder__1nEllipticalArcTo")
private external fun PathBuilder_nEllipticalArcTo(ptr: NativePointer, rx: Float, ry: Float, xAxisRotate: Float, arc: Int, direction: Int, x: Float, y: Float)

@ExternalSymbolName("org_jetbrains_skia_PathBuilder__1nREllipticalArcTo")
private external fun PathBuilder_nREllipticalArcTo(ptr: NativePointer, rx: Float, ry: Float, xAxisRotate: Float, arc: Int, direction: Int, dx: Float, dy: Float)

@ExternalSymbolName("org_jetbrains_skia_PathBuilder__1nClosePath")
private external fun PathBuilder_nClosePath(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_PathBuilder__1nAddRect")
private external fun PathBuilder_nAddRect(ptr: NativePointer, left: Float, top: Float, right: Float, bottom: Float, dir: Int, start: Int)

@ExternalSymbolName("org_jetbrains_skia_PathBuilder__1nAddOval")
private external fun PathBuilder_nAddOval(ptr: NativePointer, left: Float, top: Float, right: Float, bottom: Float, dir: Int, start: Int)

@ExternalSymbolName("org_jetbrains_skia_PathBuilder__1nAddCircle")
private external fun PathBuilder_nAddCircle(ptr: NativePointer, x: Float, y: Float, radius: Float, dir: Int)

@ExternalSymbolName("org_jetbrains_skia_PathBuilder__1nAddArc")
private external fun PathBuilder_nAddArc(ptr: NativePointer, left: Float, top: Float, right: Float, bottom: Float, startAngle: Float, sweepAngle: Float)

@ExternalSymbolName("org_jetbrains_skia_PathBuilder__1nAddRRect")
private external fun PathBuilder_nAddRRect(ptr: NativePointer, left: Float, top: Float, right: Float, bottom: Float, radii: InteropPointer, size: Int, dir: Int, start: Int)

@ExternalSymbolName("org_jetbrains_skia_PathBuilder__1nAddPoly")
private external fun PathBuilder_nAddPoly(ptr: NativePointer, coords: InteropPointer, count: Int, close: Boolean)

@ExternalSymbolName("org_jetbrains_skia_PathBuilder__1nAddPath")
private external fun PathBuilder_nAddPath(ptr: NativePointer, srcPtr: NativePointer, mode: Int)

@ExternalSymbolName("org_jetbrains_skia_PathBuilder__1nAddPathOffset")
private external fun PathBuilder_nAddPathOffset(ptr: NativePointer, srcPtr: NativePointer, dx: Float, dy: Float, mode: Int)

@ExternalSymbolName("org_jetbrains_skia_PathBuilder__1nAddPathTransform")
private external fun PathBuilder_nAddPathTransform(ptr: NativePointer, srcPtr: NativePointer, matrix: InteropPointer, mode: Int)

@ExternalSymbolName("org_jetbrains_skia_PathBuilder__1nSetLastPt")
private external fun PathBuilder_nSetLastPt(ptr: NativePointer, x: Float, y: Float)
