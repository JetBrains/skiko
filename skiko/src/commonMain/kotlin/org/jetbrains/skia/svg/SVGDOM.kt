package org.jetbrains.skia.svg

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.*
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ForceNativeThreadState
import org.jetbrains.skia.impl.*

private fun makeSVGDOM(data: Data): NativePointer =
    nativeCallFromStatic(data) { data -> SVGDOM_nMakeFromData(data.pointerOrNullPointer) }

class SVGDOM internal constructor(ptr: NativePointer) : RefCnt(ptr) {
    companion object {
        init {
            staticLoad()
        }
    }

    constructor(data: Data) : this(makeSVGDOM(data))

    val root: SVGSVG?
        get() = nativeCall {
            val ptr = SVGDOM_nGetRoot(_ptr)
            if (ptr == NullPointer) null else SVGSVG(ptr)
        }

    /**
     * Deprecated. Use getRoot().intrinsicSize() instead
     */
    @get:Deprecated("")
    val containerSize: Point
        get() = nativeCall { Point.fromInteropPointer { SVGDOM_nGetContainerSize(_ptr, it) } }

    fun setContainerSize(width: Float, height: Float) =
        nativeCall { SVGDOM_nSetContainerSize(_ptr, width, height) }

    fun setContainerSize(size: Point) =
        nativeCall { SVGDOM_nSetContainerSize(_ptr, size.x, size.y) }

    // sk_sp<SkSVGNode>* findNodeById(const char* id);
    fun render(canvas: Canvas): SVGDOM =
        nativeCall(canvas) { canvas -> SVGDOM_nRender(_ptr, canvas.pointerOrNullPointer); this }
}

@ExternalSymbolName("org_jetbrains_skia_svg_SVGDOM__1nMakeFromData")
// Needs ForceNativeThreadState: parses the whole XML document and builds its node tree, so its time grows with the data
// size.
// Native call path: SkSVGDOM::MakeFromStream
@ForceNativeThreadState
private external fun SVGDOM_nMakeFromData(dataPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_svg_SVGDOM__1nGetRoot")
// No ForceNativeThreadState: returns the stored root and bumps its reference counter, so it is short and cannot block.
// Native call path: SkSVGDOM::getRoot
private external fun SVGDOM_nGetRoot(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_svg_SVGDOM__1nGetContainerSize")
// No ForceNativeThreadState: only returns a stored container size, so it is short and cannot block.
// Native call path: SkSVGDOM::containerSize
private external fun SVGDOM_nGetContainerSize(ptr: NativePointer, dst: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_svg_SVGDOM__1nSetContainerSize")
// No ForceNativeThreadState: only assigns a stored container size, so it is short and cannot block.
// Native call path: SkSVGDOM::setContainerSize
private external fun SVGDOM_nSetContainerSize(ptr: NativePointer, width: Float, height: Float)

@ExternalSymbolName("org_jetbrains_skia_svg_SVGDOM__1nRender")
// Needs ForceNativeThreadState: draws the SVG tree onto a canvas that can be GPU-backed, and its time grows with the
// size of the tree.
// Native call path: SkSVGDOM::render
@ForceNativeThreadState
private external fun SVGDOM_nRender(ptr: NativePointer, canvasPtr: NativePointer)
