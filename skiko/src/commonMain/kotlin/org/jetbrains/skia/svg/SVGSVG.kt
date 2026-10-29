package org.jetbrains.skia.svg

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.*
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.*

class SVGSVG internal constructor(ptr: NativePointer) : SVGContainer(ptr) {
    companion object {
        init {
            staticLoad()
        }
    }

    var x: SVGLength
        get() = nativeCall { SVGLength.fromInterop { SVGSVG_nGetX(_ptr, it) } }
        set(value) = nativeCall { SVGSVG_nSetX(_ptr, value.value, value.unit.ordinal) }

    var y: SVGLength
        get() = nativeCall { SVGLength.fromInterop { SVGSVG_nGetY(_ptr, it) } }
        set(value) = nativeCall { SVGSVG_nSetY(_ptr, value.value, value.unit.ordinal) }

    var width: SVGLength
        get() = nativeCall { SVGLength.fromInterop { SVGSVG_nGetWidth(_ptr, it) } }
        set(value) = nativeCall { SVGSVG_nSetWidth(_ptr, value.value, value.unit.ordinal) }

    var height: SVGLength
        get() = nativeCall { SVGLength.fromInterop { SVGSVG_nGetHeight(_ptr, it) } }
        set(value) = nativeCall { SVGSVG_nSetHeight(_ptr, value.value, value.unit.ordinal) }


    var preserveAspectRatio: SVGPreserveAspectRatio
        get() = nativeCall { SVGPreserveAspectRatio.fromInterop { SVGSVG_nGetPreserveAspectRatio(_ptr, it) } }
        set(value) = nativeCall { SVGSVG_nSetPreserveAspectRatio(_ptr, value.align._value, value.scale.ordinal) }

    var viewBox: Rect?
        get() = nativeCall { Rect.fromInteropPointerNullable { SVGSVG_nGetViewBox(_ptr, it) } }
        set(value) = nativeCall { SVGSVG_nSetViewBox(_ptr, value!!.left, value.top, value.right, value.bottom) }

    fun getIntrinsicSize(lc: SVGLengthContext): Point =
        nativeCall { Point.fromInteropPointer { SVGSVG_nGetIntrinsicSize(_ptr, lc.width, lc.height, lc.dpi, it) } }
}

// <editor-fold defaultstate="collapsed" desc="Native External Functions">
@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nGetX")
// No ForceNativeThreadState: returns a stored length and copies two ints out, so it is short and cannot block.
// Native call path: SkSVGSVG::getX
private external fun SVGSVG_nGetX(ptr: NativePointer, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nGetY")
// No ForceNativeThreadState: returns a stored length and copies two ints out, so it is short and cannot block.
// Native call path: SkSVGSVG::getY
private external fun SVGSVG_nGetY(ptr: NativePointer, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nGetWidth")
// No ForceNativeThreadState: returns a stored length and copies two ints out, so it is short and cannot block.
// Native call path: SkSVGSVG::getWidth
private external fun SVGSVG_nGetWidth(ptr: NativePointer, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nGetHeight")
// No ForceNativeThreadState: returns a stored length and copies two ints out, so it is short and cannot block.
// Native call path: SkSVGSVG::getHeight
private external fun SVGSVG_nGetHeight(ptr: NativePointer, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nGetPreserveAspectRatio")
// No ForceNativeThreadState: returns a stored field and copies two ints out, so it is short and cannot block.
// Native call path: SkSVGSVG::getPreserveAspectRatio
private external fun SVGSVG_nGetPreserveAspectRatio(ptr: NativePointer, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nGetViewBox")
// No ForceNativeThreadState: returns a stored optional rectangle and copies it out, so it is short and cannot block.
// Native call path: SkSVGSVG::getViewBox
private external fun SVGSVG_nGetViewBox(ptr: NativePointer, result: InteropPointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nGetIntrinsicSize")
// Needs ForceNativeThreadState: for em or ex units in debug builds it can print to stderr, which can block on I/O.
// Native call path: SkSVGSVG::intrinsicSize -> SkSVGLengthContext::resolve -> SkDebugf -> SkLogVAList
@ForceNativeThreadState
private external fun SVGSVG_nGetIntrinsicSize(ptr: NativePointer, width: Float, height: Float, dpi: Float, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nSetX")
// No ForceNativeThreadState: assigns a small value into a stored field, so it is short and cannot block.
// Native call path: SkSVGSVG::setX
private external fun SVGSVG_nSetX(ptr: NativePointer, value: Float, unit: Int)

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nSetY")
// No ForceNativeThreadState: assigns a small value into a stored field, so it is short and cannot block.
// Native call path: SkSVGSVG::setY
private external fun SVGSVG_nSetY(ptr: NativePointer, value: Float, unit: Int)

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nSetWidth")
// No ForceNativeThreadState: assigns a small value into a stored field, so it is short and cannot block.
// Native call path: SkSVGSVG::setWidth
private external fun SVGSVG_nSetWidth(ptr: NativePointer, value: Float, unit: Int)

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nSetHeight")
// No ForceNativeThreadState: assigns a small value into a stored field, so it is short and cannot block.
// Native call path: SkSVGSVG::setHeight
private external fun SVGSVG_nSetHeight(ptr: NativePointer, value: Float, unit: Int)

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nSetPreserveAspectRatio")
// No ForceNativeThreadState: assigns a small value into a stored field, so it is short and cannot block.
// Native call path: SkSVGSVG::setPreserveAspectRatio
private external fun SVGSVG_nSetPreserveAspectRatio(ptr: NativePointer, align: Int, scale: Int)

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nSetViewBox")
// No ForceNativeThreadState: assigns a small value into a stored field, so it is short and cannot block.
// Native call path: SkSVGSVG::setViewBox
private external fun SVGSVG_nSetViewBox(ptr: NativePointer, l: Float, t: Float, r: Float, b: Float)
// </editor-fold>