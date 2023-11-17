package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import kotlin.math.min

/**
 *
 * Path contain geometry. Path may be empty, or contain one or more verbs that
 * outline a figure. Path always starts with a move verb to a Cartesian coordinate,
 * and may be followed by additional verbs that add lines or curves.
 *
 *
 * Adding a close verb makes the geometry into a continuous loop, a closed contour.
 * Path may contain any number of contours, each beginning with a move verb.
 *
 *
 * Path contours may contain only a move verb, or may also contain lines,
 * quadratic beziers, conics, and cubic beziers. Path contours may be open or
 * closed.
 *
 *
 * When used to draw a filled area, Path describes whether the fill is inside or
 * outside the geometry. Path also describes the winding rule used to fill
 * overlapping contours.
 *
 *
 * Internally, Path lazily computes metrics likes bounds and convexity. Call
 * [.updateBoundsCache] to make Path thread safe.
 */
class Path internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR), Iterable<PathSegment?> {
    companion object {
        fun makeFromSVGString(svg: String): Path  {
            Stats.onNativeCall()
            val result = interopScope {
                _nMakeFromSVGString(toInterop(svg))
            }

            if (result == NullPointer) {
                throw IllegalArgumentException("Failed to parse SVG Path string: $svg")
            } else {
                return Path(result)
            }
        }

        /**
         *
         * Tests if line between Point pair is degenerate.
         *
         *
         * Line with no length or that moves a very short distance is degenerate; it is
         * treated as a point.
         *
         *
         * exact changes the equality test. If true, returns true only if p1 equals p2.
         * If false, returns true if p1 equals or nearly equals p2.
         *
         * @param p1     line start point
         * @param p2     line end point
         * @param exact  if false, allow nearly equals
         * @return       true if line is degenerate; its length is effectively zero
         *
         * @see [https://fiddle.skia.org/c/@Path_IsLineDegenerate](https://fiddle.skia.org/c/@Path_IsLineDegenerate)
         */
        fun isLineDegenerate(p1: Point, p2: Point, exact: Boolean): Boolean {
            Stats.onNativeCall()
            return _nIsLineDegenerate(p1.x, p1.y, p2.x, p2.y, exact)
        }

        /**
         *
         * Tests if quad is degenerate.
         *
         *
         * Quad with no length or that moves a very short distance is degenerate; it is
         * treated as a point.
         *
         * @param p1     quad start point
         * @param p2     quad control point
         * @param p3     quad end point
         * @param exact  if true, returns true only if p1, p2, and p3 are equal;
         * if false, returns true if p1, p2, and p3 are equal or nearly equal
         * @return       true if quad is degenerate; its length is effectively zero
         */
        fun isQuadDegenerate(p1: Point, p2: Point, p3: Point, exact: Boolean): Boolean {
            Stats.onNativeCall()
            return _nIsQuadDegenerate(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, exact)
        }

        /**
         *
         * Tests if cubic is degenerate.
         *
         *
         * Cubic with no length or that moves a very short distance is degenerate; it is
         * treated as a point.
         *
         * @param p1     cubic start point
         * @param p2     cubic control point 1
         * @param p3     cubic control point 2
         * @param p4     cubic end point
         * @param exact  if true, returns true only if p1, p2, p3, and p4 are equal;
         * if false, returns true if p1, p2, p3, and p4 are equal or nearly equal
         * @return       true if cubic is degenerate; its length is effectively zero
         */
        fun isCubicDegenerate(p1: Point, p2: Point, p3: Point, p4: Point, exact: Boolean): Boolean {
            Stats.onNativeCall()
            return _nIsCubicDegenerate(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, p4.x, p4.y, exact)
        }

        /**
         *
         * Approximates conic with quad array. Conic is constructed from start Point p0,
         * control Point p1, end Point p2, and weight w.
         *
         *
         * Quad array is stored in pts; this storage is supplied by caller.
         *
         *
         * Maximum quad count is 2 to the pow2.
         *
         *
         * Every third point in array shares last Point of previous quad and first Point of
         * next quad. Maximum pts storage size is given by: `(1 + 2 * (1 << pow2)).</p>`
         *
         *
         * Returns quad count used the approximation, which may be smaller
         * than the number requested.
         *
         *
         * conic weight determines the amount of influence conic control point has on the curve.
         *
         *
         * w less than one represents an elliptical section. w greater than one represents
         * a hyperbolic section. w equal to one represents a parabolic section.
         *
         *
         * Two quad curves are sufficient to approximate an elliptical conic with a sweep
         * of up to 90 degrees; in this case, set pow2 to one.
         *
         * @param p0    conic start Point
         * @param p1    conic control Point
         * @param p2    conic end Point
         * @param w     conic weight
         * @param pow2  quad count, as power of two, normally 0 to 5 (1 to 32 quad curves)
         * @return      number of quad curves written to pts
         */
        fun convertConicToQuads(p0: Point, p1: Point, p2: Point, w: Float, pow2: Int): Array<Point> {
            Stats.onNativeCall()
            val maxResultPointCount = (1 + 2 * (1 shl pow2)) // See Skia docs
            var pointCount = 0
            val coords = withResult(FloatArray(maxResultPointCount * 2)) {
                pointCount = _nConvertConicToQuads(p0.x, p0.y, p1.x, p1.y, p2.x, p2.y, w, pow2, it)
            }
            return Array(pointCount) { Point(coords[2 * it], coords[2 * it + 1]) }
        }

        /**
         *
         * Returns Path that is the result of applying the Op to the first path and the second path.
         *
         * The resulting path will be constructed from non-overlapping contours.
         *
         * The curve order is reduced where possible so that cubics may be turned
         * into quadratics, and quadratics maybe turned into lines.
         *
         * @param one The first operand (for difference, the minuend)
         * @param two The second operand (for difference, the subtrahend)
         * @param op  The operator to apply.
         * @return    Path if operation was able to produce a result, null otherwise
         */
        fun makeCombining(one: Path, two: Path, op: PathOp): Path? {
            return try {
                Stats.onNativeCall()
                val ptr = _nMakeCombining(
                    getPtr(one),
                    getPtr(two),
                    op.ordinal
                )
                if (ptr == NullPointer) null else Path(ptr)
            } finally {
                reachabilityBarrier(one)
                reachabilityBarrier(two)
            }
        }

        /**
         *
         * Initializes Path from byte buffer. Returns null if the buffer is
         * data is inconsistent, or the length is too small.
         *
         *
         * Reads [PathFillMode], verb array, Point array, conic weight, and
         * additionally reads computed information like path convexity and bounds.
         *
         *
         * Used only in concert with [];
         * the format used for Path in memory is not guaranteed.
         *
         * @param data  storage for Path
         * @return      reconstructed Path
         *
         * @see [https://fiddle.skia.org/c/@Path_readFromMemory](https://fiddle.skia.org/c/@Path_readFromMemory)
         */
        fun makeFromBytes(data: ByteArray): Path {
            Stats.onNativeCall()
            val result = interopScope {
                _nMakeFromBytes(toInterop(data), data.size)
            }

            if (result == NullPointer) {
                throw IllegalArgumentException("Failed to parse serialized Path")
            } else {
                return Path(result)
            }
        }

        init {
            staticLoad()
        }
    }

    internal object _FinalizerHolder {
        val PTR = Path_nGetFinalizer()
    }

    /**
     * Constructs an empty Path. By default, Path has no verbs, no [Point], and no weights.
     * FillMode is set to [PathFillMode.WINDING].
     */
    constructor() : this(Path_nMake()) {
        Stats.onNativeCall()
    }

    /**
     * Compares this path and o; Returns true if [PathFillMode], verb array, Point array, and weights
     * are equivalent.
     *
     * @param other  Path to compare
     * @return   true if this and Path are equivalent
     */
    override fun nativeEquals(other: Native?): Boolean {
        return try {
            Path_nEquals(_ptr, getPtr(other))
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(other)
        }
    }

    /**
     *
     * Returns true if Path contain equal verbs and equal weights.
     * If Path contain one or more conics, the weights must match.
     *
     *
     * [.conicTo] may add different verbs
     * depending on conic weight, so it is not trivial to interpolate a pair of Path
     * containing conics with different conic weight values.
     *
     * @param compare  Path to compare
     * @return         true if Path verb array and weights are equivalent
     *
     * @see [https://fiddle.skia.org/c/@Path_isInterpolatable](https://fiddle.skia.org/c/@Path_isInterpolatable)
     */
    fun isInterpolatable(compare: Path?): Boolean {
        return try {
            Stats.onNativeCall()
            _nIsInterpolatable(
                _ptr,
                getPtr(compare)
            )
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(compare)
        }
    }

    /**
     * Interpolates between Path with [Point] array of equal size.
     * Copy verb array and weights to out, and set out Point array to a weighted
     * average of this Point array and ending Point array, using the formula:
     *
     *
     * `(Path Point * weight) + ending Point * (1 - weight)`
     *
     *
     * weight is most useful when between zero (ending Point array) and
     * one (this Point_Array); will work with values outside of this
     * range.
     *
     *
     * interpolate() returns null if Point array is not
     * the same size as ending Point array. Call [.isInterpolatable] to check Path
     * compatibility prior to calling interpolate().
     *
     * @param ending  Point array averaged with this Point array
     * @param weight  contribution of this Point array, and
     * one minus contribution of ending Point array
     * @return        interpolated Path if Path contain same number of Point, null otherwise
     *
     * @see [https://fiddle.skia.org/c/@Path_interpolate](https://fiddle.skia.org/c/@Path_interpolate)
     */
    fun makeLerp(ending: Path?, weight: Float): Path {
        return try {
            Stats.onNativeCall()
            val ptr = _nMakeLerp(
                _ptr,
                getPtr(ending),
                weight
            )
            require(ptr != NullPointer) { "Point array is not the same size as ending Point array" }
            Path(ptr)
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(ending)
        }
    }

    var fillMode: PathFillMode
        get() = try {
            Stats.onNativeCall()
            PathFillMode.values().get(_nGetFillMode(_ptr))
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            _nSetFillMode(_ptr, value.ordinal)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * Returns true if the path is convex. If necessary, it will first compute the convexity.
     *
     * @return  true or false
     */
    val isConvex: Boolean
        get() = try {
            Stats.onNativeCall()
            _nIsConvex(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * Returns oval bounds if this path is recognized as an oval or circle.
     *
     * @return  bounds is recognized as an oval or circle, null otherwise
     *
     * @see [https://fiddle.skia.org/c/@Path_isOval](https://fiddle.skia.org/c/@Path_isOval)
     */
    val isOval: Rect?
        get() = try {
            Stats.onNativeCall()
            Rect.fromInteropPointerNullable { _nIsOval(_ptr, it) }
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * Returns [RRect] if this path is recognized as an oval, circle or RRect.
     *
     * @return  bounds is recognized as an oval, circle or RRect, null otherwise
     *
     * @see [https://fiddle.skia.org/c/@Path_isRRect](https://fiddle.skia.org/c/@Path_isRRect)
     */
    val isRRect: RRect?
        get() = try {
            Stats.onNativeCall()
            RRect.fromInteropPointerNullable { _nIsRRect(_ptr, it) }
        } finally {
            reachabilityBarrier(this)
        }

    /**
     *
     * Sets Path to its initial state.
     *
     *
     * Removes verb array, Point array, and weights, and sets FillMode to [PathFillMode.WINDING].
     * Internal storage associated with Path is released.
     *
     * @return  this
     *
     * @see [https://fiddle.skia.org/c/@Path_reset](https://fiddle.skia.org/c/@Path_reset)
     */
    fun reset(): Path {
        Stats.onNativeCall()
        Path_nReset(_ptr)
        return this
    }

    /**
     *
     * Sets Path to its initial state, preserving internal storage.
     * Removes verb array, Point array, and weights, and sets FillMode to kWinding.
     * Internal storage associated with Path is retained.
     *
     *
     * Use [.rewind] instead of [.reset] if Path storage will be reused and performance
     * is critical.
     *
     * @return  this
     *
     * @see [https://fiddle.skia.org/c/@Path_rewind](https://fiddle.skia.org/c/@Path_rewind)
     */
    fun rewind(): Path {
        Stats.onNativeCall()
        _nRewind(_ptr)
        return this
    }

    /**
     *
     * Returns if Path is empty.
     *
     *
     * Empty Path may have FillMode but has no [Point], [PathVerb], or conic weight.
     * [] constructs empty Path; [.reset] and [.rewind] make Path empty.
     *
     * @return  true if the path contains no Verb array
     */
    val isEmpty: Boolean
        get() = try {
            Stats.onNativeCall()
            _nIsEmpty(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     *
     * Returns if contour is closed.
     *
     *
     * Contour is closed if Path Verb array was last modified by [.closePath]. When stroked,
     * closed contour draws [PaintStrokeJoin] instead of [PaintStrokeCap] at first and last Point.
     *
     * @return  true if the last contour ends with a [PathVerb.CLOSE]
     *
     * @see [https://fiddle.skia.org/c/@Path_isLastContourClosed](https://fiddle.skia.org/c/@Path_isLastContourClosed)
     */
    val isLastContourClosed: Boolean
        get() = try {
            Stats.onNativeCall()
            _nIsLastContourClosed(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * Returns true for finite Point array values between negative Float.MIN_VALUE and
     * positive Float.MAX_VALUE. Returns false for any Point array value of
     * Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, or Float.NaN.
     *
     * @return  true if all Point values are finite
     */
    val isFinite: Boolean
        get() = try {
            Stats.onNativeCall()
            // TODO For some reason this method returns 0 instead of false in JS target, investigate
            !!_nIsFinite(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     *
     * Specifies whether Path is volatile; whether it will be altered or discarded
     * by the caller after it is drawn. Path by default have volatile set false, allowing
     * SkBaseDevice to attach a cache of data which speeds repeated drawing.
     *
     * Mark temporary paths, discarded or modified after use, as volatile
     * to inform SkBaseDevice that the path need not be cached.
     *
     * Mark animating Path volatile to improve performance.
     * Mark unchanging Path non-volatile to improve repeated rendering.
     *
     * raster surface Path draws are affected by volatile for some shadows.
     * GPU surface Path draws are affected by volatile for some shadows and concave geometries.
     *
     * Returns true if the path is volatile; it will not be altered or discarded
     * by the caller after it is drawn. Path by default have volatile set false, allowing
     * [Surface] to attach a cache of data which speeds repeated drawing. If true, [Surface]
     * may not speed repeated drawing.
     *
     * @return  true if caller will alter Path after drawing
     */
    var isVolatile: Boolean
        get() = try {
            Stats.onNativeCall()
            Path_nIsVolatile(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            Path_nSetVolatile(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }
    
    /**
     *
     * Specifies whether Path is volatile; whether it will be altered or discarded
     * by the caller after it is drawn. Path by default have volatile set false, allowing
     * SkBaseDevice to attach a cache of data which speeds repeated drawing.
     *
     *
     * Mark temporary paths, discarded or modified after use, as volatile
     * to inform SkBaseDevice that the path need not be cached.
     *
     *
     * Mark animating Path volatile to improve performance.
     * Mark unchanging Path non-volatile to improve repeated rendering.
     *
     *
     * raster surface Path draws are affected by volatile for some shadows.
     * GPU surface Path draws are affected by volatile for some shadows and concave geometries.
     *
     * @param isVolatile  true if caller will alter Path after drawing
     * @return            this
     */
    fun setVolatile(isVolatile: Boolean): Path {
        Stats.onNativeCall()
        Path_nSetVolatile(_ptr, isVolatile)
        return this
    }

    /**
     * Returns array of two points if Path contains only one line;
     * Verb array has two entries: [PathVerb.MOVE], [PathVerb.LINE].
     * Returns null if Path is not one line.
     *
     * @return  Point[2] if Path contains exactly one line, null otherwise
     *
     * @see [https://fiddle.skia.org/c/@Path_isLine](https://fiddle.skia.org/c/@Path_isLine)
     */
    val asLine: Array<Point>?
        get() = try {
            Stats.onNativeCall()
            // HACK Use a temporary Rect as a buffer to store two points
            val rectBuffer = Rect.fromInteropPointerNullable { _nMaybeGetAsLine(_ptr, it) }
            rectBuffer?.run {
                arrayOf(
                    Point(left, top),
                    Point(right, bottom)
                )
            }
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * Returns the number of points in Path.
     * Point count is initially zero.
     *
     * @return  Path Point array length
     *
     * @see [https://fiddle.skia.org/c/@Path_countPoints](https://fiddle.skia.org/c/@Path_countPoints)
     */
    val pointsCount: Int
        get() = try {
            Stats.onNativeCall()
            _nGetPointsCount(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     *
     * Returns Point at index in Point array. Valid range for index is
     * 0 to countPoints() - 1.
     *
     *
     * Returns (0, 0) if index is out of range.
     *
     * @param index  Point array element selector
     * @return       Point array value or (0, 0)
     *
     * @see [https://fiddle.skia.org/c/@Path_getPoint](https://fiddle.skia.org/c/@Path_getPoint)
     */
    fun getPoint(index: Int): Point {
        return try {
            Stats.onNativeCall()
            Point.fromInteropPointer { _nGetPoint(_ptr, index, it) }
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     *
     * Returns all points in Path.
     *
     * @return        Path Point array length
     *
     * @see [https://fiddle.skia.org/c/@Path_getPoints](https://fiddle.skia.org/c/@Path_getPoints)
     */
    val points: Array<Point?>
        get() {
            val res = arrayOfNulls<Point>(pointsCount)
            getPoints(res, res.size)
            return res
        }

    /**
     *
     * Returns number of points in Path. Up to max points are copied.
     *
     *
     * points may be null; then, max must be zero.
     * If max is greater than number of points, excess points storage is unaltered.
     *
     * @param points  storage for Path Point array. May be null
     * @param max     maximum to copy; must be greater than or equal to zero
     * @return        Path Point array length
     *
     * @see [https://fiddle.skia.org/c/@Path_getPoints](https://fiddle.skia.org/c/@Path_getPoints)
     */
    fun getPoints(points: Array<Point?>?, max: Int): Int {
        return try {
            require(if (points == null) max == 0 else max >= 0)
            Stats.onNativeCall()
            if (points == null) {
                interopScope {
                    _nGetPoints(_ptr, toInterop(null as FloatArray?), max)
                }
            } else {
                var result = 0
                val coords = withResult(FloatArray(max * 2)) {
                    result = _nGetPoints(_ptr, it, max)
                }
                for (i in 0 until min(max, result)) {
                    points[i] = Point(coords[2 * i], coords[2 * i + 1])
                }
                result
            }
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Returns the number of verbs: [PathVerb.MOVE], [PathVerb.LINE], [PathVerb.QUAD], [PathVerb.CONIC],
     * [PathVerb.CUBIC], and [PathVerb.CLOSE]; added to Path.
     *
     * @return  length of verb array
     *
     * @see [https://fiddle.skia.org/c/@Path_countVerbs](https://fiddle.skia.org/c/@Path_countVerbs)
     */
    val verbsCount: Int
        get() = try {
            Stats.onNativeCall()
            _nCountVerbs(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    val verbs: Array<PathVerb?>
        get() {
            val res = arrayOfNulls<PathVerb>(verbsCount)
            getVerbs(res, res.size)
            return res
        }

    /**
     * Returns the number of verbs in the path. Up to max verbs are copied.
     *
     * @param verbs  storage for verbs, may be null
     * @param max    maximum number to copy into verbs
     * @return       the actual number of verbs in the path
     *
     * @see [https://fiddle.skia.org/c/@Path_getVerbs](https://fiddle.skia.org/c/@Path_getVerbs)
     */
    fun getVerbs(verbs: Array<PathVerb?>?, max: Int): Int {
        return try {
            require(if (verbs == null) max == 0 else true)
            Stats.onNativeCall()
            val out = if (verbs == null) null else ByteArray(max)
            val count = interopScope {
                val ptr = toInterop(out)
                _nGetVerbs(_ptr, ptr, max).also {
                    out?.let { ptr.fromInterop(it) }
                }
            }
            if (verbs != null) for (i in 0 until minOf(count, max)) verbs[i] = PathVerb.values().get(
                out!![i].toInt()
            )
            count
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Returns the approximate byte size of the Path in memory.
     *
     * @return  approximate size
     */
    val approximateBytesUsed: NativePointer
        get() = try {
            Stats.onNativeCall()
            _nApproximateBytesUsed(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     *
     * Exchanges the verb array, Point array, weights, and FillMode with other.
     * Cached state is also exchanged. swap() internally exchanges pointers, so
     * it is lightweight and does not allocate memory.
     *
     * @param   other  Path exchanged by value
     * @return  this
     *
     * @see [https://fiddle.skia.org/c/@Path_swap](https://fiddle.skia.org/c/@Path_swap)
     */
    fun swap(other: Path?): Path {
        return try {
            Stats.onNativeCall()
            Path_nSwap(_ptr, getPtr(other))
            this
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(other)
        }
    }

    /**
     *
     * Returns minimum and maximum axes values of Point array.
     *
     *
     * Returns (0, 0, 0, 0) if Path contains no points. Returned bounds width and height may
     * be larger or smaller than area affected when Path is drawn.
     *
     *
     * Rect returned includes all Point added to Path, including Point associated with
     * [PathVerb.MOVE] that define empty contours.
     *
     * @return  bounds of all Point in Point array
     */
    val bounds: Rect
        get() = try {
            Stats.onNativeCall()
            Rect.fromInteropPointer { _nGetBounds(_ptr, it) }
        } finally {
            reachabilityBarrier(this)
        }

    /**
     *
     * Updates internal bounds so that subsequent calls to [.getBounds] are instantaneous.
     * Unaltered copies of Path may also access cached bounds through [.getBounds].
     *
     *
     * For now, identical to calling [.getBounds] and ignoring the returned value.
     *
     *
     * Call to prepare Path subsequently drawn from multiple threads,
     * to avoid a race condition where each draw separately computes the bounds.
     *
     * @return  this
     */
    fun updateBoundsCache(): Path {
        Stats.onNativeCall()
        _nUpdateBoundsCache(_ptr)
        return this
    }

    /**
     *
     * Returns minimum and maximum axes values of the lines and curves in Path.
     * Returns (0, 0, 0, 0) if Path contains no points.
     * Returned bounds width and height may be larger or smaller than area affected
     * when Path is drawn.
     *
     *
     * Includes Point associated with [PathVerb.MOVE] that define empty
     * contours.
     *
     * Behaves identically to [.getBounds] when Path contains
     * only lines. If Path contains curves, computed bounds includes
     * the maximum extent of the quad, conic, or cubic; is slower than [.getBounds];
     * and unlike [.getBounds], does not cache the result.
     *
     * @return  tight bounds of curves in Path
     *
     * @see [https://fiddle.skia.org/c/@Path_computeTightBounds](https://fiddle.skia.org/c/@Path_computeTightBounds)
     */
    fun computeTightBounds(): Rect {
        return try {
            Stats.onNativeCall()
            Rect.fromInteropPointer { _nComputeTightBounds(_ptr, it) }
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     *
     * Returns true if rect is contained by Path.
     * May return false when rect is contained by Path.
     *
     *
     * For now, only returns true if Path has one contour and is convex.
     * rect may share points and edges with Path and be contained.
     * Returns true if rect is empty, that is, it has zero width or height; and
     * the Point or line described by rect is contained by Path.
     *
     * @param rect  Rect, line, or Point checked for containment
     * @return      true if rect is contained
     *
     * @see [https://fiddle.skia.org/c/@Path_conservativelyContainsRect](https://fiddle.skia.org/c/@Path_conservativelyContainsRect)
     */
    fun conservativelyContainsRect(rect: Rect): Boolean {
        return try {
            Stats.onNativeCall()
            _nConservativelyContainsRect(
                _ptr,
                rect.left,
                rect.top,
                rect.right,
                rect.bottom
            )
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     *
     * Grows Path verb array and Point array to contain extraPtCount additional Point.
     * May improve performance and use less memory by
     * reducing the number and size of allocations when creating Path.
     *
     * @param extraPtCount  number of additional Point to allocate
     * @return              this
     *
     * @see [https://fiddle.skia.org/c/@Path_incReserve](https://fiddle.skia.org/c/@Path_incReserve)
     */
    fun incReserve(extraPtCount: Int): Path {
        Stats.onNativeCall()
        _nIncReserve(_ptr, extraPtCount)
        return this
    }

    /**
     * Adds beginning of contour at Point (x, y).
     *
     * @param x  x-axis value of contour start
     * @param y  y-axis value of contour start
     * @return   reference to Path
     *
     * @see [https://fiddle.skia.org/c/@Path_moveTo](https://fiddle.skia.org/c/@Path_moveTo)
     */
    fun moveTo(x: Float, y: Float): Path {
        Stats.onNativeCall()
        _nMoveTo(_ptr, x, y)
        return this
    }

    /**
     * Adds beginning of contour at Point p.
     *
     * @param p  contour start
     * @return   this
     */
    fun moveTo(p: Point): Path {
        return moveTo(p.x, p.y)
    }

    /**
     *
     * Adds beginning of contour relative to last point.
     *
     *
     * If Path is empty, starts contour at (dx, dy).
     * Otherwise, start contour at last point offset by (dx, dy).
     * Function name stands for "relative move to".
     *
     * @param dx  offset from last point to contour start on x-axis
     * @param dy  offset from last point to contour start on y-axis
     * @return    reference to Path
     *
     * @see [https://fiddle.skia.org/c/@Path_rMoveTo](https://fiddle.skia.org/c/@Path_rMoveTo)
     */
    fun rMoveTo(dx: Float, dy: Float): Path {
        Stats.onNativeCall()
        _nRMoveTo(_ptr, dx, dy)
        return this
    }

    /**
     *
     * Adds line from last point to (x, y). If Path is empty, or last Verb is
     * [PathVerb.CLOSE], last point is set to (0, 0) before adding line.
     *
     *
     * lineTo() appends [PathVerb.MOVE] to verb array and (0, 0) to Point array, if needed.
     * lineTo() then appends [PathVerb.LINE] to verb array and (x, y) to Point array.
     *
     * @param x  end of added line on x-axis
     * @param y  end of added line on y-axis
     * @return   this
     *
     * @see [https://fiddle.skia.org/c/@Path_lineTo](https://fiddle.skia.org/c/@Path_lineTo)
     */
    fun lineTo(x: Float, y: Float): Path {
        Stats.onNativeCall()
        _nLineTo(_ptr, x, y)
        return this
    }

    /**
     *
     * Adds line from last point to Point p. If Path is empty, or last [PathVerb] is
     * [PathVerb.CLOSE], last point is set to (0, 0) before adding line.
     *
     *
     * lineTo() first appends [PathVerb.MOVE] to verb array and (0, 0) to Point array, if needed.
     * lineTo() then appends [PathVerb.LINE] to verb array and Point p to Point array.
     *
     * @param p  end Point of added line
     * @return   reference to Path
     */
    fun lineTo(p: Point): Path {
        return lineTo(p.x, p.y)
    }

    /**
     *
     * Adds line from last point to vector (dx, dy). If Path is empty, or last [PathVerb] is
     * [PathVerb.CLOSE], last point is set to (0, 0) before adding line.
     *
     *
     * Appends [PathVerb.MOVE] to verb array and (0, 0) to Point array, if needed;
     * then appends [PathVerb.LINE] to verb array and line end to Point array.
     *
     *
     * Line end is last point plus vector (dx, dy).
     *
     *
     * Function name stands for "relative line to".
     *
     * @param dx  offset from last point to line end on x-axis
     * @param dy  offset from last point to line end on y-axis
     * @return    reference to Path
     *
     * @see [https://fiddle.skia.org/c/@Path_rLineTo](https://fiddle.skia.org/c/@Path_rLineTo)
     *
     * @see [https://fiddle.skia.org/c/@Quad_a](https://fiddle.skia.org/c/@Quad_a)
     *
     * @see [https://fiddle.skia.org/c/@Quad_b](https://fiddle.skia.org/c/@Quad_b)
     */
    fun rLineTo(dx: Float, dy: Float): Path {
        Stats.onNativeCall()
        _nRLineTo(_ptr, dx, dy)
        return this
    }

    /**
     * Adds quad from last point towards (x1, y1), to (x2, y2).
     * If Path is empty, or last [PathVerb] is [PathVerb.CLOSE], last point is set to (0, 0)
     * before adding quad.
     *
     * Appends [PathVerb.MOVE] to verb array and (0, 0) to Point array, if needed;
     * then appends [PathVerb.QUAD] to verb array; and (x1, y1), (x2, y2)
     * to Point array.
     *
     * @param x1  control Point of quad on x-axis
     * @param y1  control Point of quad on y-axis
     * @param x2  end Point of quad on x-axis
     * @param y2  end Point of quad on y-axis
     * @return    reference to Path
     *
     * @see [https://fiddle.skia.org/c/@Path_quadTo](https://fiddle.skia.org/c/@Path_quadTo)
     */
    fun quadTo(x1: Float, y1: Float, x2: Float, y2: Float): Path {
        Stats.onNativeCall()
        _nQuadTo(_ptr, x1, y1, x2, y2)
        return this
    }

    /**
     *
     * Adds quad from last point towards Point p1, to Point p2.
     *
     *
     * If Path is empty, or last [PathVerb] is [PathVerb.CLOSE], last point is set to (0, 0)
     * before adding quad.
     *
     *
     * Appends [PathVerb.MOVE] to verb array and (0, 0) to Point array, if needed;
     * then appends [PathVerb.QUAD] to verb array; and Point p1, p2
     * to Point array.
     *
     * @param p1  control Point of added quad
     * @param p2  end Point of added quad
     * @return    reference to Path
     */
    fun quadTo(p1: Point, p2: Point): Path {
        return quadTo(p1.x, p1.y, p2.x, p2.y)
    }

    /**
     *
     * Adds quad from last point towards vector (dx1, dy1), to vector (dx2, dy2).
     * If Path is empty, or last [PathVerb]
     * is [PathVerb.CLOSE], last point is set to (0, 0) before adding quad.
     *
     *
     * Appends [PathVerb.MOVE] to verb array and (0, 0) to Point array,
     * if needed; then appends [PathVerb.QUAD] to verb array; and appends quad
     * control and quad end to Point array.
     *
     *
     * Quad control is last point plus vector (dx1, dy1).
     *
     *
     * Quad end is last point plus vector (dx2, dy2).
     *
     *
     * Function name stands for "relative quad to".
     *
     * @param dx1  offset from last point to quad control on x-axis
     * @param dy1  offset from last point to quad control on y-axis
     * @param dx2  offset from last point to quad end on x-axis
     * @param dy2  offset from last point to quad end on y-axis
     * @return     reference to Path
     *
     * @see [https://fiddle.skia.org/c/@Conic_Weight_a](https://fiddle.skia.org/c/@Conic_Weight_a)
     *
     * @see [https://fiddle.skia.org/c/@Conic_Weight_b](https://fiddle.skia.org/c/@Conic_Weight_b)
     *
     * @see [https://fiddle.skia.org/c/@Conic_Weight_c](https://fiddle.skia.org/c/@Conic_Weight_c)
     *
     * @see [https://fiddle.skia.org/c/@Path_rQuadTo](https://fiddle.skia.org/c/@Path_rQuadTo)
     */
    fun rQuadTo(dx1: Float, dy1: Float, dx2: Float, dy2: Float): Path {
        Stats.onNativeCall()
        _nRQuadTo(_ptr, dx1, dy1, dx2, dy2)
        return this
    }

    /**
     *
     * Adds conic from last point towards (x1, y1), to (x2, y2), weighted by w.
     *
     *
     * If Path is empty, or last [PathVerb] is [PathVerb.CLOSE], last point is set to (0, 0)
     * before adding conic.
     *
     *
     * Appends [PathVerb.MOVE] to verb array and (0, 0) to Point array, if needed.
     *
     *
     * If w is finite and not one, appends [PathVerb.CONIC] to verb array;
     * and (x1, y1), (x2, y2) to Point array; and w to conic weights.
     *
     *
     * If w is one, appends [PathVerb.QUAD] to verb array, and
     * (x1, y1), (x2, y2) to Point array.
     *
     *
     * If w is not finite, appends [PathVerb.LINE] twice to verb array, and
     * (x1, y1), (x2, y2) to Point array.
     *
     * @param x1  control Point of conic on x-axis
     * @param y1  control Point of conic on y-axis
     * @param x2  end Point of conic on x-axis
     * @param y2  end Point of conic on y-axis
     * @param w   weight of added conic
     * @return    reference to Path
     */
    fun conicTo(x1: Float, y1: Float, x2: Float, y2: Float, w: Float): Path {
        Stats.onNativeCall()
        _nConicTo(_ptr, x1, y1, x2, y2, w)
        return this
    }

    /**
     *
     * Adds conic from last point towards Point p1, to Point p2, weighted by w.
     *
     *
     * If Path is empty, or last [PathVerb] is [PathVerb.CLOSE], last point is set to (0, 0)
     * before adding conic.
     *
     *
     * Appends [PathVerb.MOVE] to verb array and (0, 0) to Point array, if needed.
     *
     *
     * If w is finite and not one, appends [PathVerb.CONIC] to verb array;
     * and Point p1, p2 to Point array; and w to conic weights.
     *
     *
     * If w is one, appends [PathVerb.QUAD] to verb array, and Point p1, p2
     * to Point array.
     *
     *
     * If w is not finite, appends [PathVerb.LINE] twice to verb array, and
     * Point p1, p2 to Point array.
     *
     * @param p1  control Point of added conic
     * @param p2  end Point of added conic
     * @param w   weight of added conic
     * @return    reference to Path
     */
    fun conicTo(p1: Point, p2: Point, w: Float): Path {
        return conicTo(p1.x, p1.y, p2.x, p2.y, w)
    }

    /**
     *
     * Adds conic from last point towards vector (dx1, dy1), to vector (dx2, dy2),
     * weighted by w. If Path is empty, or last [PathVerb]
     * is [PathVerb.CLOSE], last point is set to (0, 0) before adding conic.
     *
     *
     * Appends [PathVerb.MOVE] to verb array and (0, 0) to Point array,
     * if needed.
     *
     *
     * If w is finite and not one, next appends [PathVerb.CONIC] to verb array,
     * and w is recorded as conic weight; otherwise, if w is one, appends
     * [PathVerb.QUAD] to verb array; or if w is not finite, appends [PathVerb.LINE]
     * twice to verb array.
     *
     *
     * In all cases appends Point control and end to Point array.
     * control is last point plus vector (dx1, dy1).
     * end is last point plus vector (dx2, dy2).
     *
     *
     * Function name stands for "relative conic to".
     *
     * @param dx1  offset from last point to conic control on x-axis
     * @param dy1  offset from last point to conic control on y-axis
     * @param dx2  offset from last point to conic end on x-axis
     * @param dy2  offset from last point to conic end on y-axis
     * @param w    weight of added conic
     * @return     reference to Path
     */
    fun rConicTo(dx1: Float, dy1: Float, dx2: Float, dy2: Float, w: Float): Path {
        Stats.onNativeCall()
        _nRConicTo(_ptr, dx1, dy1, dx2, dy2, w)
        return this
    }

    /**
     *
     * Adds cubic from last point towards (x1, y1), then towards (x2, y2), ending at
     * (x3, y3). If Path is empty, or last [PathVerb] is [PathVerb.CLOSE], last point is set to
     * (0, 0) before adding cubic.
     *
     *
     * Appends [PathVerb.MOVE] to verb array and (0, 0) to Point array, if needed;
     * then appends [PathVerb.CUBIC] to verb array; and (x1, y1), (x2, y2), (x3, y3)
     * to Point array.
     *
     * @param x1  first control Point of cubic on x-axis
     * @param y1  first control Point of cubic on y-axis
     * @param x2  second control Point of cubic on x-axis
     * @param y2  second control Point of cubic on y-axis
     * @param x3  end Point of cubic on x-axis
     * @param y3  end Point of cubic on y-axis
     * @return    reference to Path
     */
    fun cubicTo(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float): Path {
        Stats.onNativeCall()
        _nCubicTo(_ptr, x1, y1, x2, y2, x3, y3)
        return this
    }

    /**
     *
     * Adds cubic from last point towards Point p1, then towards Point p2, ending at
     * Point p3. If Path is empty, or last [PathVerb] is [PathVerb.CLOSE], last point is set to
     * (0, 0) before adding cubic.
     *
     *
     * Appends [PathVerb.MOVE] to verb array and (0, 0) to Point array, if needed;
     * then appends [PathVerb.CUBIC] to verb array; and Point p1, p2, p3
     * to Point array.
     *
     * @param p1  first control Point of cubic
     * @param p2  second control Point of cubic
     * @param p3  end Point of cubic
     * @return    reference to Path
     */
    fun cubicTo(p1: Point, p2: Point, p3: Point): Path {
        return cubicTo(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y)
    }

    /**
     *
     * Adds cubic from last point towards vector (dx1, dy1), then towards
     * vector (dx2, dy2), to vector (dx3, dy3).
     * If Path is empty, or last [PathVerb]
     * is [PathVerb.CLOSE], last point is set to (0, 0) before adding cubic.
     *
     *
     * Appends [PathVerb.MOVE] to verb array and (0, 0) to Point array,
     * if needed; then appends [PathVerb.CUBIC] to verb array; and appends cubic
     * control and cubic end to Point array.
     *
     *
     * Cubic control is last point plus vector (dx1, dy1).
     *
     *
     * Cubic end is last point plus vector (dx2, dy2).
     *
     *
     * Function name stands for "relative cubic to".
     *
     * @param dx1  offset from last point to first cubic control on x-axis
     * @param dy1  offset from last point to first cubic control on y-axis
     * @param dx2  offset from last point to second cubic control on x-axis
     * @param dy2  offset from last point to second cubic control on y-axis
     * @param dx3  offset from last point to cubic end on x-axis
     * @param dy3  offset from last point to cubic end on y-axis
     * @return    reference to Path
     */
    fun rCubicTo(dx1: Float, dy1: Float, dx2: Float, dy2: Float, dx3: Float, dy3: Float): Path {
        Stats.onNativeCall()
        _nRCubicTo(_ptr, dx1, dy1, dx2, dy2, dx3, dy3)
        return this
    }

    /**
     *
     * Appends arc to Path. Arc added is part of ellipse
     * bounded by oval, from startAngle through sweepAngle. Both startAngle and
     * sweepAngle are measured in degrees, where zero degrees is aligned with the
     * positive x-axis, and positive sweeps extends arc clockwise.
     *
     *
     * arcTo() adds line connecting Path last Point to initial arc Point if forceMoveTo
     * is false and Path is not empty. Otherwise, added contour begins with first point
     * of arc. Angles greater than -360 and less than 360 are treated modulo 360.
     *
     * @param oval         bounds of ellipse containing arc
     * @param startAngle   starting angle of arc in degrees
     * @param sweepAngle   sweep, in degrees. Positive is clockwise; treated modulo 360
     * @param forceMoveTo  true to start a new contour with arc
     * @return             reference to Path
     *
     * @see [https://fiddle.skia.org/c/@Path_arcTo](https://fiddle.skia.org/c/@Path_arcTo)
     */
    fun arcTo(oval: Rect, startAngle: Float, sweepAngle: Float, forceMoveTo: Boolean): Path {
        Stats.onNativeCall()
        _nArcTo(_ptr, oval.left, oval.top, oval.right, oval.bottom, startAngle, sweepAngle, forceMoveTo)
        return this
    }

    /**
     *
     * Appends arc to Path, after appending line if needed. Arc is implemented by conic
     * weighted to describe part of circle. Arc is contained by tangent from
     * last Path point to (x1, y1), and tangent from (x1, y1) to (x2, y2). Arc
     * is part of circle sized to radius, positioned so it touches both tangent lines.
     *
     *
     * If last Path Point does not start Arc, tangentArcTo appends connecting Line to Path.
     * The length of Vector from (x1, y1) to (x2, y2) does not affect Arc.
     *
     *
     * Arc sweep is always less than 180 degrees. If radius is zero, or if
     * tangents are nearly parallel, tangentArcTo appends Line from last Path Point to (x1, y1).
     *
     *
     * tangentArcTo appends at most one Line and one conic.
     *
     *
     * tangentArcTo implements the functionality of PostScript arct and HTML Canvas tangentArcTo.
     *
     * @param x1      x-axis value common to pair of tangents
     * @param y1      y-axis value common to pair of tangents
     * @param x2      x-axis value end of second tangent
     * @param y2      y-axis value end of second tangent
     * @param radius  distance from arc to circle center
     * @return        reference to Path
     *
     * @see [https://fiddle.skia.org/c/@Path_arcTo_2_a](https://fiddle.skia.org/c/@Path_arcTo_2_a)
     *
     * @see [https://fiddle.skia.org/c/@Path_arcTo_2_b](https://fiddle.skia.org/c/@Path_arcTo_2_b)
     *
     * @see [https://fiddle.skia.org/c/@Path_arcTo_2_c](https://fiddle.skia.org/c/@Path_arcTo_2_c)
     */
    fun tangentArcTo(x1: Float, y1: Float, x2: Float, y2: Float, radius: Float): Path {
        Stats.onNativeCall()
        _nTangentArcTo(_ptr, x1, y1, x2, y2, radius)
        return this
    }

    /**
     *
     * Appends arc to Path, after appending line if needed. Arc is implemented by conic
     * weighted to describe part of circle. Arc is contained by tangent from
     * last Path point to p1, and tangent from p1 to p2. Arc
     * is part of circle sized to radius, positioned so it touches both tangent lines.
     *
     *
     * If last Path Point does not start arc, tangentArcTo() appends connecting line to Path.
     * The length of vector from p1 to p2 does not affect arc.
     *
     *
     * Arc sweep is always less than 180 degrees. If radius is zero, or if
     * tangents are nearly parallel, tangentArcTo() appends line from last Path Point to p1.
     *
     *
     * tangentArcTo() appends at most one line and one conic.
     *
     *
     * tangentArcTo() implements the functionality of PostScript arct and HTML Canvas tangentArcTo.
     *
     * @param p1      Point common to pair of tangents
     * @param p2      end of second tangent
     * @param radius  distance from arc to circle center
     * @return        reference to Path
     */
    fun tangentArcTo(p1: Point, p2: Point, radius: Float): Path {
        return tangentArcTo(p1.x, p1.y, p2.x, p2.y, radius)
    }

    /**
     *
     *Appends arc to Path. Arc is implemented by one or more conics weighted to
     * describe part of oval with radii (rx, ry) rotated by xAxisRotate degrees. Arc
     * curves from last Path Point to (x, y), choosing one of four possible routes:
     * clockwise or counterclockwise, and smaller or larger.
     *
     *
     * Arc sweep is always less than 360 degrees. ellipticalArcTo() appends line to (x, y) if
     * either radii are zero, or if last Path Point equals (x, y). ellipticalArcTo() scales radii
     * (rx, ry) to fit last Path Point and (x, y) if both are greater than zero but
     * too small.
     *
     *
     * ellipticalArcTo() appends up to four conic curves.
     *
     *
     * ellipticalArcTo() implements the functionality of SVG arc, although SVG sweep-flag value
     * is opposite the integer value of sweep; SVG sweep-flag uses 1 for clockwise,
     * while [PathDirection.CLOCKWISE] cast to int is zero.
     *
     * @param rx           radius on x-axis before x-axis rotation
     * @param ry           radius on y-axis before x-axis rotation
     * @param xAxisRotate  x-axis rotation in degrees; positive values are clockwise
     * @param arc          chooses smaller or larger arc
     * @param direction    chooses clockwise or counterclockwise arc
     * @param x            end of arc
     * @param y            end of arc
     * @return             reference to Path
     */
    fun ellipticalArcTo(
        rx: Float,
        ry: Float,
        xAxisRotate: Float,
        arc: PathEllipseArc,
        direction: PathDirection,
        x: Float,
        y: Float
    ): Path {
        Stats.onNativeCall()
        _nEllipticalArcTo(_ptr, rx, ry, xAxisRotate, arc.ordinal, direction.ordinal, x, y)
        return this
    }

    /**
     *
     * Appends arc to Path. Arc is implemented by one or more conic weighted to describe
     * part of oval with radii (r.fX, r.fY) rotated by xAxisRotate degrees. Arc curves
     * from last Path Point to (xy.fX, xy.fY), choosing one of four possible routes:
     * clockwise or counterclockwise, and smaller or larger.
     *
     *
     * Arc sweep is always less than 360 degrees. ellipticalArcTo() appends line to xy if either
     * radii are zero, or if last Path Point equals (xy.fX, xy.fY). ellipticalArcTo() scales radii r to
     * fit last Path Point and xy if both are greater than zero but too small to describe
     * an arc.
     *
     *
     * ellipticalArcTo() appends up to four conic curves.
     *
     *
     * ellipticalArcTo() implements the functionality of SVG arc, although SVG sweep-flag value is
     * opposite the integer value of sweep; SVG sweep-flag uses 1 for clockwise, while
     * [PathDirection.CLOCKWISE] cast to int is zero.
     *
     * @param r            radii on axes before x-axis rotation
     * @param xAxisRotate  x-axis rotation in degrees; positive values are clockwise
     * @param arc          chooses smaller or larger arc
     * @param direction    chooses clockwise or counterclockwise arc
     * @param xy           end of arc
     * @return             reference to Path
     */
    fun ellipticalArcTo(r: Point, xAxisRotate: Float, arc: PathEllipseArc, direction: PathDirection, xy: Point): Path {
        return ellipticalArcTo(r.x, r.y, xAxisRotate, arc, direction, xy.x, xy.y)
    }

    /**
     *
     * Appends arc to Path, relative to last Path Point. Arc is implemented by one or
     * more conic, weighted to describe part of oval with radii (rx, ry) rotated by
     * xAxisRotate degrees. Arc curves from last Path Point to relative end Point:
     * (dx, dy), choosing one of four possible routes: clockwise or
     * counterclockwise, and smaller or larger. If Path is empty, the start arc Point
     * is (0, 0).
     *
     *
     * Arc sweep is always less than 360 degrees. rEllipticalArcTo() appends line to end Point
     * if either radii are zero, or if last Path Point equals end Point.
     * rEllipticalArcTo() scales radii (rx, ry) to fit last Path Point and end Point if both are
     * greater than zero but too small to describe an arc.
     *
     *
     * rEllipticalArcTo() appends up to four conic curves.
     *
     *
     * rEllipticalArcTo() implements the functionality of svg arc, although SVG "sweep-flag" value is
     * opposite the integer value of sweep; SVG "sweep-flag" uses 1 for clockwise, while
     * [PathDirection.CLOCKWISE] cast to int is zero.
     *
     * @param rx           radius before x-axis rotation
     * @param ry           radius before x-axis rotation
     * @param xAxisRotate  x-axis rotation in degrees; positive values are clockwise
     * @param arc          chooses smaller or larger arc
     * @param direction    chooses clockwise or counterclockwise arc
     * @param dx           x-axis offset end of arc from last Path Point
     * @param dy           y-axis offset end of arc from last Path Point
     * @return             reference to Path
     */
    fun rEllipticalArcTo(
        rx: Float,
        ry: Float,
        xAxisRotate: Float,
        arc: PathEllipseArc,
        direction: PathDirection,
        dx: Float,
        dy: Float
    ): Path {
        Stats.onNativeCall()
        _nREllipticalArcTo(_ptr, rx, ry, xAxisRotate, arc.ordinal, direction.ordinal, dx, dy)
        return this
    }

    /**
     *
     * Appends [PathVerb.CLOSE] to Path. A closed contour connects the first and last Point
     * with line, forming a continuous loop. Open and closed contour draw the same
     * with [PaintMode.FILL]. With [PaintMode.STROKE], open contour draws
     * [PaintStrokeCap] at contour start and end; closed contour draws
     * [PaintStrokeJoin] at contour start and end.
     *
     *
     * closePath() has no effect if Path is empty or last Path [PathVerb] is [PathVerb.CLOSE].
     *
     * @return  reference to Path
     *
     * @see [https://fiddle.skia.org/c/@Path_close](https://fiddle.skia.org/c/@Path_close)
     */
    fun closePath(): Path {
        Stats.onNativeCall()
        _nClosePath(_ptr)
        return this
    }

    /**
     *
     * Returns Rect if Path is equivalent to Rect when filled.
     *
     * rect may be smaller than the Path bounds. Path bounds may include [PathVerb.MOVE] points
     * that do not alter the area drawn by the returned rect.
     *
     * @return  bounds if Path contains Rect, null otherwise
     *
     * @see [https://fiddle.skia.org/c/@Path_isRect](https://fiddle.skia.org/c/@Path_isRect)
     */
    val isRect: Rect?
        get() = try {
            Stats.onNativeCall()
            Rect.fromInteropPointerNullable { _nIsRect(_ptr, it) }
        } finally {
            reachabilityBarrier(this)
        }
    /**
     * Adds Rect to Path, appending [PathVerb.MOVE], three [PathVerb.LINE], and [PathVerb.CLOSE].
     * If dir is [PathDirection.CLOCKWISE], Rect corners are added clockwise; if dir is
     * [PathDirection.COUNTER_CLOCKWISE], Rect corners are added counterclockwise.
     * start determines the first corner added.
     *
     * @param rect   Rect to add as a closed contour
     * @param dir    Direction to wind added contour
     * @param start  initial corner of Rect to add. 0 for top left, 1 for top right, 2 for lower right, 3 for lower left
     * @return       reference to Path
     *
     * @see [https://fiddle.skia.org/c/@Path_addRect_2](https://fiddle.skia.org/c/@Path_addRect_2)
     */
    /**
     * Adds Rect to Path, appending [PathVerb.MOVE], three [PathVerb.LINE], and [PathVerb.CLOSE],
     * starting with top-left corner of Rect; followed by top-right, bottom-right,
     * and bottom-left.
     *
     * @param rect  Rect to add as a closed contour
     * @return      reference to Path
     *
     * @see [https://fiddle.skia.org/c/@Path_addRect](https://fiddle.skia.org/c/@Path_addRect)
     */
    /**
     * Adds Rect to Path, appending [PathVerb.MOVE], three [PathVerb.LINE], and [PathVerb.CLOSE],
     * starting with top-left corner of Rect; followed by top-right, bottom-right,
     * and bottom-left if dir is [PathDirection.CLOCKWISE]; or followed by bottom-left,
     * bottom-right, and top-right if dir is [PathDirection.COUNTER_CLOCKWISE].
     *
     * @param rect  Rect to add as a closed contour
     * @param dir   Direction to wind added contour
     * @return      reference to Path
     *
     * @see [https://fiddle.skia.org/c/@Path_addRect](https://fiddle.skia.org/c/@Path_addRect)
     */
    fun addRect(rect: Rect, dir: PathDirection = PathDirection.CLOCKWISE, start: Int = 0): Path {
        Stats.onNativeCall()
        _nAddRect(_ptr, rect.left, rect.top, rect.right, rect.bottom, dir.ordinal, start)
        return this
    }
    /**
     * Adds oval to Path, appending [PathVerb.MOVE], four [PathVerb.CONIC], and [PathVerb.CLOSE].
     * Oval is upright ellipse bounded by Rect oval with radii equal to half oval width
     * and half oval height. Oval begins at start and continues
     * clockwise if dir is [PathDirection.CLOCKWISE], counterclockwise if dir is [PathDirection.COUNTER_CLOCKWISE].
     *
     * @param oval   bounds of ellipse added
     * @param dir    Direction to wind ellipse
     * @param start  index of initial point of ellipse. 0 for top, 1 for right, 2 for bottom, 3 for left
     * @return       reference to Path
     *
     * @see [https://fiddle.skia.org/c/@Path_addOval_2](https://fiddle.skia.org/c/@Path_addOval_2)
     */
    /**
     *
     * Adds oval to path, appending [PathVerb.MOVE], four [PathVerb.CONIC], and [PathVerb.CLOSE].
     *
     *
     * Oval is upright ellipse bounded by Rect oval with radii equal to half oval width
     * and half oval height. Oval begins at (oval.fRight, oval.centerY()) and continues
     * clockwise.
     *
     * @param oval  bounds of ellipse added
     * @return      reference to Path
     *
     * @see [https://fiddle.skia.org/c/@Path_addOval](https://fiddle.skia.org/c/@Path_addOval)
     */
    /**
     *
     * Adds oval to path, appending [PathVerb.MOVE], four [PathVerb.CONIC], and [PathVerb.CLOSE].
     *
     *
     * Oval is upright ellipse bounded by Rect oval with radii equal to half oval width
     * and half oval height. Oval begins at (oval.fRight, oval.centerY()) and continues
     * clockwise if dir is [PathDirection.CLOCKWISE], counterclockwise if dir is [PathDirection.COUNTER_CLOCKWISE].
     *
     * @param oval  bounds of ellipse added
     * @param dir   Direction to wind ellipse
     * @return      reference to Path
     *
     * @see [https://fiddle.skia.org/c/@Path_addOval](https://fiddle.skia.org/c/@Path_addOval)
     */
    fun addOval(oval: Rect, dir: PathDirection = PathDirection.CLOCKWISE, start: Int = 1): Path {
        Stats.onNativeCall()
        _nAddOval(_ptr, oval.left, oval.top, oval.right, oval.bottom, dir.ordinal, start)
        return this
    }
    /**
     *
     * Adds circle centered at (x, y) of size radius to Path, appending [PathVerb.MOVE],
     * four [PathVerb.CONIC], and [PathVerb.CLOSE]. Circle begins at: (x + radius, y), continuing
     * clockwise if dir is [PathDirection.CLOCKWISE], and counterclockwise if dir is [PathDirection.COUNTER_CLOCKWISE].
     *
     *
     * Has no effect if radius is zero or negative.
     *
     * @param x       center of circle
     * @param y       center of circle
     * @param radius  distance from center to edge
     * @param dir     Direction to wind circle
     * @return        reference to Path
     */
    /**
     *
     * Adds circle centered at (x, y) of size radius to Path, appending [PathVerb.MOVE],
     * four [PathVerb.CONIC], and [PathVerb.CLOSE]. Circle begins at: (x + radius, y)
     *
     *
     * Has no effect if radius is zero or negative.
     *
     * @param x       center of circle
     * @param y       center of circle
     * @param radius  distance from center to edge
     * @return        reference to Path
     */
    fun addCircle(x: Float, y: Float, radius: Float, dir: PathDirection = PathDirection.CLOCKWISE): Path {
        Stats.onNativeCall()
        _nAddCircle(_ptr, x, y, radius, dir.ordinal)
        return this
    }

    /**
     *
     * Appends arc to Path, as the start of new contour. Arc added is part of ellipse
     * bounded by oval, from startAngle through sweepAngle. Both startAngle and
     * sweepAngle are measured in degrees, where zero degrees is aligned with the
     * positive x-axis, and positive sweeps extends arc clockwise.
     *
     *
     * If sweepAngle  -360, or sweepAngle  360; and startAngle modulo 90 is nearly
     * zero, append oval instead of arc. Otherwise, sweepAngle values are treated
     * modulo 360, and arc may or may not draw depending on numeric rounding.
     *
     * @param oval        bounds of ellipse containing arc
     * @param startAngle  starting angle of arc in degrees
     * @param sweepAngle  sweep, in degrees. Positive is clockwise; treated modulo 360
     * @return            reference to Path
     *
     * @see [https://fiddle.skia.org/c/@Path_addArc](https://fiddle.skia.org/c/@Path_addArc)
     */
    fun addArc(oval: Rect, startAngle: Float, sweepAngle: Float): Path {
        Stats.onNativeCall()
        _nAddArc(_ptr, oval.left, oval.top, oval.right, oval.bottom, startAngle, sweepAngle)
        return this
    }

    /**
     *
     * Adds rrect to Path, creating a new closed contour. If dir is [PathDirection.CLOCKWISE], rrect
     * winds clockwise; if dir is [PathDirection.COUNTER_CLOCKWISE], rrect winds counterclockwise.
     * start determines the first point of rrect to add.
     *
     * @param rrect  bounds and radii of rounded rectangle
     * @param dir    Direction to wind RRect
     * @param start  index of initial point of RRect. 0 for top-right end of the arc at top left,
     * 1 for top-left end of the arc at top right, 2 for bottom-right end of top right arc, etc.
     * @return       reference to Path
     *
     * @see [https://fiddle.skia.org/c/@Path_addRRect_2](https://fiddle.skia.org/c/@Path_addRRect_2)
     */
    /**
     *
     * Adds rrect to Path, creating a new closed contour. RRect starts at top-left of the lower-left corner and
     * winds clockwise.
     *
     *
     * After appending, Path may be empty, or may contain: Rect, Oval, or RRect.
     *
     * @param rrect  bounds and radii of rounded rectangle
     * @return       reference to Path
     *
     * @see [https://fiddle.skia.org/c/@Path_addRRect](https://fiddle.skia.org/c/@Path_addRRect)
     */
    fun addRRect(rrect: RRect, dir: PathDirection = PathDirection.CLOCKWISE, start: Int = 6): Path {
        Stats.onNativeCall()
        interopScope {
            _nAddRRect(_ptr, rrect.left, rrect.top, rrect.right, rrect.bottom, toInterop(rrect.radii), rrect.radii.size, dir.ordinal, start)
        }
        return this
    }

    /**
     *
     * Adds contour created from line array, adding (pts.length - 1) line segments.
     * Contour added starts at pts[0], then adds a line for every additional Point
     * in pts array. If close is true, appends [PathVerb.CLOSE] to Path, connecting
     * pts[pts.length - 1] and pts[0].
     *
     *
     * If pts is empty, append [PathVerb.MOVE] to path.
     *
     * @param pts    array of line sharing end and start Point
     * @param close  true to add line connecting contour end and start
     * @return       reference to Path
     *
     * @see [https://fiddle.skia.org/c/@Path_addPoly](https://fiddle.skia.org/c/@Path_addPoly)
     */
    fun addPoly(pts: Array<Point>, close: Boolean): Path {
        val flat = FloatArray(pts.size * 2)
        for (i in pts.indices) {
            flat[i * 2] = pts[i].x
            flat[i * 2 + 1] = pts[i].y
        }
        return addPoly(flat, close)
    }

    /**
     *
     * Adds contour created from line array, adding (pts.length / 2 - 1) line segments.
     * Contour added starts at (pts[0], pts[1]), then adds a line for every additional pair of floats
     * in pts array. If close is true, appends [PathVerb.CLOSE] to Path, connecting
     * (pts[count - 2], pts[count - 1]) and (pts[0], pts[1]).
     *
     *
     * If pts is empty, append [PathVerb.MOVE] to path.
     *
     * @param pts    flat array of line sharing end and start Point
     * @param close  true to add line connecting contour end and start
     * @return       reference to Path
     *
     * @see [https://fiddle.skia.org/c/@Path_addPoly](https://fiddle.skia.org/c/@Path_addPoly)
     */
    fun addPoly(pts: FloatArray, close: Boolean): Path {
        require(pts.size % 2 == 0) { "Expected even amount of pts, got " + pts.size }
        Stats.onNativeCall()
        interopScope {
            _nAddPoly(_ptr, toInterop(pts), pts.size / 2, close)
        }
        return this
    }
    /**
     *
     * Appends src to Path.
     *
     *
     * If extend is false, src verb array, Point array, and conic weights are
     * added unaltered. If extend is true, add line before appending
     * verbs, Point, and conic weights.
     *
     * @param src     Path verbs, Point, and conic weights to add
     * @param extend  if should add a line before appending verbs
     * @return        reference to Path
     */
    /**
     *
     * Appends src to Path.
     *
     *
     * src verb array, Point array, and conic weights are
     * added unaltered.
     *
     * @param src  Path verbs, Point, and conic weights to add
     * @return     reference to Path
     */
    fun addPath(src: Path?, extend: Boolean = false): Path {
        return try {
            Stats.onNativeCall()
            _nAddPath(
                _ptr,
                getPtr(src),
                extend
            )
            this
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(src)
        }
    }
    /**
     *
     * Appends src to Path, offset by (dx, dy).
     *
     *
     * If extend is false, src verb array, Point array, and conic weights are
     * added unaltered. If extend is true, add line before appending
     * verbs, Point, and conic weights.
     *
     * @param src     Path verbs, Point, and conic weights to add
     * @param dx      offset added to src Point array x-axis coordinates
     * @param dy      offset added to src Point array y-axis coordinates
     * @param extend  if should add a line before appending verbs
     * @return        reference to Path
     */
    /**
     *
     * Appends src to Path, offset by (dx, dy).
     *
     *
     * Src verb array, Point array, and conic weights are
     * added unaltered.
     *
     * @param src     Path verbs, Point, and conic weights to add
     * @param dx      offset added to src Point array x-axis coordinates
     * @param dy      offset added to src Point array y-axis coordinates
     * @return        reference to Path
     */
    fun addPath(src: Path?, dx: Float, dy: Float, extend: Boolean = false): Path {
        return try {
            Stats.onNativeCall()
            _nAddPathOffset(
                _ptr,
                getPtr(src),
                dx,
                dy,
                extend
            )
            this
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(src)
        }
    }
    /**
     *
     * Appends src to Path, transformed by matrix. Transformed curves may have different
     * verbs, Point, and conic weights.
     *
     *
     * If extend is false, src verb array, Point array, and conic weights are
     * added unaltered. If extend is true, add line before appending
     * verbs, Point, and conic weights.
     *
     * @param src     Path verbs, Point, and conic weights to add
     * @param matrix  transform applied to src
     * @param extend  if should add a line before appending verbs
     * @return        reference to Path
     */
    /**
     *
     * Appends src to Path, transformed by matrix. Transformed curves may have different
     * verbs, Point, and conic weights.
     *
     *
     * Src verb array, Point array, and conic weights are
     * added unaltered.
     *
     * @param src     Path verbs, Point, and conic weights to add
     * @param matrix  transform applied to src
     * @return        reference to Path
     */
    fun addPath(src: Path?, matrix: Matrix33, extend: Boolean = false): Path {
        return try {
            Stats.onNativeCall()
            interopScope {
                _nAddPathTransform(
                    _ptr,
                    getPtr(src),
                    toInterop(matrix.mat),
                    extend
                )
            }
            this
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(src)
        }
    }

    /**
     * Appends src to Path, from back to front.
     * Reversed src always appends a new contour to Path.
     *
     * @param src  Path verbs, Point, and conic weights to add
     * @return     reference to Path
     *
     * @see [https://fiddle.skia.org/c/@Path_reverseAddPath](https://fiddle.skia.org/c/@Path_reverseAddPath)
     */
    fun reverseAddPath(src: Path?): Path {
        return try {
            Stats.onNativeCall()
            _nReverseAddPath(_ptr, getPtr(src))
            this
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(src)
        }
    }
    /**
     * Offsets Point array by (dx, dy). Offset Path replaces dst.
     * If dst is null, Path is replaced by offset data.
     *
     * @param dx   offset added to Point array x-axis coordinates
     * @param dy   offset added to Point array y-axis coordinates
     * @param dst  overwritten, translated copy of Path; may be null
     * @return     this
     *
     * @see [https://fiddle.skia.org/c/@Path_offset](https://fiddle.skia.org/c/@Path_offset)
     */
    /**
     * Offsets Point array by (dx, dy). Path is replaced by offset data.
     *
     * @param dx  offset added to Point array x-axis coordinates
     * @param dy  offset added to Point array y-axis coordinates
     * @return    this
     */
    fun offset(dx: Float, dy: Float, dst: Path? = null): Path {
        return try {
            Stats.onNativeCall()
            _nOffset(_ptr, dx, dy, getPtr(dst))
            this
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(dst)
        }
    }

    /**
     * Transforms verb array, Point array, and weight by matrix.
     * transform may change verbs and increase their number.
     * Path is replaced by transformed data.
     *
     * @param matrix                matrix to apply to Path
     * @param applyPerspectiveClip  whether to apply perspective clipping
     * @return                      this
     */
    fun transform(matrix: Matrix33, applyPerspectiveClip: Boolean): Path {
        return transform(matrix, null, applyPerspectiveClip)
    }
    /**
     * Transforms verb array, Point array, and weight by matrix.
     * transform may change verbs and increase their number.
     * Transformed Path replaces dst; if dst is null, original data
     * is replaced.
     *
     * @param matrix                matrix to apply to Path
     * @param dst                   overwritten, transformed copy of Path; may be null
     * @param applyPerspectiveClip  whether to apply perspective clipping
     * @return                      this
     *
     * @see [https://fiddle.skia.org/c/@Path_transform](https://fiddle.skia.org/c/@Path_transform)
     */
    /**
     * Transforms verb array, Point array, and weight by matrix.
     * transform may change verbs and increase their number.
     * Path is replaced by transformed data.
     *
     * @param matrix  matrix to apply to Path
     * @return  this
     */
    /**
     * Transforms verb array, Point array, and weight by matrix.
     * transform may change verbs and increase their number.
     * Transformed Path replaces dst; if dst is null, original data
     * is replaced.
     *
     * @param matrix  matrix to apply to Path
     * @param dst     overwritten, transformed copy of Path; may be null
     * @return        this
     *
     * @see [https://fiddle.skia.org/c/@Path_transform](https://fiddle.skia.org/c/@Path_transform)
     */
    fun transform(matrix: Matrix33, dst: Path? = null, applyPerspectiveClip: Boolean = true): Path {
        return try {
            Stats.onNativeCall()
            interopScope {
                _nTransform(
                    _ptr,
                    toInterop(matrix.mat),
                    getPtr(dst),
                    applyPerspectiveClip
                )
            }
            this
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(dst)
        }
    }

    /**
     * Returns last point on Path in lastPt. Returns null if Point array is empty.
     *
     * @return        point if Point array contains one or more Point, null otherwise
     *
     * @see [https://fiddle.skia.org/c/@Path_getLastPt](https://fiddle.skia.org/c/@Path_getLastPt)
     */
    var lastPt: Point
        get() = try {
            Stats.onNativeCall()
            Point.fromInteropPointer { _nGetLastPt(_ptr, it) }
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setLastPt(value.x, value.y)
        }

    /**
     * Sets last point to (x, y). If Point array is empty, append [PathVerb.MOVE] to
     * verb array and append (x, y) to Point array.
     *
     * @param x  set x-axis value of last point
     * @param y  set y-axis value of last point
     * @return   this
     *
     * @see [https://fiddle.skia.org/c/@Path_setLastPt](https://fiddle.skia.org/c/@Path_setLastPt)
     */
    fun setLastPt(x: Float, y: Float): Path {
        Stats.onNativeCall()
        _nSetLastPt(_ptr, x, y)
        return this
    }

    /**
     *
     * Returns a mask, where each set bit corresponds to a SegmentMask constant
     * if Path contains one or more verbs of that type.
     *
     *
     * Returns zero if Path contains no lines, or curves: quads, conics, or cubics.
     *
     *
     * getSegmentMasks() returns a cached result; it is very fast.
     *
     * @return  SegmentMask bits or zero
     *
     * @see PathSegmentMask.LINE
     *
     * @see PathSegmentMask.QUAD
     *
     * @see PathSegmentMask.CONIC
     *
     * @see PathSegmentMask.CUBIC
     */
    val segmentMasks: Int
        get() = try {
            Stats.onNativeCall()
            _nGetSegmentMasks(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    override fun iterator(): PathSegmentIterator {
        return iterator(false)
    }

    fun iterator(forceClose: Boolean): PathSegmentIterator {
        return PathSegmentIterator.make(this, forceClose)
    }

    /**
     * Returns true if the point (x, y) is contained by Path, taking into
     * account [PathFillMode].
     *
     * @param x  x-axis value of containment test
     * @param y  y-axis value of containment test
     * @return   true if Point is in Path
     *
     * @see [https://fiddle.skia.org/c/@Path_contains](https://fiddle.skia.org/c/@Path_contains)
     */
    fun contains(x: Float, y: Float): Boolean {
        return try {
            Stats.onNativeCall()
            _nContains(_ptr, x, y)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Returns true if the point is contained by Path, taking into
     * account [PathFillMode].
     *
     * @param p  point of containment test
     * @return   true if Point is in Path
     *
     * @see [https://fiddle.skia.org/c/@Path_contains](https://fiddle.skia.org/c/@Path_contains)
     */
    operator fun contains(p: Point): Boolean {
        return contains(p.x, p.y)
    }

    /**
     * Writes text representation of Path to standard output. The representation may be
     * directly compiled as C++ code. Floating point values are written
     * with limited precision; it may not be possible to reconstruct original Path
     * from output.
     *
     * @return  this
     *
     * @see [https://fiddle.skia.org/c/@Path_dump_2](https://fiddle.skia.org/c/@Path_dump_2)
     */
    fun dump(): Path {
        Stats.onNativeCall()
        _nDump(_ptr)
        return this
    }

    /**
     *
     * Writes text representation of Path to standard output. The representation may be
     * directly compiled as C++ code. Floating point values are written
     * in hexadecimal to preserve their exact bit pattern. The output reconstructs the
     * original Path.
     *
     *
     * Use instead of [] when submitting
     *
     * @return  this
     *
     * @see [https://fiddle.skia.org/c/@Path_dumpHex](https://fiddle.skia.org/c/@Path_dumpHex)
     */
    fun dumpHex(): Path {
        Stats.onNativeCall()
        _nDumpHex(_ptr)
        return this
    }

    /**
     *
     * Writes Path to byte buffer.
     *
     *
     * Writes [PathFillMode], verb array, Point array, conic weight, and
     * additionally writes computed information like path convexity and bounds.
     *
     *
     * Use only be used in concert with [];
     * the format used for Path in memory is not guaranteed.
     *
     * @return  serialized Path; length always a multiple of 4
     *
     * @see [https://fiddle.skia.org/c/@Path_writeToMemory](https://fiddle.skia.org/c/@Path_writeToMemory)
     */
    fun serializeToBytes(): ByteArray {
        return try {
            Stats.onNativeCall()
            val size = interopScope { _nSerializeToBytes(_ptr, toInterop(null as ByteArray?)) }
            if (size == -1) {
                throw Error("Path is too big")
            }
            withResult(ByteArray(size)) {
                _nSerializeToBytes(_ptr, it)
            }
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     *
     * Returns a non-zero, globally unique value. A different value is returned
     * if verb array, Point array, or conic weight changes.
     *
     *
     * Setting [PathFillMode] does not change generation identifier.
     *
     *
     * Each time the path is modified, a different generation identifier will be returned.
     * [PathFillMode] does affect generation identifier on Android framework.
     *
     * @return  non-zero, globally unique value
     *
     * @see [https://fiddle.skia.org/c/@Path_getGenerationID](https://fiddle.skia.org/c/@Path_getGenerationID)
     *
     * @see [https://bugs.chromium.org/p/skia/issues/detail?id=1762](https://bugs.chromium.org/p/skia/issues/detail?id=1762)
     */
    val generationId: Int
        get() = try {
            Stats.onNativeCall()
            Path_nGetGenerationId(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * Returns if Path data is consistent. Corrupt Path data is detected if
     * internal values are out of range or internal storage does not match
     * array dimensions.
     *
     * @return  true if Path data is consistent
     */
    val isValid: Boolean
        get() = try {
            Stats.onNativeCall()
            _nIsValid(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
}


@ExternalSymbolName("org_jetbrains_skia_Path__1nGetFinalizer")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nGetFinalizer")
internal external fun Path_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Path__1nMake")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nMake")
private external fun Path_nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Path__1nEquals")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nEquals")
private external fun Path_nEquals(aPtr: NativePointer, bPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Path__1nReset")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nReset")
private external fun Path_nReset(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Path__1nIsVolatile")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nIsVolatile")
private external fun Path_nIsVolatile(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Path__1nSetVolatile")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nSetVolatile")
private external fun Path_nSetVolatile(ptr: NativePointer, isVolatile: Boolean)

@ExternalSymbolName("org_jetbrains_skia_Path__1nSwap")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nSwap")
private external fun Path_nSwap(ptr: NativePointer, otherPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Path__1nGetGenerationId")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nGetGenerationId")
private external fun Path_nGetGenerationId(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Path__1nMakeFromSVGString")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nMakeFromSVGString")
private external fun _nMakeFromSVGString(svg: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Path__1nIsInterpolatable")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nIsInterpolatable")
private external fun _nIsInterpolatable(ptr: NativePointer, comparePtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Path__1nMakeLerp")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nMakeLerp")
private external fun _nMakeLerp(ptr: NativePointer, endingPtr: NativePointer, weight: Float): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Path__1nGetFillMode")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nGetFillMode")
private external fun _nGetFillMode(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Path__1nSetFillMode")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nSetFillMode")
private external fun _nSetFillMode(ptr: NativePointer, fillMode: Int)

@ExternalSymbolName("org_jetbrains_skia_Path__1nIsConvex")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nIsConvex")
private external fun _nIsConvex(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Path__1nIsOval")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nIsOval")
private external fun _nIsOval(ptr: NativePointer, rect: InteropPointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Path__1nIsRRect")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nIsRRect")
private external fun _nIsRRect(ptr: NativePointer, rrect: InteropPointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Path__1nRewind")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nRewind")
private external fun _nRewind(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Path__1nIsEmpty")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nIsEmpty")
private external fun _nIsEmpty(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Path__1nIsLastContourClosed")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nIsLastContourClosed")
private external fun _nIsLastContourClosed(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Path__1nIsFinite")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nIsFinite")
private external fun _nIsFinite(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Path__1nIsLineDegenerate")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nIsLineDegenerate")
private external fun _nIsLineDegenerate(x0: Float, y0: Float, x1: Float, y1: Float, exact: Boolean): Boolean

@ExternalSymbolName("org_jetbrains_skia_Path__1nIsQuadDegenerate")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nIsQuadDegenerate")
private external fun _nIsQuadDegenerate(
    x0: Float,
    y0: Float,
    x1: Float,
    y1: Float,
    x2: Float,
    y2: Float,
    exact: Boolean
): Boolean


@ExternalSymbolName("org_jetbrains_skia_Path__1nIsCubicDegenerate")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nIsCubicDegenerate")
private external fun _nIsCubicDegenerate(
    x0: Float,
    y0: Float,
    x1: Float,
    y1: Float,
    x2: Float,
    y2: Float,
    x3: Float,
    y3: Float,
    exact: Boolean
): Boolean


@ExternalSymbolName("org_jetbrains_skia_Path__1nMaybeGetAsLine")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nMaybeGetAsLine")
private external fun _nMaybeGetAsLine(ptr: NativePointer, rectBuffer: InteropPointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Path__1nGetPointsCount")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nGetPointsCount")
private external fun _nGetPointsCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Path__1nGetPoint")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nGetPoint")
private external fun _nGetPoint(ptr: NativePointer, index: Int, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Path__1nGetPoints")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nGetPoints")
private external fun _nGetPoints(ptr: NativePointer, points: InteropPointer, max: Int): Int

@ExternalSymbolName("org_jetbrains_skia_Path__1nCountVerbs")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nCountVerbs")
private external fun _nCountVerbs(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Path__1nGetVerbs")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nGetVerbs")
private external fun _nGetVerbs(ptr: NativePointer, verbs: InteropPointer, max: Int): Int

@ExternalSymbolName("org_jetbrains_skia_Path__1nApproximateBytesUsed")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nApproximateBytesUsed")
private external fun _nApproximateBytesUsed(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Path__1nGetBounds")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nGetBounds")
private external fun _nGetBounds(ptr: NativePointer, rect: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Path__1nUpdateBoundsCache")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nUpdateBoundsCache")
private external fun _nUpdateBoundsCache(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Path__1nComputeTightBounds")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nComputeTightBounds")
private external fun _nComputeTightBounds(ptr: NativePointer, rect: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Path__1nConservativelyContainsRect")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nConservativelyContainsRect")
private external fun _nConservativelyContainsRect(ptr: NativePointer, l: Float, t: Float, r: Float, b: Float): Boolean

@ExternalSymbolName("org_jetbrains_skia_Path__1nIncReserve")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nIncReserve")
private external fun _nIncReserve(ptr: NativePointer, extraPtCount: Int)

@ExternalSymbolName("org_jetbrains_skia_Path__1nMoveTo")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nMoveTo")
private external fun _nMoveTo(ptr: NativePointer, x: Float, y: Float)

@ExternalSymbolName("org_jetbrains_skia_Path__1nRMoveTo")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nRMoveTo")
private external fun _nRMoveTo(ptr: NativePointer, dx: Float, dy: Float)

@ExternalSymbolName("org_jetbrains_skia_Path__1nLineTo")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nLineTo")
private external fun _nLineTo(ptr: NativePointer, x: Float, y: Float)

@ExternalSymbolName("org_jetbrains_skia_Path__1nRLineTo")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nRLineTo")
private external fun _nRLineTo(ptr: NativePointer, dx: Float, dy: Float)

@ExternalSymbolName("org_jetbrains_skia_Path__1nQuadTo")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nQuadTo")
private external fun _nQuadTo(ptr: NativePointer, x1: Float, y1: Float, x2: Float, y2: Float)

@ExternalSymbolName("org_jetbrains_skia_Path__1nRQuadTo")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nRQuadTo")
private external fun _nRQuadTo(ptr: NativePointer, dx1: Float, dy1: Float, dx2: Float, dy2: Float)

@ExternalSymbolName("org_jetbrains_skia_Path__1nConicTo")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nConicTo")
private external fun _nConicTo(ptr: NativePointer, x1: Float, y1: Float, x2: Float, y2: Float, w: Float)

@ExternalSymbolName("org_jetbrains_skia_Path__1nRConicTo")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nRConicTo")
private external fun _nRConicTo(ptr: NativePointer, dx1: Float, dy1: Float, dx2: Float, dy2: Float, w: Float)

@ExternalSymbolName("org_jetbrains_skia_Path__1nCubicTo")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nCubicTo")
private external fun _nCubicTo(ptr: NativePointer, x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float)

@ExternalSymbolName("org_jetbrains_skia_Path__1nRCubicTo")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nRCubicTo")
private external fun _nRCubicTo(ptr: NativePointer, dx1: Float, dy1: Float, dx2: Float, dy2: Float, dx3: Float, dy3: Float)

@ExternalSymbolName("org_jetbrains_skia_Path__1nArcTo")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nArcTo")
private external fun _nArcTo(
    ptr: NativePointer,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    startAngle: Float,
    sweepAngle: Float,
    forceMoveTo: Boolean
)


@ExternalSymbolName("org_jetbrains_skia_Path__1nTangentArcTo")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nTangentArcTo")
private external fun _nTangentArcTo(ptr: NativePointer, x1: Float, y1: Float, x2: Float, y2: Float, radius: Float)

@ExternalSymbolName("org_jetbrains_skia_Path__1nEllipticalArcTo")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nEllipticalArcTo")
private external fun _nEllipticalArcTo(
    ptr: NativePointer,
    rx: Float,
    ry: Float,
    xAxisRotate: Float,
    size: Int,
    direction: Int,
    x: Float,
    y: Float
)


@ExternalSymbolName("org_jetbrains_skia_Path__1nREllipticalArcTo")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nREllipticalArcTo")
private external fun _nREllipticalArcTo(
    ptr: NativePointer,
    rx: Float,
    ry: Float,
    xAxisRotate: Float,
    size: Int,
    direction: Int,
    dx: Float,
    dy: Float
)


@ExternalSymbolName("org_jetbrains_skia_Path__1nClosePath")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nClosePath")
private external fun _nClosePath(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Path__1nConvertConicToQuads")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nConvertConicToQuads")
private external fun _nConvertConicToQuads(
    x0: Float,
    y0: Float,
    x1: Float,
    y1: Float,
    x2: Float,
    y2: Float,
    w: Float,
    pow2: Int,
    result: InteropPointer
): Int


@ExternalSymbolName("org_jetbrains_skia_Path__1nIsRect")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nIsRect")
private external fun _nIsRect(ptr: NativePointer, rect: InteropPointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Path__1nAddRect")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nAddRect")
private external fun _nAddRect(ptr: NativePointer, l: Float, t: Float, r: Float, b: Float, dir: Int, start: Int)

@ExternalSymbolName("org_jetbrains_skia_Path__1nAddOval")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nAddOval")
private external fun _nAddOval(ptr: NativePointer, l: Float, t: Float, r: Float, b: Float, dir: Int, start: Int)

@ExternalSymbolName("org_jetbrains_skia_Path__1nAddCircle")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nAddCircle")
private external fun _nAddCircle(ptr: NativePointer, x: Float, y: Float, r: Float, dir: Int)

@ExternalSymbolName("org_jetbrains_skia_Path__1nAddArc")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nAddArc")
private external fun _nAddArc(ptr: NativePointer, l: Float, t: Float, r: Float, b: Float, startAngle: Float, sweepAngle: Float)

@ExternalSymbolName("org_jetbrains_skia_Path__1nAddRRect")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nAddRRect")
private external fun _nAddRRect(
    ptr: NativePointer,
    l: Float,
    t: Float,
    r: Float,
    b: Float,
    radii: InteropPointer,
    size: Int,
    dir: Int,
    start: Int
)


@ExternalSymbolName("org_jetbrains_skia_Path__1nAddPoly")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nAddPoly")
private external fun _nAddPoly(ptr: NativePointer, coords: InteropPointer, count: Int, close: Boolean)

@ExternalSymbolName("org_jetbrains_skia_Path__1nAddPath")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nAddPath")
private external fun _nAddPath(ptr: NativePointer, srcPtr: NativePointer, extend: Boolean)

@ExternalSymbolName("org_jetbrains_skia_Path__1nAddPathOffset")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nAddPathOffset")
private external fun _nAddPathOffset(ptr: NativePointer, srcPtr: NativePointer, dx: Float, dy: Float, extend: Boolean)

@ExternalSymbolName("org_jetbrains_skia_Path__1nAddPathTransform")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nAddPathTransform")
private external fun _nAddPathTransform(ptr: NativePointer, srcPtr: NativePointer, matrix: InteropPointer, extend: Boolean)

@ExternalSymbolName("org_jetbrains_skia_Path__1nReverseAddPath")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nReverseAddPath")
private external fun _nReverseAddPath(ptr: NativePointer, srcPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Path__1nOffset")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nOffset")
private external fun _nOffset(ptr: NativePointer, dx: Float, dy: Float, dst: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Path__1nTransform")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nTransform")
private external fun _nTransform(ptr: NativePointer, matrix: InteropPointer, dst: NativePointer, applyPerspectiveClip: Boolean)

@ExternalSymbolName("org_jetbrains_skia_Path__1nGetLastPt")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nGetLastPt")
private external fun _nGetLastPt(ptr: NativePointer, result: InteropPointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Path__1nSetLastPt")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nSetLastPt")
private external fun _nSetLastPt(ptr: NativePointer, x: Float, y: Float)

@ExternalSymbolName("org_jetbrains_skia_Path__1nGetSegmentMasks")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nGetSegmentMasks")
private external fun _nGetSegmentMasks(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Path__1nContains")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nContains")
private external fun _nContains(ptr: NativePointer, x: Float, y: Float): Boolean

@ExternalSymbolName("org_jetbrains_skia_Path__1nDump")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nDump")
private external fun _nDump(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Path__1nDumpHex")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nDumpHex")
private external fun _nDumpHex(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Path__1nSerializeToBytes")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nSerializeToBytes")
private external fun _nSerializeToBytes(ptr: NativePointer, dst: InteropPointer): Int

@ExternalSymbolName("org_jetbrains_skia_Path__1nMakeCombining")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nMakeCombining")
private external fun _nMakeCombining(onePtr: NativePointer, twoPtr: NativePointer, op: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Path__1nMakeFromBytes")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nMakeFromBytes")
private external fun _nMakeFromBytes(data: InteropPointer, size: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Path__1nIsValid")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Path__1nIsValid")
private external fun _nIsValid(ptr: NativePointer): Boolean