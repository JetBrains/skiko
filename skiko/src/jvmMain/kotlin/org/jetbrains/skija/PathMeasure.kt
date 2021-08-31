package org.jetbrains.skija

import org.jetbrains.skija.impl.Library.Companion.staticLoad
import org.jetbrains.skija.impl.Managed
import org.jetbrains.skija.impl.Native
import org.jetbrains.skija.impl.Stats
import java.lang.ref.Reference

class PathMeasure internal constructor(ptr: Long) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        @JvmStatic external fun _nGetFinalizer(): Long
        @JvmStatic external fun _nMake(): Long
        @JvmStatic external fun _nMakePath(pathPtr: Long, forceClosed: Boolean, resScale: Float): Long
        @JvmStatic external fun _nSetPath(ptr: Long, pathPtr: Long, forceClosed: Boolean)
        @JvmStatic external fun _nGetLength(ptr: Long): Float
        @JvmStatic external fun _nGetPosition(ptr: Long, distance: Float): Point?
        @JvmStatic external fun _nGetTangent(ptr: Long, distance: Float): Point?
        @JvmStatic external fun _nGetRSXform(ptr: Long, distance: Float): RSXform?
        @JvmStatic external fun _nGetMatrix(ptr: Long, distance: Float, getPosition: Boolean, getTangent: Boolean): FloatArray?
        @JvmStatic external fun _nGetSegment(
            ptr: Long,
            startD: Float,
            endD: Float,
            dstPtr: Long,
            startWithMoveTo: Boolean
        ): Boolean

        @JvmStatic external fun _nIsClosed(ptr: Long): Boolean
        @JvmStatic external fun _nNextContour(ptr: Long): Boolean

        init {
            staticLoad()
        }
    }

    constructor() : this(_nMake()) {
        Stats.onNativeCall()
    }
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
    @JvmOverloads
    constructor(
        path: Path?,
        forceClosed: Boolean = false,
        resScale: Float = 1f
    ) : this(_nMakePath(Native.Companion.getPtr(path), forceClosed, resScale)) {
        Stats.onNativeCall()
        Reference.reachabilityFence(path)
    }

    /**
     * Reset the pathmeasure with the specified path. The parts of the path that are needed
     * are copied, so the client is free to modify/delete the path after this call.
     */
    fun setPath(path: Path?, forceClosed: Boolean): PathMeasure {
        return try {
            Stats.onNativeCall()
            _nSetPath(_ptr, Native.Companion.getPtr(path), forceClosed)
            this
        } finally {
            Reference.reachabilityFence(path)
        }
    }

    /**
     * Return the total length of the current contour, or 0 if no path
     * is associated (e.g. resetPath(null))
     */
    val length: Float
        get() = try {
            Stats.onNativeCall()
            _nGetLength(_ptr)
        } finally {
            Reference.reachabilityFence(this)
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
            _nGetPosition(_ptr, distance)
        } finally {
            Reference.reachabilityFence(this)
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
            _nGetTangent(_ptr, distance)
        } finally {
            Reference.reachabilityFence(this)
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
            _nGetRSXform(_ptr, distance)
        } finally {
            Reference.reachabilityFence(this)
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
            val mat = _nGetMatrix(_ptr, distance, getPosition, getTangent)
            mat?.let { Matrix33(*it.clone()) }
        } finally {
            Reference.reachabilityFence(this)
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
            _nGetSegment(
                _ptr,
                startD,
                endD,
                Native.Companion.getPtr(dst),
                startWithMoveTo
            )
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(dst)
        }
    }

    /**
     * @return  true if the current contour is closed.
     */
    override val isClosed: Boolean
        get() = try {
            Stats.onNativeCall()
            _nIsClosed(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    /**
     * Move to the next contour in the path. Return true if one exists, or false if
     * we're done with the path.
     */
    fun nextContour(): Boolean {
        return try {
            Stats.onNativeCall()
            _nNextContour(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    internal object _FinalizerHolder {
        val PTR = _nGetFinalizer()
    }
}