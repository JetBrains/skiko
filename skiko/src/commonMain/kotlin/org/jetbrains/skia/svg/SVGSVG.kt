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
        get() = try {
            Stats.onNativeCall()
            SVGLength.fromInterop { SVGSVG_nGetX(_ptr, it) }
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            SVGSVG_nSetX(_ptr, value.value, value.unit.ordinal)
        } finally {
            reachabilityBarrier(this)
        }

    var y: SVGLength
        get() = try {
            Stats.onNativeCall()
            SVGLength.fromInterop { SVGSVG_nGetY(_ptr, it) }
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setY(value)
        }

    fun setY(length: SVGLength): SVGSVG {
        return try {
            Stats.onNativeCall()
            SVGSVG_nSetY(_ptr, length.value, length.unit.ordinal)
            this
        } finally {
            reachabilityBarrier(this)
        }
    }

    var width: SVGLength
        get() = try {
            Stats.onNativeCall()
            SVGLength.fromInterop { SVGSVG_nGetWidth(_ptr, it) }
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            SVGSVG_nSetWidth(_ptr, value.value, value.unit.ordinal)
        } finally {
            reachabilityBarrier(this)
        }

    var height: SVGLength
        get() = try {
            Stats.onNativeCall()
            SVGLength.fromInterop { SVGSVG_nGetHeight(_ptr, it) }
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            SVGSVG_nSetHeight(_ptr, value.value, value.unit.ordinal)
        } finally {
            reachabilityBarrier(this)
        }


    var preserveAspectRatio: SVGPreserveAspectRatio
        get() = try {
            Stats.onNativeCall()
            SVGPreserveAspectRatio.fromInterop { SVGSVG_nGetPreserveAspectRatio(_ptr, it) }
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            SVGSVG_nSetPreserveAspectRatio(_ptr, value._align._value, value._scale.ordinal)
        } finally {
            reachabilityBarrier(this)
        }

    var viewBox: Rect?
        get() = try {
            Stats.onNativeCall()
            Rect.fromInteropPointerNullable { SVGSVG_nGetViewBox(_ptr, it) }
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            SVGSVG_nSetViewBox(_ptr, value!!.left, value.top, value.right, value.bottom)
        } finally {
            reachabilityBarrier(this)
        }

    fun getIntrinsicSize(lc: SVGLengthContext): Point {
        return try {
            Stats.onNativeCall()
            Point.fromInteropPointer { SVGSVG_nGetIntrinsicSize(_ptr, lc.width, lc.height, lc.dpi, it) }
        } finally {
            reachabilityBarrier(this)
        }
    }
}

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
