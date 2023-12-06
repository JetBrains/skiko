package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad

class Region : Managed(Region_nMake(), _FinalizerHolder.PTR) {
    companion object {
        init {
            staticLoad()
        }
    }

    enum class Op {
        DIFFERENCE, INTERSECT, UNION, XOR, REVERSE_DIFFERENCE, REPLACE;

        companion object {
            internal val _values = values()
        }
    }

    fun set(r: Region?): Boolean {
        return try {
            Stats.onNativeCall()
            Region_nSet(_ptr, getPtr(r))
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(r)
        }
    }

    val isEmpty: Boolean
        get() = try {
            Stats.onNativeCall()
            Region_nIsEmpty(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    val isRect: Boolean
        get() = try {
            Stats.onNativeCall()
            Region_nIsRect(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    val isComplex: Boolean
        get() = try {
            Stats.onNativeCall()
            Region_nIsComplex(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
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

    fun computeRegionComplexity(): Int {
        return try {
            Stats.onNativeCall()
            Region_nComputeRegionComplexity(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun getBoundaryPath(p: Path?): Boolean {
        return try {
            Stats.onNativeCall()
            Region_nGetBoundaryPath(
                _ptr,
                getPtr(p)
            )
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(p)
        }
    }

    fun setEmpty(): Boolean {
        return try {
            Stats.onNativeCall()
            Region_nSetEmpty(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun setRect(rect: IRect): Boolean {
        return try {
            Stats.onNativeCall()
            Region_nSetRect(_ptr, rect.left, rect.top, rect.right, rect.bottom)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun setRects(rects: Array<IRect>): Boolean {
        return try {
            val arr = IntArray(rects.size * 4)
            for (i in rects.indices) {
                arr[i * 4] = rects[i].left
                arr[i * 4 + 1] = rects[i].top
                arr[i * 4 + 2] = rects[i].right
                arr[i * 4 + 3] = rects[i].bottom
            }
            Stats.onNativeCall()
            interopScope {
                Region_nSetRects(_ptr, toInterop(arr), rects.size)
            }
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun setRegion(r: Region?): Boolean {
        return try {
            Stats.onNativeCall()
            Region_nSetRegion(_ptr, getPtr(r))
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(r)
        }
    }

    fun setPath(path: Path?, clip: Region?): Boolean {
        return try {
            Stats.onNativeCall()
            Region_nSetPath(
                _ptr,
                getPtr(path),
                getPtr(clip)
            )
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(path)
            reachabilityBarrier(clip)
        }
    }

    fun intersects(rect: IRect): Boolean {
        return try {
            Stats.onNativeCall()
            Region_nIntersectsIRect(_ptr, rect.left, rect.top, rect.right, rect.bottom)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun intersects(r: Region?): Boolean {
        return try {
            Stats.onNativeCall()
            Region_nIntersectsRegion(
                _ptr,
                getPtr(r)
            )
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(r)
        }
    }

    fun contains(x: Int, y: Int): Boolean {
        return try {
            Stats.onNativeCall()
            Region_nContainsIPoint(_ptr, x, y)
        } finally {
            reachabilityBarrier(this)
        }
    }

    operator fun contains(rect: IRect): Boolean {
        return try {
            Stats.onNativeCall()
            Region_nContainsIRect(_ptr, rect.left, rect.top, rect.right, rect.bottom)
        } finally {
            reachabilityBarrier(this)
        }
    }

    operator fun contains(r: Region?): Boolean {
        return try {
            Stats.onNativeCall()
            Region_nContainsRegion(_ptr, getPtr(r))
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(r)
        }
    }

    fun quickContains(rect: IRect): Boolean {
        return try {
            Stats.onNativeCall()
            Region_nQuickContains(_ptr, rect.left, rect.top, rect.right, rect.bottom)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun quickReject(rect: IRect): Boolean {
        return try {
            Stats.onNativeCall()
            Region_nQuickRejectIRect(_ptr, rect.left, rect.top, rect.right, rect.bottom)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun quickReject(r: Region?): Boolean {
        return try {
            Stats.onNativeCall()
            Region_nQuickRejectRegion(
                _ptr,
                getPtr(r)
            )
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(r)
        }
    }

    fun translate(dx: Int, dy: Int) {
        try {
            Stats.onNativeCall()
            Region_nTranslate(_ptr, dx, dy)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun op(rect: IRect, op: Op): Boolean {
        return try {
            Stats.onNativeCall()
            Region_nOpIRect(
                _ptr,
                rect.left,
                rect.top,
                rect.right,
                rect.bottom,
                op.ordinal
            )
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun op(r: Region?, op: Op): Boolean {
        return try {
            Stats.onNativeCall()
            Region_nOpRegion(
                _ptr,
                getPtr(r),
                op.ordinal
            )
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(r)
        }
    }

    fun op(rect: IRect, r: Region?, op: Op): Boolean {
        return try {
            Stats.onNativeCall()
            Region_nOpIRectRegion(
                _ptr,
                rect.left,
                rect.top,
                rect.right,
                rect.bottom,
                getPtr(r),
                op.ordinal
            )
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(r)
        }
    }

    fun op(r: Region?, rect: IRect, op: Op): Boolean {
        return try {
            Stats.onNativeCall()
            Region_nOpRegionIRect(
                _ptr,
                getPtr(r),
                rect.left,
                rect.top,
                rect.right,
                rect.bottom,
                op.ordinal
            )
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(r)
        }
    }

    fun op(a: Region?, b: Region?, op: Op): Boolean {
        return try {
            Stats.onNativeCall()
            Region_nOpRegionRegion(
                _ptr,
                getPtr(a),
                getPtr(b),
                op.ordinal
            )
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(a)
            reachabilityBarrier(b)
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
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Region__1nMake")
private external fun Region_nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Region__1nGetFinalizer")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Region__1nGetFinalizer")
private external fun Region_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Region__1nIsEmpty")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Region__1nIsEmpty")
private external fun Region_nIsEmpty(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nIsRect")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Region__1nIsRect")
private external fun Region_nIsRect(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nGetBounds")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Region__1nGetBounds")
private external fun Region_nGetBounds(ptr: NativePointer, ltrb: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Region__1nSet")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Region__1nSet")
private external fun Region_nSet(ptr: NativePointer, regoinPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nIsComplex")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Region__1nIsComplex")
private external fun Region_nIsComplex(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nComputeRegionComplexity")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Region__1nComputeRegionComplexity")
private external fun Region_nComputeRegionComplexity(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Region__1nGetBoundaryPath")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Region__1nGetBoundaryPath")
private external fun Region_nGetBoundaryPath(ptr: NativePointer, pathPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nSetEmpty")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Region__1nSetEmpty")
private external fun Region_nSetEmpty(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nSetRect")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Region__1nSetRect")
private external fun Region_nSetRect(ptr: NativePointer, left: Int, top: Int, right: Int, bottom: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nSetRects")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Region__1nSetRects")
private external fun Region_nSetRects(ptr: NativePointer, rects: InteropPointer, count: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nSetRegion")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Region__1nSetRegion")
private external fun Region_nSetRegion(ptr: NativePointer, regionPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nSetPath")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Region__1nSetPath")
private external fun Region_nSetPath(ptr: NativePointer, pathPtr: NativePointer, regionPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nIntersectsIRect")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Region__1nIntersectsIRect")
private external fun Region_nIntersectsIRect(ptr: NativePointer, left: Int, top: Int, right: Int, bottom: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nIntersectsRegion")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Region__1nIntersectsRegion")
private external fun Region_nIntersectsRegion(ptr: NativePointer, regionPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nContainsIPoint")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Region__1nContainsIPoint")
private external fun Region_nContainsIPoint(ptr: NativePointer, x: Int, y: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nContainsIRect")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Region__1nContainsIRect")
private external fun Region_nContainsIRect(ptr: NativePointer, left: Int, top: Int, right: Int, bottom: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nContainsRegion")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Region__1nContainsRegion")
private external fun Region_nContainsRegion(ptr: NativePointer, regionPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nQuickContains")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Region__1nQuickContains")
private external fun Region_nQuickContains(ptr: NativePointer, left: Int, top: Int, right: Int, bottom: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nQuickRejectIRect")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Region__1nQuickRejectIRect")
private external fun Region_nQuickRejectIRect(ptr: NativePointer, left: Int, top: Int, right: Int, bottom: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nQuickRejectRegion")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Region__1nQuickRejectRegion")
private external fun Region_nQuickRejectRegion(ptr: NativePointer, regionPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nTranslate")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Region__1nTranslate")
private external fun Region_nTranslate(ptr: NativePointer, dx: Int, dy: Int)

@ExternalSymbolName("org_jetbrains_skia_Region__1nOpIRect")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Region__1nOpIRect")
private external fun Region_nOpIRect(ptr: NativePointer, left: Int, top: Int, right: Int, bottom: Int, op: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nOpRegion")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Region__1nOpRegion")
private external fun Region_nOpRegion(ptr: NativePointer, regionPtr: NativePointer, op: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nOpIRectRegion")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Region__1nOpIRectRegion")
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
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Region__1nOpRegionIRect")
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
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Region__1nOpRegionRegion")
private external fun Region_nOpRegionRegion(
    ptr: NativePointer,
    regionPtrA: NativePointer,
    regionPtrB: NativePointer,
    op: Int
): Boolean