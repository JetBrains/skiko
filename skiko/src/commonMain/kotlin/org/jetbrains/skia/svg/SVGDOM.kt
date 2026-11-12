package org.jetbrains.skia.svg

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.*
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ForceNativeThreadState
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

    /**
     * Parses [data] without a font manager.
     *
     * `<text>` nodes that need a font are not drawn. Use [Builder.setFontManager] when the SVG
     * contains text.
     */
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

    /**
     * Builds an [SVGDOM] the same way as [SkSVGDOM.Builder](https://api.skia.org/classSkSVGDOM_1_1Builder.html).
     *
     * A font manager has to be set before [buildFromData] for `<text>` to be drawn. The manager is
     * referenced by the resulting [SVGDOM] for as long as that DOM lives, independently of this
     * builder.
     */
    class Builder internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
        private object _FinalizerHolder {
            val PTR = SVGDOM_nBuilderGetFinalizer()
        }

        constructor() : this(SVGDOM_nBuilderMake()) {
            Stats.onNativeCall()
        }

        /**
         * Font manager used to load fonts for `<text>` nodes.
         *
         * Without one, text that needs a font is not drawn. [FontMgr.default] resolves families
         * from the system; a [org.jetbrains.skia.paragraph.TypefaceFontProvider] resolves only the
         * typefaces registered on it.
         *
         * Passing `null` clears a manager set earlier.
         */
        fun setFontManager(fontMgr: FontMgr?): Builder {
            return try {
                Stats.onNativeCall()
                SVGDOM_nBuilderSetFontManager(_ptr, getPtr(fontMgr))
                this
            } finally {
                reachabilityBarrier(this)
                reachabilityBarrier(fontMgr)
            }
        }

        /**
         * Parses [data]. Returns `null` when the stream is not an SVG document with an `<svg>` root.
         */
        fun buildFromData(data: Data): SVGDOM? {
            return try {
                Stats.onNativeCall()
                val ptr = SVGDOM_nBuilderBuildFromData(_ptr, getPtr(data))
                if (ptr == NullPointer) null else SVGDOM(ptr)
            } finally {
                reachabilityBarrier(this)
                reachabilityBarrier(data)
            }
        }
    }
}

@ExternalSymbolName("org_jetbrains_skia_svg_SVGDOM__1nMakeFromData")
// Needs ForceNativeThreadState: parses the whole XML document and builds its node tree, so its time grows with the data
// size. Same path as Builder.buildFromData with no font manager.
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
// size of the tree. Text shaping goes through the font manager stored on the DOM.
// Native call path: SkSVGDOM::render
@ForceNativeThreadState
private external fun SVGDOM_nRender(ptr: NativePointer, canvasPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_svg_SVGDOM__1nBuilderGetFinalizer")
// No ForceNativeThreadState: returns the address of a delete function without calling it, so it is short and cannot
// block.
// Native call path: deleteSVGDOMBuilder
private external fun SVGDOM_nBuilderGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_svg_SVGDOM__1nBuilderMake")
// No ForceNativeThreadState: allocates an empty builder that owns nothing yet, so it is short and cannot block.
// Native call path: SkSVGDOM::Builder::Builder
private external fun SVGDOM_nBuilderMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_svg_SVGDOM__1nBuilderSetFontManager")
// Needs ForceNativeThreadState: can drop the previous font manager, which releases CoreText font collections and
// typefaces.
// Native call path: SkSVGDOM::Builder::setFontManager -> ~SkFontMgr
@ForceNativeThreadState
private external fun SVGDOM_nBuilderSetFontManager(ptr: NativePointer, fontMgrPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_svg_SVGDOM__1nBuilderBuildFromData")
// Needs ForceNativeThreadState: parses the whole XML document and builds its node tree, so its time grows with the data
// size.
// Native call path: SkSVGDOM::Builder::make
@ForceNativeThreadState
private external fun SVGDOM_nBuilderBuildFromData(ptr: NativePointer, dataPtr: NativePointer): NativePointer
