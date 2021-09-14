@file:Suppress("NESTED_EXTERNAL_DECLARATION")

package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.reachabilityBarrier

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
            _nSet(_ptr, getPtr(r))
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
            _nIsComplex(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    val bounds: IRect
        get() = try {
            Stats.onNativeCall()
            Region_nGetBounds(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    fun computeRegionComplexity(): Int {
        return try {
            Stats.onNativeCall()
            _nComputeRegionComplexity(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun getBoundaryPath(p: Path?): Boolean {
        return try {
            Stats.onNativeCall()
            _nGetBoundaryPath(
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
            _nSetEmpty(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun setRect(rect: IRect): Boolean {
        return try {
            Stats.onNativeCall()
            _nSetRect(_ptr, rect.left, rect.top, rect.right, rect.bottom)
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
            _nSetRects(_ptr, arr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun setRegion(r: Region?): Boolean {
        return try {
            Stats.onNativeCall()
            _nSetRegion(_ptr, getPtr(r))
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(r)
        }
    }

    fun setPath(path: Path?, clip: Region?): Boolean {
        return try {
            Stats.onNativeCall()
            _nSetPath(
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
            _nIntersectsIRect(_ptr, rect.left, rect.top, rect.right, rect.bottom)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun intersects(r: Region?): Boolean {
        return try {
            Stats.onNativeCall()
            _nIntersectsRegion(
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
            _nContainsIPoint(_ptr, x, y)
        } finally {
            reachabilityBarrier(this)
        }
    }

    operator fun contains(rect: IRect): Boolean {
        return try {
            Stats.onNativeCall()
            _nContainsIRect(_ptr, rect.left, rect.top, rect.right, rect.bottom)
        } finally {
            reachabilityBarrier(this)
        }
    }

    operator fun contains(r: Region?): Boolean {
        return try {
            Stats.onNativeCall()
            _nContainsRegion(_ptr, getPtr(r))
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(r)
        }
    }

    fun quickContains(rect: IRect): Boolean {
        return try {
            Stats.onNativeCall()
            _nQuickContains(_ptr, rect.left, rect.top, rect.right, rect.bottom)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun quickReject(rect: IRect): Boolean {
        return try {
            Stats.onNativeCall()
            _nQuickRejectIRect(_ptr, rect.left, rect.top, rect.right, rect.bottom)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun quickReject(r: Region?): Boolean {
        return try {
            Stats.onNativeCall()
            _nQuickRejectRegion(
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
            _nTranslate(_ptr, dx, dy)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun op(rect: IRect, op: Op): Boolean {
        return try {
            Stats.onNativeCall()
            _nOpIRect(
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
            _nOpRegion(
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
            _nOpIRectRegion(
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
            _nOpRegionIRect(
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
            _nOpRegionRegion(
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
private external fun Region_nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Region__1nGetFinalizer")
private external fun Region_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Region__1nIsEmpty")
private external fun Region_nIsEmpty(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nIsRect")
private external fun Region_nIsRect(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nGetBounds")
private external fun Region_nGetBounds(ptr: NativePointer): IRect

@ExternalSymbolName("org_jetbrains_skia_Region__1nSet")
private external fun _nSet(ptr: NativePointer, regoinPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nIsComplex")
private external fun _nIsComplex(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nComputeRegionComplexity")
private external fun _nComputeRegionComplexity(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Region__1nGetBoundaryPath")
private external fun _nGetBoundaryPath(ptr: NativePointer, pathPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nSetEmpty")
private external fun _nSetEmpty(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nSetRect")
private external fun _nSetRect(ptr: NativePointer, left: Int, top: Int, right: Int, bottom: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nSetRects")
private external fun _nSetRects(ptr: NativePointer, rects: IntArray?): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nSetRegion")
private external fun _nSetRegion(ptr: NativePointer, regionPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nSetPath")
private external fun _nSetPath(ptr: NativePointer, pathPtr: NativePointer, regionPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nIntersectsIRect")
private external fun _nIntersectsIRect(ptr: NativePointer, left: Int, top: Int, right: Int, bottom: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nIntersectsRegion")
private external fun _nIntersectsRegion(ptr: NativePointer, regionPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nContainsIPoint")
private external fun _nContainsIPoint(ptr: NativePointer, x: Int, y: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nContainsIRect")
private external fun _nContainsIRect(ptr: NativePointer, left: Int, top: Int, right: Int, bottom: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nContainsRegion")
private external fun _nContainsRegion(ptr: NativePointer, regionPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nQuickContains")
private external fun _nQuickContains(ptr: NativePointer, left: Int, top: Int, right: Int, bottom: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nQuickRejectIRect")
private external fun _nQuickRejectIRect(ptr: NativePointer, left: Int, top: Int, right: Int, bottom: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nQuickRejectRegion")
private external fun _nQuickRejectRegion(ptr: NativePointer, regionPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nTranslate")
private external fun _nTranslate(ptr: NativePointer, dx: Int, dy: Int)

@ExternalSymbolName("org_jetbrains_skia_Region__1nOpIRect")
private external fun _nOpIRect(ptr: NativePointer, left: Int, top: Int, right: Int, bottom: Int, op: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nOpRegion")
private external fun _nOpRegion(ptr: NativePointer, regionPtr: NativePointer, op: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nOpIRectRegion")
private external fun _nOpIRectRegion(
    ptr: NativePointer,
    left: Int,
    top: Int,
    right: Int,
    bottom: Int,
    regionPtr: NativePointer,
    op: Int
): Boolean


@ExternalSymbolName("org_jetbrains_skia_Region__1nOpRegionIRect")
private external fun _nOpRegionIRect(
    ptr: NativePointer,
    regionPtr: NativePointer,
    left: Int,
    top: Int,
    right: Int,
    bottom: Int,
    op: Int
): Boolean


@ExternalSymbolName("org_jetbrains_skia_Region__1nOpRegionRegion")
private external fun _nOpRegionRegion(
    ptr: NativePointer,
    regionPtrA: NativePointer,
    regionPtrB: NativePointer,
    op: Int
): Boolean