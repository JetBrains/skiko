package org.jetbrains.skia.svg

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.*
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ModuleImport
import org.jetbrains.skia.impl.*

private fun makeSVGDOM(data: Data): NativePointer {
    Stats.onNativeCall()
    return try {
        SVGDOM_nMakeFromData(getPtr(data))
    } finally {
        reachabilityBarrier(data)
    }
}

class SVGDOM internal constructor(ptr: NativePointer) : RefCnt(ptr) {
    companion object {
        init {
            staticLoad()
        }
    }

    constructor(data: Data) : this(makeSVGDOM(data))

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
            Point.fromInteropPointer { SVGDOM_nGetContainerSize(_ptr, it) }
        } finally {
            reachabilityBarrier(this)
        }

    fun setContainerSize(width: Float, height: Float) {
        Stats.onNativeCall()
        SVGDOM_nSetContainerSize(_ptr, width, height)
    }

    fun setContainerSize(size: Point) {
        Stats.onNativeCall()
        SVGDOM_nSetContainerSize(_ptr, size.x, size.y)
    }

    // sk_sp<SkSVGNode>* findNodeById(const char* id);
    fun render(canvas: Canvas): SVGDOM {
        return try {
            Stats.onNativeCall()
            SVGDOM_nRender(_ptr, getPtr(canvas))
            this
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(canvas)
        }
    }
}

@ExternalSymbolName("org_jetbrains_skia_svg_SVGDOM__1nMakeFromData")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_svg_SVGDOM__1nMakeFromData")
private external fun SVGDOM_nMakeFromData(dataPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_svg_SVGDOM__1nGetRoot")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_svg_SVGDOM__1nGetRoot")
private external fun SVGDOM_nGetRoot(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_svg_SVGDOM__1nGetContainerSize")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_svg_SVGDOM__1nGetContainerSize")
private external fun SVGDOM_nGetContainerSize(ptr: NativePointer, dst: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_svg_SVGDOM__1nSetContainerSize")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_svg_SVGDOM__1nSetContainerSize")
private external fun SVGDOM_nSetContainerSize(ptr: NativePointer, width: Float, height: Float)

@ExternalSymbolName("org_jetbrains_skia_svg_SVGDOM__1nRender")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_svg_SVGDOM__1nRender")
private external fun SVGDOM_nRender(ptr: NativePointer, canvasPtr: NativePointer)
