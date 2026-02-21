package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skiko.internal.unpackTo

/**
 * Describes a set of pixels as either one integer rectangle or a run-length encoded
 * set of rectangles.
 */
class Region : Managed(Region_nMake(), _FinalizerHolder.PTR) {
    companion object {
        init {
            staticLoad()
        }
    }

    /** Logical operations used to combine regions. */
    enum class Op {
        /** target minus operand */
        DIFFERENCE,

        /** target intersected with operand */
        INTERSECT,

        /** target unioned with operand */
        UNION,

        /** target exclusive-or with operand */
        XOR,

        /** operand minus target */
        REVERSE_DIFFERENCE,

        /** replace target with operand */
        REPLACE;

        companion object {
            internal val _values = Op.entries.toTypedArray()
        }
    }

    /**
     * Sets this region to [src].
     *
     * @return true if the resulting region is not empty
     */
    fun set(src: Region): Boolean {
        return try {
            Stats.onNativeCall()
            Region_nSet(_ptr, getPtr(src))
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(src)
        }
    }

    /** Returns true if this region is empty. */
    val isEmpty: Boolean
        get() = try {
            Stats.onNativeCall()
            Region_nIsEmpty(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /** Returns true if this region is one non-empty rectangle. */
    val isRect: Boolean
        get() = try {
            Stats.onNativeCall()
            Region_nIsRect(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /** Returns true if this region is represented by more than one rectangle. */
    val isComplex: Boolean
        get() = try {
            Stats.onNativeCall()
            Region_nIsComplex(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /** Returns the union bounds of all rectangles in this region. */
    val bounds: IRect
        get() = try {
            Stats.onNativeCall()
            val ltrb = withResult(IntArray(4)) {
                Region_nGetBounds(_ptr, it)
            }
            IRect(ltrb[0], ltrb[1], ltrb[2], ltrb[3])
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * Returns a value that increases with the number of elements in the region.
     * Returns 0 for empty, 1 for a single rectangle, and larger values for complex regions.
     */
    fun computeRegionComplexity(): Int {
        return try {
            Stats.onNativeCall()
            Region_nComputeRegionComplexity(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Appends the boundary outline of this region into [pathBuilder].
     *
     * @return true if this region is not empty and the builder changed
     */
    fun addBoundaryPath(pathBuilder: PathBuilder): Boolean {
        return try {
            Stats.onNativeCall()
            Region_nAddBoundaryPath(_ptr, getPtr(pathBuilder))
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(pathBuilder)
        }
    }

    /** Returns the boundary of this region as a new immutable [Path]. */
    fun getBoundaryPath(): Path {
        return try {
            Stats.onNativeCall()
            Path(Region_nGetBoundaryPath(_ptr))
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Sets this region to empty.
     *
     * @return always false
     */
    fun setEmpty(): Boolean {
        return try {
            Stats.onNativeCall()
            Region_nSetEmpty(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /** Sets this region to [rect]. Returns true if [rect] is not empty. */
    fun setRect(rect: IRect): Boolean {
        return setRect(rect.left, rect.top, rect.right, rect.bottom)
    }

    /** Sets this region to [left], [top], [right], [bottom]. */
    fun setRect(left: Int, top: Int, right: Int, bottom: Int): Boolean {
        return try {
            Stats.onNativeCall()
            Region_nSetRect(_ptr, left, top, right, bottom)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Sets this region to a union of [rects].
     *
     * @return true if the resulting region is not empty
     */
    fun setRects(rects: Array<IRect>): Boolean {
        return try {
            val arr = IntArray(rects.size * 4)
            rects.unpackTo(arr) { rect, destination, i ->
                destination[i] = rect.left
                destination[i + 1] = rect.top
                destination[i + 2] = rect.right
                destination[i + 3] = rect.bottom
                //Stepping 4 places for the next rect
                i + 4
            }
            Stats.onNativeCall()
            interopScope {
                Region_nSetRects(_ptr, toInterop(arr), rects.size)
            }
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Sets this region to [region].
     *
     * @return true if [region] is not empty
     */
    fun setRegion(region: Region): Boolean {
        return try {
            Stats.onNativeCall()
            Region_nSetRegion(_ptr, getPtr(region))
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(region)
        }
    }

    /**
     * Sets this region to the rasterized outline of [path] clipped by [clip].
     *
     * @return true if the resulting region is not empty
     */
    fun setPath(path: Path, clip: Region): Boolean {
        return try {
            Stats.onNativeCall()
            Region_nSetPath(_ptr, getPtr(path), getPtr(clip))
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(path)
            reachabilityBarrier(clip)
        }
    }

    /** Returns true if this region intersects [rect]. */
    fun intersects(rect: IRect): Boolean {
        return intersects(rect.left, rect.top, rect.right, rect.bottom)
    }

    /** Returns true if this region intersects [left], [top], [right], [bottom]. */
    fun intersects(left: Int, top: Int, right: Int, bottom: Int): Boolean {
        return try {
            Stats.onNativeCall()
            Region_nIntersectsIRect(_ptr, left, top, right, bottom)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /** Returns true if this region intersects [region]. */
    fun intersects(region: Region): Boolean {
        return try {
            Stats.onNativeCall()
            Region_nIntersectsRegion(_ptr, getPtr(region))
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(region)
        }
    }

    /** Returns true if point ([x], [y]) is inside this region. */
    fun contains(x: Int, y: Int): Boolean {
        return try {
            Stats.onNativeCall()
            Region_nContainsIPoint(_ptr, x, y)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /** Returns true if rectangle ([left], [top], [right], [bottom]) is fully inside this region. */
    fun contains(left: Int, top: Int, right: Int, bottom: Int): Boolean {
        return try {
            Stats.onNativeCall()
            Region_nContainsIRect(_ptr, left, top, right, bottom)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /** Returns true if [rect] is fully inside this region. */
    operator fun contains(rect: IRect): Boolean {
        return contains(rect.left, rect.top, rect.right, rect.bottom)
    }

    /** Returns true if [region] is fully inside this region. */
    operator fun contains(region: Region): Boolean {
        return try {
            Stats.onNativeCall()
            Region_nContainsRegion(_ptr, getPtr(region))
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(region)
        }
    }

    /**
     * Fast conservative containment check.
     * Returns true only when this region is a single rectangle containing [rect].
     */
    fun quickContains(rect: IRect): Boolean {
        return quickContains(rect.left, rect.top, rect.right, rect.bottom)
    }

    /** Fast conservative containment check for rectangle coordinates. */
    fun quickContains(left: Int, top: Int, right: Int, bottom: Int): Boolean {
        return try {
            Stats.onNativeCall()
            Region_nQuickContains(_ptr, left, top, right, bottom)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /** Fast conservative rejection check for [rect]. */
    fun quickReject(rect: IRect): Boolean {
        return quickReject(rect.left, rect.top, rect.right, rect.bottom)
    }

    /** Fast conservative rejection check for rectangle coordinates. */
    fun quickReject(left: Int, top: Int, right: Int, bottom: Int): Boolean {
        return try {
            Stats.onNativeCall()
            Region_nQuickRejectIRect(_ptr, left, top, right, bottom)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /** Fast conservative rejection check for [region]. */
    fun quickReject(region: Region): Boolean {
        return try {
            Stats.onNativeCall()
            Region_nQuickRejectRegion(_ptr, getPtr(region))
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(region)
        }
    }

    /** Offsets this region by ([dx], [dy]). */
    fun translate(dx: Int, dy: Int) {
        try {
            Stats.onNativeCall()
            Region_nTranslate(_ptr, dx, dy)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /** Replaces this region with result of `this op rect`. */
    fun op(rect: IRect, op: Op): Boolean {
        return op(rect.left, rect.top, rect.right, rect.bottom, op)
    }

    /** Replaces this region with result of `this op rect`. */
    fun op(left: Int, top: Int, right: Int, bottom: Int, op: Op): Boolean {
        return try {
            Stats.onNativeCall()
            Region_nOpIRect(_ptr, left, top, right, bottom, op.ordinal)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /** Replaces this region with result of `this op region`. */
    fun op(region: Region, op: Op): Boolean {
        return try {
            Stats.onNativeCall()
            Region_nOpRegion(_ptr, getPtr(region), op.ordinal)
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(region)
        }
    }

    /** Replaces this region with result of `rect op region`. */
    fun op(rect: IRect, region: Region, op: Op): Boolean {
        return op(rect.left, rect.top, rect.right, rect.bottom, region, op)
    }

    /** Replaces this region with result of `rect op region`. */
    fun op(left: Int, top: Int, right: Int, bottom: Int, region: Region, op: Op): Boolean {
        return try {
            Stats.onNativeCall()
            Region_nOpIRectRegion(_ptr, left, top, right, bottom, getPtr(region), op.ordinal)
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(region)
        }
    }

    /** Replaces this region with result of `region op rect`. */
    fun op(region: Region, rect: IRect, op: Op): Boolean {
        return op(region, rect.left, rect.top, rect.right, rect.bottom, op)
    }

    /** Replaces this region with result of `region op rect`. */
    fun op(region: Region, left: Int, top: Int, right: Int, bottom: Int, op: Op): Boolean {
        return try {
            Stats.onNativeCall()
            Region_nOpRegionIRect(_ptr, getPtr(region), left, top, right, bottom, op.ordinal)
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(region)
        }
    }

    /** Replaces this region with result of `regionA op regionB`. */
    fun op(regionA: Region, regionB: Region, op: Op): Boolean {
        return try {
            Stats.onNativeCall()
            Region_nOpRegionRegion(_ptr, getPtr(regionA), getPtr(regionB), op.ordinal)
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(regionA)
            reachabilityBarrier(regionB)
        }
    }

    private object _FinalizerHolder {
        val PTR = Region_nGetFinalizer()
    }

    init {
        Stats.onNativeCall()
    }
}

@ExternalSymbolName("org_jetbrains_skia_Region__1nMake")
private external fun Region_nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Region__1nGetFinalizer")
private external fun Region_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Region__1nIsEmpty")
private external fun Region_nIsEmpty(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nIsRect")
private external fun Region_nIsRect(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nGetBounds")
private external fun Region_nGetBounds(ptr: NativePointer, ltrb: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Region__1nSet")
private external fun Region_nSet(ptr: NativePointer, regionPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nIsComplex")
private external fun Region_nIsComplex(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nComputeRegionComplexity")
private external fun Region_nComputeRegionComplexity(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Region__1nAddBoundaryPath")
private external fun Region_nAddBoundaryPath(ptr: NativePointer, pathBuilderPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nGetBoundaryPath")
private external fun Region_nGetBoundaryPath(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Region__1nSetEmpty")
private external fun Region_nSetEmpty(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nSetRect")
private external fun Region_nSetRect(ptr: NativePointer, left: Int, top: Int, right: Int, bottom: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nSetRects")
private external fun Region_nSetRects(ptr: NativePointer, rects: InteropPointer, count: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nSetRegion")
private external fun Region_nSetRegion(ptr: NativePointer, regionPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nSetPath")
private external fun Region_nSetPath(ptr: NativePointer, pathPtr: NativePointer, regionPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nIntersectsIRect")
private external fun Region_nIntersectsIRect(ptr: NativePointer, left: Int, top: Int, right: Int, bottom: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nIntersectsRegion")
private external fun Region_nIntersectsRegion(ptr: NativePointer, regionPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nContainsIPoint")
private external fun Region_nContainsIPoint(ptr: NativePointer, x: Int, y: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nContainsIRect")
private external fun Region_nContainsIRect(ptr: NativePointer, left: Int, top: Int, right: Int, bottom: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nContainsRegion")
private external fun Region_nContainsRegion(ptr: NativePointer, regionPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nQuickContains")
private external fun Region_nQuickContains(ptr: NativePointer, left: Int, top: Int, right: Int, bottom: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nQuickRejectIRect")
private external fun Region_nQuickRejectIRect(ptr: NativePointer, left: Int, top: Int, right: Int, bottom: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nQuickRejectRegion")
private external fun Region_nQuickRejectRegion(ptr: NativePointer, regionPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nTranslate")
private external fun Region_nTranslate(ptr: NativePointer, dx: Int, dy: Int)

@ExternalSymbolName("org_jetbrains_skia_Region__1nOpIRect")
private external fun Region_nOpIRect(ptr: NativePointer, left: Int, top: Int, right: Int, bottom: Int, op: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nOpRegion")
private external fun Region_nOpRegion(ptr: NativePointer, regionPtr: NativePointer, op: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nOpIRectRegion")
private external fun Region_nOpIRectRegion(
    ptr: NativePointer,
    left: Int,
    top: Int,
    right: Int,
    bottom: Int,
    regionPtr: NativePointer,
    op: Int
): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nOpRegionIRect")
private external fun Region_nOpRegionIRect(
    ptr: NativePointer,
    regionPtr: NativePointer,
    left: Int,
    top: Int,
    right: Int,
    bottom: Int,
    op: Int
): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nOpRegionRegion")
private external fun Region_nOpRegionRegion(
    ptr: NativePointer,
    regionPtrA: NativePointer,
    regionPtrB: NativePointer,
    op: Int
): Boolean
