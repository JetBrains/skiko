package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad

private fun makePath(
    path: Path?,
    forceClosed: Boolean,
    resScale: Float
): NativePointer {
    Stats.onNativeCall()
    return try {
        _nMakePath(getPtr(path), forceClosed, resScale)
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
            _nSetPath(_ptr, getPtr(path), forceClosed)
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
            withNullableResult(FloatArray(2)) { _nGetPosition(_ptr, distance, it) }?.let { points ->
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
            withNullableResult(FloatArray(2)) { _nGetTangent(_ptr, distance, it) }?.let { points ->
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
                _nGetRSXform(_ptr, distance, it)
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
                _nGetMatrix(_ptr, distance, getPosition, getTangent, it)
            }?.let { data ->
                Matrix33(data)
            }
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Given a start and stop distance, append to [dst] the intervening segment(s).
     * If the segment is zero-length, return false, else return true.
     * startD and endD are pinned to legal values (0..length). If startD &gt; endD
     * then return false (and leave [dst] untouched).
     * Begin the segment with a moveTo if startWithMoveTo is true
     */
    fun getSegment(startD: Float, endD: Float, dst: PathBuilder, startWithMoveTo: Boolean): Boolean {
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
        val PTR = PathMeasure_nGetFinalizer()
    }
}

// Runnable: _nGetFinalizer returns the address of deletePathMeasure; the delete runs via Managed__invokeFinalizer
@ExternalSymbolName("org_jetbrains_skia_PathMeasure__1nGetFinalizer")
private external fun PathMeasure_nGetFinalizer(): NativePointer

// Runnable: SkPathMeasure() default-constructs an empty SkContourMeasureIter and a null sk_sp
@ExternalSymbolName("org_jetbrains_skia_PathMeasure__1nMake")
private external fun PathMeasure_nMake(): NativePointer

// Native: SkPathMeasure ctor -> SkContourMeasureIter::next -> Impl::buildSegments subdivides the first contour
@ExternalSymbolName("org_jetbrains_skia_PathMeasure__1nMakePath")
@ForceNativeThreadState
private external fun _nMakePath(pathPtr: NativePointer, forceClosed: Boolean, resScale: Float): NativePointer

// Native: SkPathMeasure::setPath -> SkContourMeasureIter::next -> Impl::buildSegments subdivides the first contour
@ExternalSymbolName("org_jetbrains_skia_PathMeasure__1nSetPath")
@ForceNativeThreadState
private external fun _nSetPath(ptr: NativePointer, pathPtr: NativePointer, forceClosed: Boolean)

// Runnable: SkPathMeasure::getLength returns SkContourMeasure::fLength computed at construction
@ExternalSymbolName("org_jetbrains_skia_PathMeasure__1nGetLength")
private external fun _nGetLength(ptr: NativePointer): Float

// Native: SkContourMeasure::getPosTan -> distanceToSegment -> SkTKSearch does input-dependent binary search
@ExternalSymbolName("org_jetbrains_skia_PathMeasure__1nGetPosition")
@ForceNativeThreadState
private external fun _nGetPosition(ptr: NativePointer, distance: Float, data: InteropPointer): Boolean

// Native: SkContourMeasure::getPosTan -> distanceToSegment -> SkTKSearch does input-dependent binary search
@ExternalSymbolName("org_jetbrains_skia_PathMeasure__1nGetTangent")
@ForceNativeThreadState
private external fun _nGetTangent(ptr: NativePointer, distance: Float, data: InteropPointer): Boolean

// Native: SkContourMeasure::getPosTan -> distanceToSegment -> SkTKSearch does input-dependent binary search
@ExternalSymbolName("org_jetbrains_skia_PathMeasure__1nGetRSXform")
@ForceNativeThreadState
private external fun _nGetRSXform(ptr: NativePointer, distance: Float, data: InteropPointer): Boolean

// Native: SkContourMeasure::getMatrix -> distanceToSegment -> SkTKSearch does input-dependent binary search
@ExternalSymbolName("org_jetbrains_skia_PathMeasure__1nGetMatrix")
@ForceNativeThreadState
private external fun _nGetMatrix(
    ptr: NativePointer,
    distance: Float,
    getPosition: Boolean,
    getTangent: Boolean,
    data: InteropPointer
): Boolean

// Native: SkContourMeasure::getSegment -> SkContourMeasure_segTo loop over every segment in the distance range
@ExternalSymbolName("org_jetbrains_skia_PathMeasure__1nGetSegment")
@ForceNativeThreadState
private external fun _nGetSegment(
    ptr: NativePointer,
    startD: Float,
    endD: Float,
    dstPtr: NativePointer,
    startWithMoveTo: Boolean
): Boolean


// Runnable: SkPathMeasure::isClosed returns SkContourMeasure::fIsClosed stored at construction
@ExternalSymbolName("org_jetbrains_skia_PathMeasure__1nIsClosed")
private external fun _nIsClosed(ptr: NativePointer): Boolean

// Native: SkPathMeasure::nextContour -> SkContourMeasureIter::next -> buildSegments subdivides the next contour
@ExternalSymbolName("org_jetbrains_skia_PathMeasure__1nNextContour")
@ForceNativeThreadState
private external fun _nNextContour(ptr: NativePointer): Boolean
