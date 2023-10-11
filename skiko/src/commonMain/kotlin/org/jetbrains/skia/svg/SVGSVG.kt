package org.jetbrains.skia.svg

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.*
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ModuleImport
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
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_svg_SVGSVG__1nGetX")
private external fun SVGSVG_nGetX(ptr: NativePointer, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nGetY")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_svg_SVGSVG__1nGetY")
private external fun SVGSVG_nGetY(ptr: NativePointer, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nGetWidth")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_svg_SVGSVG__1nGetWidth")
private external fun SVGSVG_nGetWidth(ptr: NativePointer, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nGetHeight")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_svg_SVGSVG__1nGetHeight")
private external fun SVGSVG_nGetHeight(ptr: NativePointer, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nGetPreserveAspectRatio")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_svg_SVGSVG__1nGetPreserveAspectRatio")
private external fun SVGSVG_nGetPreserveAspectRatio(ptr: NativePointer, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nGetViewBox")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_svg_SVGSVG__1nGetViewBox")
private external fun SVGSVG_nGetViewBox(ptr: NativePointer, result: InteropPointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nGetIntrinsicSize")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_svg_SVGSVG__1nGetIntrinsicSize")
private external fun SVGSVG_nGetIntrinsicSize(ptr: NativePointer, width: Float, height: Float, dpi: Float, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nSetX")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_svg_SVGSVG__1nSetX")
private external fun SVGSVG_nSetX(ptr: NativePointer, value: Float, unit: Int)

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nSetY")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_svg_SVGSVG__1nSetY")
private external fun SVGSVG_nSetY(ptr: NativePointer, value: Float, unit: Int)

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nSetWidth")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_svg_SVGSVG__1nSetWidth")
private external fun SVGSVG_nSetWidth(ptr: NativePointer, value: Float, unit: Int)

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nSetHeight")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_svg_SVGSVG__1nSetHeight")
private external fun SVGSVG_nSetHeight(ptr: NativePointer, value: Float, unit: Int)

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nSetPreserveAspectRatio")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_svg_SVGSVG__1nSetPreserveAspectRatio")
private external fun SVGSVG_nSetPreserveAspectRatio(ptr: NativePointer, align: Int, scale: Int)

@ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nSetViewBox")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_svg_SVGSVG__1nSetViewBox")
private external fun SVGSVG_nSetViewBox(ptr: NativePointer, l: Float, t: Float, r: Float, b: Float)
