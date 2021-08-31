package org.jetbrains.skija

import org.jetbrains.skija.impl.Library.Companion.staticLoad
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.skija.impl.Managed
import org.jetbrains.skija.impl.Native
import org.jetbrains.skija.impl.Stats
import java.lang.ref.Reference

class Region : Managed(_nMake(), _FinalizerHolder.PTR) {
    companion object {
        external fun _nMake(): Long
        external fun _nGetFinalizer(): Long
        external fun _nSet(ptr: Long, regoinPtr: Long): Boolean
        external fun _nIsEmpty(ptr: Long): Boolean
        external fun _nIsRect(ptr: Long): Boolean
        external fun _nIsComplex(ptr: Long): Boolean
        external fun _nGetBounds(ptr: Long): IRect
        external fun _nComputeRegionComplexity(ptr: Long): Int
        external fun _nGetBoundaryPath(ptr: Long, pathPtr: Long): Boolean
        external fun _nSetEmpty(ptr: Long): Boolean
        external fun _nSetRect(ptr: Long, left: Int, top: Int, right: Int, bottom: Int): Boolean
        external fun _nSetRects(ptr: Long, rects: IntArray?): Boolean
        external fun _nSetRegion(ptr: Long, regionPtr: Long): Boolean
        external fun _nSetPath(ptr: Long, pathPtr: Long, regionPtr: Long): Boolean
        external fun _nIntersectsIRect(ptr: Long, left: Int, top: Int, right: Int, bottom: Int): Boolean
        external fun _nIntersectsRegion(ptr: Long, regionPtr: Long): Boolean
        external fun _nContainsIPoint(ptr: Long, x: Int, y: Int): Boolean
        external fun _nContainsIRect(ptr: Long, left: Int, top: Int, right: Int, bottom: Int): Boolean
        external fun _nContainsRegion(ptr: Long, regionPtr: Long): Boolean
        external fun _nQuickContains(ptr: Long, left: Int, top: Int, right: Int, bottom: Int): Boolean
        external fun _nQuickRejectIRect(ptr: Long, left: Int, top: Int, right: Int, bottom: Int): Boolean
        external fun _nQuickRejectRegion(ptr: Long, regionPtr: Long): Boolean
        external fun _nTranslate(ptr: Long, dx: Int, dy: Int)
        external fun _nOpIRect(ptr: Long, left: Int, top: Int, right: Int, bottom: Int, op: Int): Boolean
        external fun _nOpRegion(ptr: Long, regionPtr: Long, op: Int): Boolean
        external fun _nOpIRectRegion(
            ptr: Long,
            left: Int,
            top: Int,
            right: Int,
            bottom: Int,
            regionPtr: Long,
            op: Int
        ): Boolean

        external fun _nOpRegionIRect(
            ptr: Long,
            regionPtr: Long,
            left: Int,
            top: Int,
            right: Int,
            bottom: Int,
            op: Int
        ): Boolean

        external fun _nOpRegionRegion(ptr: Long, regionPtrA: Long, regionPtrB: Long, op: Int): Boolean

        init {
            staticLoad()
        }
    }

    enum class Op {
        DIFFERENCE, INTERSECT, UNION, XOR, REVERSE_DIFFERENCE, REPLACE;

        companion object {
            @ApiStatus.Internal
            val _values = values()
        }
    }

    fun set(r: Region?): Boolean {
        return try {
            Stats.onNativeCall()
            _nSet(_ptr, Native.Companion.getPtr(r))
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(r)
        }
    }

    val isEmpty: Boolean
        get() = try {
            Stats.onNativeCall()
            _nIsEmpty(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    val isRect: Boolean
        get() = try {
            Stats.onNativeCall()
            _nIsRect(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    val isComplex: Boolean
        get() = try {
            Stats.onNativeCall()
            _nIsComplex(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    val bounds: IRect
        get() = try {
            Stats.onNativeCall()
            _nGetBounds(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun computeRegionComplexity(): Int {
        return try {
            Stats.onNativeCall()
            _nComputeRegionComplexity(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun getBoundaryPath(p: Path?): Boolean {
        return try {
            Stats.onNativeCall()
            _nGetBoundaryPath(
                _ptr,
                Native.Companion.getPtr(p)
            )
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(p)
        }
    }

    fun setEmpty(): Boolean {
        return try {
            Stats.onNativeCall()
            _nSetEmpty(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun setRect(rect: IRect): Boolean {
        return try {
            Stats.onNativeCall()
            _nSetRect(_ptr, rect.left, rect.top, rect.right, rect.bottom)
        } finally {
            Reference.reachabilityFence(this)
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
            Reference.reachabilityFence(this)
        }
    }

    fun setRegion(r: Region?): Boolean {
        return try {
            Stats.onNativeCall()
            _nSetRegion(_ptr, Native.Companion.getPtr(r))
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(r)
        }
    }

    fun setPath(path: Path?, clip: Region?): Boolean {
        return try {
            Stats.onNativeCall()
            _nSetPath(
                _ptr,
                Native.Companion.getPtr(path),
                Native.Companion.getPtr(clip)
            )
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(path)
            Reference.reachabilityFence(clip)
        }
    }

    fun intersects(rect: IRect): Boolean {
        return try {
            Stats.onNativeCall()
            _nIntersectsIRect(_ptr, rect.left, rect.top, rect.right, rect.bottom)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun intersects(r: Region?): Boolean {
        return try {
            Stats.onNativeCall()
            _nIntersectsRegion(
                _ptr,
                Native.Companion.getPtr(r)
            )
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(r)
        }
    }

    fun contains(x: Int, y: Int): Boolean {
        return try {
            Stats.onNativeCall()
            _nContainsIPoint(_ptr, x, y)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    operator fun contains(rect: IRect): Boolean {
        return try {
            Stats.onNativeCall()
            _nContainsIRect(_ptr, rect.left, rect.top, rect.right, rect.bottom)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    operator fun contains(r: Region?): Boolean {
        return try {
            Stats.onNativeCall()
            _nContainsRegion(_ptr, Native.Companion.getPtr(r))
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(r)
        }
    }

    fun quickContains(rect: IRect): Boolean {
        return try {
            Stats.onNativeCall()
            _nQuickContains(_ptr, rect.left, rect.top, rect.right, rect.bottom)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun quickReject(rect: IRect): Boolean {
        return try {
            Stats.onNativeCall()
            _nQuickRejectIRect(_ptr, rect.left, rect.top, rect.right, rect.bottom)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun quickReject(r: Region?): Boolean {
        return try {
            Stats.onNativeCall()
            _nQuickRejectRegion(
                _ptr,
                Native.Companion.getPtr(r)
            )
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(r)
        }
    }

    fun translate(dx: Int, dy: Int) {
        try {
            Stats.onNativeCall()
            _nTranslate(_ptr, dx, dy)
        } finally {
            Reference.reachabilityFence(this)
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
            Reference.reachabilityFence(this)
        }
    }

    fun op(r: Region?, op: Op): Boolean {
        return try {
            Stats.onNativeCall()
            _nOpRegion(
                _ptr,
                Native.Companion.getPtr(r),
                op.ordinal
            )
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(r)
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
                Native.Companion.getPtr(r),
                op.ordinal
            )
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(r)
        }
    }

    fun op(r: Region?, rect: IRect, op: Op): Boolean {
        return try {
            Stats.onNativeCall()
            _nOpRegionIRect(
                _ptr,
                Native.Companion.getPtr(r),
                rect.left,
                rect.top,
                rect.right,
                rect.bottom,
                op.ordinal
            )
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(r)
        }
    }

    fun op(a: Region?, b: Region?, op: Op): Boolean {
        return try {
            Stats.onNativeCall()
            _nOpRegionRegion(
                _ptr,
                Native.Companion.getPtr(a),
                Native.Companion.getPtr(b),
                op.ordinal
            )
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(a)
            Reference.reachabilityFence(b)
        }
    }

    @ApiStatus.Internal
    object _FinalizerHolder {
        val PTR = _nGetFinalizer()
    }

    init {
        Stats.onNativeCall()
    }
}