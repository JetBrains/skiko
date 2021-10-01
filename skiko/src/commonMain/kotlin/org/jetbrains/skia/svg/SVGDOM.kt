package org.jetbrains.skia.svg

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.RefCnt
import org.jetbrains.skia.*
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.getPtr

class SVGDOM internal constructor(ptr: NativePointer) : RefCnt(ptr) {
    companion object {
        init {
            staticLoad()
        }
    }

    constructor(data: Data) : this(SVGDOM_nMakeFromData(getPtr(data))) {
        Stats.onNativeCall()
        reachabilityBarrier(data)
    }

    val root: SVGSVG?
        get() = try {
            Stats.onNativeCall()
            val ptr = SVGDOM_nGetRoot(_ptr)
            if (ptr == NullPointer) null else SVGSVG(ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * Deprecated. Use getRoot().intrinsicSize() instead
     */
    @get:Deprecated("")
    val containerSize: Point
        get() = try {
            SVGDOM_nGetContainerSize(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    fun setContainerSize(width: Float, height: Float): SVGDOM {
        Stats.onNativeCall()
        SVGDOM_nSetContainerSize(_ptr, width, height)
        return this
    }

    fun setContainerSize(size: Point): SVGDOM {
        Stats.onNativeCall()
        SVGDOM_nSetContainerSize(_ptr, size.x, size.y)
        return this
    }

    // sk_sp<SkSVGNode>* findNodeById(const char* id);
    fun render(canvas: Canvas): SVGDOM {
        return try {
            Stats.onNativeCall()
            SVGDOM_nRender(_ptr, getPtr(canvas))
            this
        } finally {
            reachabilityBarrier(canvas)
        }
    }
}


@ExternalSymbolName("org_jetbrains_skia_svg_SVGDOM__1nMakeFromData")
private external fun SVGDOM_nMakeFromData(dataPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_svg_SVGDOM__1nGetRoot")
private external fun SVGDOM_nGetRoot(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_svg_SVGDOM__1nGetContainerSize")
private external fun SVGDOM_nGetContainerSize(ptr: NativePointer): Point

@ExternalSymbolName("org_jetbrains_skia_svg_SVGDOM__1nSetContainerSize")
private external fun SVGDOM_nSetContainerSize(ptr: NativePointer, width: Float, height: Float)

@ExternalSymbolName("org_jetbrains_skia_svg_SVGDOM__1nRender")
private external fun SVGDOM_nRender(ptr: NativePointer, canvasPtr: NativePointer)
