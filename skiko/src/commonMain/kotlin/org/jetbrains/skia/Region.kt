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
// No ForceNativeThreadState: creates an empty region that stores empty bounds and a shared empty run head, so it is
// short and cannot block.
// Native call path: SkRegion::SkRegion
private external fun Region_nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Region__1nGetFinalizer")
// No ForceNativeThreadState: returns the address of the deletion function, so it is short and cannot block.
// Native call path: deleteRegion
private external fun Region_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Region__1nIsEmpty")
// No ForceNativeThreadState: checks whether the region's run head is the empty one, so it is short and cannot block.
// Native call path: SkRegion::isEmpty
private external fun Region_nIsEmpty(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nIsRect")
// No ForceNativeThreadState: checks whether the region's run head is the rectangle one, so it is short and cannot
// block.
// Native call path: SkRegion::isRect
private external fun Region_nIsRect(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nGetBounds")
// No ForceNativeThreadState: copies the stored bounds into the caller's array, so it is short and cannot block.
// Native call path: SkRegion::getBounds
private external fun Region_nGetBounds(ptr: NativePointer, ltrb: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Region__1nSet")
// No ForceNativeThreadState: shares the region's run block by reference count instead of copying it, so it is short and
// cannot block.
// Native call path: SkRegion::set -> setRegion
private external fun Region_nSet(ptr: NativePointer, regionPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nIsComplex")
// No ForceNativeThreadState: compares two pointers to decide whether the region is complex, so it is short and cannot
// block.
// Native call path: SkRegion::isComplex
private external fun Region_nIsComplex(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nComputeRegionComplexity")
// No ForceNativeThreadState: returns a stored interval count, so it is short and cannot block.
// Native call path: SkRegion::computeRegionComplexity
private external fun Region_nComputeRegionComplexity(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Region__1nAddBoundaryPath")
// Needs ForceNativeThreadState: walks every span of the region, sorts its edges and builds a path, so its time grows
// with the run count.
// Native call path: SkRegion::addBoundaryPath -> SkTQSort
@ForceNativeThreadState
private external fun Region_nAddBoundaryPath(ptr: NativePointer, pathBuilderPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nGetBoundaryPath")
// Needs ForceNativeThreadState: builds the boundary path by sorting and linking all of the region's edges, so its time
// grows with the run count.
// Native call path: SkRegion::getBoundaryPath -> SkRegion::addBoundaryPath -> SkTQSort
@ForceNativeThreadState
private external fun Region_nGetBoundaryPath(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Region__1nSetEmpty")
// No ForceNativeThreadState: releases the region's run block, which holds only plain ints, so it is short and cannot
// block.
// Native call path: SkRegion::setEmpty -> freeRuns
private external fun Region_nSetEmpty(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nSetRect")
// No ForceNativeThreadState: releases the old runs and stores a rectangle's bounds and head, so it is short and cannot
// block.
// Native call path: SkRegion::setRect -> setEmpty -> freeRuns
private external fun Region_nSetRect(ptr: NativePointer, left: Int, top: Int, right: Int, bottom: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nSetRects")
// Needs ForceNativeThreadState: unions the rectangles pairwise with region ops, so its time grows with the number of
// rectangles.
// Native call path: SkRegion::setRects -> SkRegion::op
@ForceNativeThreadState
private external fun Region_nSetRects(ptr: NativePointer, rects: InteropPointer, count: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nSetRegion")
// No ForceNativeThreadState: shares the source region's run block by reference count instead of copying it, so it is
// short and cannot block.
// Native call path: SkRegion::setRegion
private external fun Region_nSetRegion(ptr: NativePointer, regionPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nSetPath")
// Needs ForceNativeThreadState: turns the path into rows of filled intervals and intersects them with a complex
// clipping region; work grows with the path size.
// Native call path: SkRegion::setPath -> SkScan::FillPath
@ForceNativeThreadState
private external fun Region_nSetPath(ptr: NativePointer, pathPtr: NativePointer, regionPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nIntersectsIRect")
// Needs ForceNativeThreadState: searches the region's scanlines and walks each row of a complex region, so its time
// grows with the scanline count.
// Native call path: SkRegion::intersects -> findScanline
@ForceNativeThreadState
private external fun Region_nIntersectsIRect(ptr: NativePointer, left: Int, top: Int, right: Int, bottom: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nIntersectsRegion")
// Needs ForceNativeThreadState: merges the runs of both regions to test for overlap, so its time grows with the run
// count.
// Native call path: SkRegion::intersects -> Oper
@ForceNativeThreadState
private external fun Region_nIntersectsRegion(ptr: NativePointer, regionPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nContainsIPoint")
// Needs ForceNativeThreadState: finds the point's scanline and walks its intervals, so its time grows with the run
// count.
// Native call path: SkRegion::contains -> RunHead::findScanline
@ForceNativeThreadState
private external fun Region_nContainsIPoint(ptr: NativePointer, x: Int, y: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nContainsIRect")
// Needs ForceNativeThreadState: finds the rectangle's scanlines and checks each row, so its time grows with the run
// count.
// Native call path: SkRegion::contains -> findScanline
@ForceNativeThreadState
private external fun Region_nContainsIRect(ptr: NativePointer, left: Int, top: Int, right: Int, bottom: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nContainsRegion")
// Needs ForceNativeThreadState: walks the runs of both regions to check containment, so its time grows with the run
// count.
// Native call path: SkRegion::contains -> Oper
@ForceNativeThreadState
private external fun Region_nContainsRegion(ptr: NativePointer, regionPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nQuickContains")
// No ForceNativeThreadState: compares the head pointer and four stored bounds values, so it is short and cannot block.
// Native call path: SkRegion::quickContains
private external fun Region_nQuickContains(ptr: NativePointer, left: Int, top: Int, right: Int, bottom: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nQuickRejectIRect")
// No ForceNativeThreadState: checks for emptiness and intersects the stored bounds with a rectangle, so it is short and
// cannot block.
// Native call path: SkRegion::quickReject
private external fun Region_nQuickRejectIRect(ptr: NativePointer, left: Int, top: Int, right: Int, bottom: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nQuickRejectRegion")
// No ForceNativeThreadState: checks both regions for emptiness and intersects their stored bounds, so it is short and
// cannot block.
// Native call path: SkRegion::quickReject
private external fun Region_nQuickRejectRegion(ptr: NativePointer, regionPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nTranslate")
// Needs ForceNativeThreadState: copies the region when needed and rewrites every run of a complex region, so its time
// grows with the run count.
// Native call path: SkRegion::translate -> ensureWritable
@ForceNativeThreadState
private external fun Region_nTranslate(ptr: NativePointer, dx: Int, dy: Int)

@ExternalSymbolName("org_jetbrains_skia_Region__1nOpIRect")
// Needs ForceNativeThreadState: merges the runs of both regions into a new run array, so its time grows with the run
// count.
// Native call path: SkRegion::op -> Oper
@ForceNativeThreadState
private external fun Region_nOpIRect(ptr: NativePointer, left: Int, top: Int, right: Int, bottom: Int, op: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nOpRegion")
// Needs ForceNativeThreadState: merges the runs of both regions into a new run array, so its time grows with the run
// count.
// Native call path: SkRegion::op -> Oper
@ForceNativeThreadState
private external fun Region_nOpRegion(ptr: NativePointer, regionPtr: NativePointer, op: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nOpIRectRegion")
// Needs ForceNativeThreadState: merges the runs of both regions into a new run array, so its time grows with the run
// count.
// Native call path: SkRegion::op -> Oper
@ForceNativeThreadState
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
// Needs ForceNativeThreadState: merges the runs of both regions into a new run array, so its time grows with the run
// count.
// Native call path: SkRegion::op -> Oper
@ForceNativeThreadState
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
// Needs ForceNativeThreadState: merges the runs of both regions into a new run array, so its time grows with the run
// count.
// Native call path: SkRegion::op -> Oper
@ForceNativeThreadState
private external fun Region_nOpRegionRegion(
    ptr: NativePointer,
    regionPtrA: NativePointer,
    regionPtrB: NativePointer,
    op: Int
): Boolean
