package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.impl.withNullableResult

private fun makePath(
    path: Path?,
    forceClosed: Boolean,
    resScale: Float
): NativePointer {
    Stats.onNativeCall()
    return try {
        PathMeasure_nMakePath(getPtr(path), forceClosed, resScale)
    } finally {
        Stats.onNativeCall()
        reachabilityBarrier(path)
    }
}

class PathMeasure internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        init {
            staticLoad()
        }
    }

    constructor() : this(PathMeasure_nMake())
    /**
     *
     * Initialize the pathmeasure with the specified path. The parts of the path that are needed
     * are copied, so the client is free to modify/delete the path after this call.
     *
     *
     * resScale controls the precision of the measure. values &gt; 1 increase the
     * precision (and possible slow down the computation).
     */
    /**
     * Initialize the pathmeasure with the specified path. The parts of the path that are needed
     * are copied, so the client is free to modify/delete the path after this call.
     */
    /**
     * Initialize the pathmeasure with the specified path. The parts of the path that are needed
     * are copied, so the client is free to modify/delete the path after this call.
     */
    constructor(
        path: Path?,
        forceClosed: Boolean = false,
        resScale: Float = 1f
    ) : this(makePath(path, forceClosed, resScale))

    /**
     * Reset the pathmeasure with the specified path. The parts of the path that are needed
     * are copied, so the client is free to modify/delete the path after this call.
     */
    fun setPath(path: Path?, forceClosed: Boolean): PathMeasure {
        return try {
            Stats.onNativeCall()
            PathMeasure_nSetPath(_ptr, getPtr(path), forceClosed)
            this
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(path)
        }
    }

    /**
     * Return the total length of the current contour, or 0 if no path
     * is associated (e.g. resetPath(null))
     */
    val length: Float
        get() = try {
            Stats.onNativeCall()
            PathMeasure_nGetLength(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * Pins distance to 0 &lt;= distance &lt;= getLength(), and then computes
     * the corresponding position.
     *
     * @return  null if there is no path, or a zero-length path was specified.
     */
    fun getPosition(distance: Float): Point? {
        return try {
            Stats.onNativeCall()
            withNullableResult(FloatArray(2)) { PathMeasure_nGetPosition(_ptr, distance, it) }?.let { points ->
                Point(points[0], points[1])
            }
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Pins distance to 0 &lt;= distance &lt;= getLength(), and then computes
     * the corresponding tangent.
     *
     * @return  null if there is no path, or a zero-length path was specified.
     */
    fun getTangent(distance: Float): Point? {
        return try {
            Stats.onNativeCall()
            withNullableResult(FloatArray(2)) { PathMeasure_nGetTangent(_ptr, distance, it) }?.let { points ->
                Point(points[0], points[1])
            }
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Pins distance to 0 &lt;= distance &lt;= getLength(), and then computes
     * the corresponding RSXform.
     *
     * @return  null if there is no path, or a zero-length path was specified.
     */
    fun getRSXform(distance: Float): RSXform? {
        return try {
            Stats.onNativeCall()
            withNullableResult(FloatArray(4)) {
                PathMeasure_nGetRSXform(_ptr, distance, it)
            }?.let { data ->
                RSXform(
                    data[0],
                    data[1],
                    data[2],
                    data[3]
                )
            }
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Pins distance to 0 &lt;= distance &lt;= getLength(), and then computes
     * the corresponding matrix (by calling getPosition/getTangent).
     *
     * @return  null if there is no path, or a zero-length path was specified.
     */
    fun getMatrix(distance: Float, getPosition: Boolean, getTangent: Boolean): Matrix33? {
        return try {
            Stats.onNativeCall()
            withNullableResult(FloatArray(9)) {
                PathMeasure_nGetMatrix(_ptr, distance, getPosition, getTangent, it)
            }?.let { data ->
                Matrix33(
                    data[0],
                    data[1],
                    data[2],
                    data[3],
                    data[4],
                    data[5],
                    data[6],
                    data[7],
                    data[8]
                )
            }
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Given a start and stop distance, return in dst the intervening segment(s).
     * If the segment is zero-length, return false, else return true.
     * startD and stopD are pinned to legal values (0..getLength()). If startD &gt; stopD
     * then return false (and leave dst untouched).
     * Begin the segment with a moveTo if startWithMoveTo is true
     */
    fun getSegment(startD: Float, endD: Float, dst: Path, startWithMoveTo: Boolean): Boolean {
        return try {
            Stats.onNativeCall()
            PathMeasure_nGetSegment(
                _ptr,
                startD,
                endD,
                getPtr(dst),
                startWithMoveTo
            )
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(dst)
        }
    }

    /**
     * @return  true if the current contour is closed.
     */
    override val isClosed: Boolean
        get() = try {
            Stats.onNativeCall()
            PathMeasure_nIsClosed(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * Move to the next contour in the path. Return true if one exists, or false if
     * we're done with the path.
     */
    fun nextContour(): Boolean {
        return try {
            Stats.onNativeCall()
            PathMeasure_nNextContour(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    internal object _FinalizerHolder {
        val PTR = PathMeasure_nGetFinalizer()
    }
}
