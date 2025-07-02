package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.impl.withResult

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