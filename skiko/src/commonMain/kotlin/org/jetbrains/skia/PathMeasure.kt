@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.getPtr
import kotlin.jvm.JvmStatic

class PathMeasure internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_PathMeasure__1nGetFinalizer")
        external fun _nGetFinalizer(): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_PathMeasure__1nMake")
        external fun _nMake(): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_PathMeasure__1nMakePath")
        external fun _nMakePath(pathPtr: NativePointer, forceClosed: Boolean, resScale: Float): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_PathMeasure__1nSetPath")
        external fun _nSetPath(ptr: NativePointer, pathPtr: NativePointer, forceClosed: Boolean)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_PathMeasure__1nGetLength")
        external fun _nGetLength(ptr: NativePointer): Float
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_PathMeasure__1nGetPosition")
        external fun _nGetPosition(ptr: NativePointer, distance: Float): Point?
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_PathMeasure__1nGetTangent")
        external fun _nGetTangent(ptr: NativePointer, distance: Float): Point?
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_PathMeasure__1nGetRSXform")
        external fun _nGetRSXform(ptr: NativePointer, distance: Float): RSXform?
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_PathMeasure__1nGetMatrix")
        external fun _nGetMatrix(ptr: NativePointer, distance: Float, getPosition: Boolean, getTangent: Boolean): FloatArray?
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_PathMeasure__1nGetSegment")
        external fun _nGetSegment(
            ptr: NativePointer,
            startD: Float,
            endD: Float,
            dstPtr: NativePointer,
            startWithMoveTo: Boolean
        ): Boolean

        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_PathMeasure__1nIsClosed")
        external fun _nIsClosed(ptr: NativePointer): Boolean
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_PathMeasure__1nNextContour")
        external fun _nNextContour(ptr: NativePointer): Boolean

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
    constructor(
        path: Path?,
        forceClosed: Boolean = false,
        resScale: Float = 1f
    ) : this(_nMakePath(getPtr(path), forceClosed, resScale)) {
        Stats.onNativeCall()
        reachabilityBarrier(path)
    }

    /**
     * Reset the pathmeasure with the specified path. The parts of the path that are needed
     * are copied, so the client is free to modify/delete the path after this call.
     */
    fun setPath(path: Path?, forceClosed: Boolean): PathMeasure {
        return try {
            Stats.onNativeCall()
            _nSetPath(_ptr, getPtr(path), forceClosed)
            this
        } finally {
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
            _nGetLength(_ptr)
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
            _nGetPosition(_ptr, distance)
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
            _nGetTangent(_ptr, distance)
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
            _nGetRSXform(_ptr, distance)
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
            val mat = _nGetMatrix(_ptr, distance, getPosition, getTangent)
            mat?.let { Matrix33(*it.copyOf()) }
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
            _nGetSegment(
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
            _nIsClosed(_ptr)
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
            _nNextContour(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    internal object _FinalizerHolder {
        val PTR = _nGetFinalizer()
    }
}