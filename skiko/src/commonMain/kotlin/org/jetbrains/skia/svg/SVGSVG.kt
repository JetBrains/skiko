package org.jetbrains.skia.svg

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.*
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.NativePointer

class SVGSVG internal constructor(ptr: NativePointer) : SVGContainer(ptr) {
    companion object {
        init {
            staticLoad()
        }
    }

    var x: SVGLength
        get() = try {
            Stats.onNativeCall()
            SVGSVG_nGetX(_ptr)
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
            SVGSVG_nGetY(_ptr)
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
            SVGSVG_nGetWidth(_ptr)
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
            SVGSVG_nGetHeight(_ptr)
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
            SVGSVG_nGetPreserveAspectRatio(_ptr)
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
            SVGSVG_nGetViewBox(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            SVGSVG_nSetViewBox(_ptr, value!!.left, value.top, value.right, viewBox!!.bottom)
        } finally {
            reachabilityBarrier(this)
        }

    fun getIntrinsicSize(lc: SVGLengthContext): Point {
        return try {
            Stats.onNativeCall()
            SVGSVG_nGetIntrinsicSize(_ptr, lc.width, lc.height, lc.dpi)
        } finally {
            reachabilityBarrier(this)
        }
    }
}


@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nGetX")
private external fun SVGSVG_nGetX(ptr: NativePointer): SVGLength

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nGetY")
private external fun SVGSVG_nGetY(ptr: NativePointer): SVGLength

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nGetWidth")
private external fun SVGSVG_nGetWidth(ptr: NativePointer): SVGLength

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nGetHeight")
private external fun SVGSVG_nGetHeight(ptr: NativePointer): SVGLength

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nGetPreserveAspectRatio")
private external fun SVGSVG_nGetPreserveAspectRatio(ptr: NativePointer): SVGPreserveAspectRatio

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nGetViewBox")
private external fun SVGSVG_nGetViewBox(ptr: NativePointer): Rect?

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nGetIntrinsicSize")
private external fun SVGSVG_nGetIntrinsicSize(ptr: NativePointer, width: Float, height: Float, dpi: Float): Point

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nSetX")
private external fun SVGSVG_nSetX(ptr: NativePointer, value: Float, unit: Int)

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nSetY")
private external fun SVGSVG_nSetY(ptr: NativePointer, value: Float, unit: Int)

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nSetWidth")
private external fun SVGSVG_nSetWidth(ptr: NativePointer, value: Float, unit: Int)

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nSetHeight")
private external fun SVGSVG_nSetHeight(ptr: NativePointer, value: Float, unit: Int)

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nSetPreserveAspectRatio")
private external fun SVGSVG_nSetPreserveAspectRatio(ptr: NativePointer, align: Int, scale: Int)

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nSetViewBox")
private external fun SVGSVG_nSetViewBox(ptr: NativePointer, l: Float, t: Float, r: Float, b: Float)
