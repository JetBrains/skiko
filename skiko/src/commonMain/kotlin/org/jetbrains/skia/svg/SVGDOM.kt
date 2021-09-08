@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia.svg

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.RefCnt
import org.jetbrains.skia.*
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.NULLPNTR
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.getPtr
import kotlin.jvm.JvmStatic

class SVGDOM internal constructor(ptr: NativePointer) : RefCnt(ptr) {
    companion object {
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_svg_SVGDOM__1nMakeFromData")
        external fun _nMakeFromData(dataPtr: NativePointer): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_svg_SVGDOM__1nGetRoot")
        external fun _nGetRoot(ptr: NativePointer): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_svg_SVGDOM__1nGetContainerSize")
        external fun _nGetContainerSize(ptr: NativePointer): Point
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_svg_SVGDOM__1nSetContainerSize")
        external fun _nSetContainerSize(ptr: NativePointer, width: Float, height: Float)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_svg_SVGDOM__1nRender")
        external fun _nRender(ptr: NativePointer, canvasPtr: NativePointer)

        init {
            staticLoad()
        }
    }

    constructor(data: Data) : this(_nMakeFromData(getPtr(data))) {
        Stats.onNativeCall()
        reachabilityBarrier(data)
    }

    val root: SVGSVG?
        get() = try {
            Stats.onNativeCall()
            val ptr = _nGetRoot(_ptr)
            if (ptr == NULLPNTR) null else SVGSVG(ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * Deprecated. Use getRoot().intrinsicSize() instead
     */
    @get:Deprecated("")
    val containerSize: Point
        get() = try {
            _nGetContainerSize(_ptr)
        } finally {
            reachabilityBarrier(this)
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
            _nRender(_ptr, getPtr(canvas))
            this
        } finally {
            reachabilityBarrier(canvas)
        }
    }
}