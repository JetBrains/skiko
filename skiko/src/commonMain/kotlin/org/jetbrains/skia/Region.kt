@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.ExternalSymbolName
import kotlin.jvm.JvmStatic

class Region : Managed(_nMake(), _FinalizerHolder.PTR) {
    companion object {
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Region__1nMake")
        external fun _nMake(): Long
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Region__1nGetFinalizer")
        external fun _nGetFinalizer(): Long
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Region__1nSet")
        external fun _nSet(ptr: Long, regoinPtr: Long): Boolean
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Region__1nIsEmpty")
        external fun _nIsEmpty(ptr: Long): Boolean
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Region__1nIsRect")
        external fun _nIsRect(ptr: Long): Boolean
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Region__1nIsComplex")
        external fun _nIsComplex(ptr: Long): Boolean
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Region__1nGetBounds")
        external fun _nGetBounds(ptr: Long): IRect
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Region__1nComputeRegionComplexity")
        external fun _nComputeRegionComplexity(ptr: Long): Int
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Region__1nGetBoundaryPath")
        external fun _nGetBoundaryPath(ptr: Long, pathPtr: Long): Boolean
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Region__1nSetEmpty")
        external fun _nSetEmpty(ptr: Long): Boolean
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Region__1nSetRect")
        external fun _nSetRect(ptr: Long, left: Int, top: Int, right: Int, bottom: Int): Boolean
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Region__1nSetRects")
        external fun _nSetRects(ptr: Long, rects: IntArray?): Boolean
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Region__1nSetRegion")
        external fun _nSetRegion(ptr: Long, regionPtr: Long): Boolean
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Region__1nSetPath")
        external fun _nSetPath(ptr: Long, pathPtr: Long, regionPtr: Long): Boolean
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Region__1nIntersectsIRect")
        external fun _nIntersectsIRect(ptr: Long, left: Int, top: Int, right: Int, bottom: Int): Boolean
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Region__1nIntersectsRegion")
        external fun _nIntersectsRegion(ptr: Long, regionPtr: Long): Boolean
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Region__1nContainsIPoint")
        external fun _nContainsIPoint(ptr: Long, x: Int, y: Int): Boolean
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Region__1nContainsIRect")
        external fun _nContainsIRect(ptr: Long, left: Int, top: Int, right: Int, bottom: Int): Boolean
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Region__1nContainsRegion")
        external fun _nContainsRegion(ptr: Long, regionPtr: Long): Boolean
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Region__1nQuickContains")
        external fun _nQuickContains(ptr: Long, left: Int, top: Int, right: Int, bottom: Int): Boolean
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Region__1nQuickRejectIRect")
        external fun _nQuickRejectIRect(ptr: Long, left: Int, top: Int, right: Int, bottom: Int): Boolean
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Region__1nQuickRejectRegion")
        external fun _nQuickRejectRegion(ptr: Long, regionPtr: Long): Boolean
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Region__1nTranslate")
        external fun _nTranslate(ptr: Long, dx: Int, dy: Int)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Region__1nOpIRect")
        external fun _nOpIRect(ptr: Long, left: Int, top: Int, right: Int, bottom: Int, op: Int): Boolean
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Region__1nOpRegion")
        external fun _nOpRegion(ptr: Long, regionPtr: Long, op: Int): Boolean
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Region__1nOpIRectRegion")
        external fun _nOpIRectRegion(
            ptr: Long,
            left: Int,
            top: Int,
            right: Int,
            bottom: Int,
            regionPtr: Long,
            op: Int
        ): Boolean

        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Region__1nOpRegionIRect")
        external fun _nOpRegionIRect(
            ptr: Long,
            regionPtr: Long,
            left: Int,
            top: Int,
            right: Int,
            bottom: Int,
            op: Int
        ): Boolean

        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Region__1nOpRegionRegion")
        external fun _nOpRegionRegion(ptr: Long, regionPtrA: Long, regionPtrB: Long, op: Int): Boolean

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
            _nIsEmpty(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    val isRect: Boolean
        get() = try {
            Stats.onNativeCall()
            _nIsRect(_ptr)
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
            _nGetBounds(_ptr)
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
        val PTR = _nGetFinalizer()
    }

    init {
        Stats.onNativeCall()
    }
}