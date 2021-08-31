package org.jetbrains.skija.svg

import org.jetbrains.skija.impl.Library.Companion.staticLoad
import org.jetbrains.skija.impl.RefCnt
import org.jetbrains.skija.*
import org.jetbrains.skija.impl.Native
import org.jetbrains.skija.impl.Stats
import java.lang.ref.Reference

class SVGDOM internal constructor(ptr: Long) : RefCnt(ptr) {
    companion object {
        @JvmStatic external fun _nMakeFromData(dataPtr: Long): Long
        @JvmStatic external fun _nGetRoot(ptr: Long): Long
        @JvmStatic external fun _nGetContainerSize(ptr: Long): Point
        @JvmStatic external fun _nSetContainerSize(ptr: Long, width: Float, height: Float)
        @JvmStatic external fun _nRender(ptr: Long, canvasPtr: Long)

        init {
            staticLoad()
        }
    }

    constructor(data: Data) : this(_nMakeFromData(Native.getPtr(data))) {
        Stats.onNativeCall()
        Reference.reachabilityFence(data)
    }

    val root: org.jetbrains.skija.svg.SVGSVG?
        get() = try {
            Stats.onNativeCall()
            val ptr = _nGetRoot(_ptr)
            if (ptr == 0L) null else SVGSVG(ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    /**
     * Deprecated. Use getRoot().intrinsicSize() instead
     */
    @get:Deprecated("")
    val containerSize: Point
        get() = try {
            _nGetContainerSize(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setContainerSize(width: Float, height: Float): SVGDOM {
        Stats.onNativeCall()
        _nSetContainerSize(_ptr, width, height)
        return this
    }

    fun setContainerSize(size: Point): SVGDOM {
        Stats.onNativeCall()
        _nSetContainerSize(_ptr, size.x, size.y)
        return this
    }

    // sk_sp<SkSVGNode>* findNodeById(const char* id);
    fun render(canvas: Canvas): SVGDOM {
        return try {
            Stats.onNativeCall()
            _nRender(_ptr, Native.Companion.getPtr(canvas))
            this
        } finally {
            Reference.reachabilityFence(canvas)
        }
    }
}