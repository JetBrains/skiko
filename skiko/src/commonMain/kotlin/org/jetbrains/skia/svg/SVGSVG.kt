@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia.svg

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.*
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.NativePointer
import kotlin.jvm.JvmStatic

class SVGSVG internal constructor(ptr: NativePointer) : SVGContainer(ptr) {
    companion object {
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nGetX")
        external fun _nGetX(ptr: NativePointer): SVGLength
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nGetY")
        external fun _nGetY(ptr: NativePointer): SVGLength
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nGetWidth")
        external fun _nGetWidth(ptr: NativePointer): SVGLength
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nGetHeight")
        external fun _nGetHeight(ptr: NativePointer): SVGLength
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nGetPreserveAspectRatio")
        external fun _nGetPreserveAspectRatio(ptr: NativePointer): SVGPreserveAspectRatio
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nGetViewBox")
        external fun _nGetViewBox(ptr: NativePointer): Rect?
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nGetIntrinsicSize")
        external fun _nGetIntrinsicSize(ptr: NativePointer, width: Float, height: Float, dpi: Float): Point
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nSetX")
        external fun _nSetX(ptr: NativePointer, value: Float, unit: Int)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nSetY")
        external fun _nSetY(ptr: NativePointer, value: Float, unit: Int)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nSetWidth")
        external fun _nSetWidth(ptr: NativePointer, value: Float, unit: Int)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nSetHeight")
        external fun _nSetHeight(ptr: NativePointer, value: Float, unit: Int)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nSetPreserveAspectRatio")
        external fun _nSetPreserveAspectRatio(ptr: NativePointer, align: Int, scale: Int)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_svg_SVGSVG__1nSetViewBox")
        external fun _nSetViewBox(ptr: NativePointer, l: Float, t: Float, r: Float, b: Float)

        init {
            staticLoad()
        }
    }

    var x: SVGLength
        get() = try {
            Stats.onNativeCall()
            _nGetX(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setX(value)
        }
    
    fun setX(length: SVGLength): SVGSVG {
        return try {
            Stats.onNativeCall()
            _nSetX(_ptr, length.value, length._unit.ordinal)
            this
        } finally {
            reachabilityBarrier(this)
        }
    }

    var y: SVGLength
        get() = try {
            Stats.onNativeCall()
            _nGetY(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setY(value)
        }
    
    fun setY(length: SVGLength): SVGSVG {
        return try {
            Stats.onNativeCall()
            _nSetY(_ptr, length.value, length._unit.ordinal)
            this
        } finally {
            reachabilityBarrier(this)
        }
    }

    var width: SVGLength
        get() = try {
            Stats.onNativeCall()
            _nGetWidth(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setWidth(value)
        }
    
    fun setWidth(length: SVGLength): SVGSVG {
        return try {
            Stats.onNativeCall()
            _nSetWidth(_ptr, length.value, length._unit.ordinal)
            this
        } finally {
            reachabilityBarrier(this)
        }
    }

    var height: SVGLength
        get() = try {
            Stats.onNativeCall()
            _nGetHeight(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setHeight(value)
        }

    fun setHeight(length: SVGLength): SVGSVG {
        return try {
            Stats.onNativeCall()
            _nSetHeight(_ptr, length.value, length._unit.ordinal)
            this
        } finally {
            reachabilityBarrier(this)
        }
    }

    var preserveAspectRatio: SVGPreserveAspectRatio
        get() = try {
            Stats.onNativeCall()
            _nGetPreserveAspectRatio(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setPreserveAspectRatio(value)
        }

    fun setPreserveAspectRatio(ratio: SVGPreserveAspectRatio): SVGSVG {
        return try {
            Stats.onNativeCall()
            _nSetPreserveAspectRatio(_ptr, ratio._align._value, ratio._scale.ordinal)
            this
        } finally {
            reachabilityBarrier(this)
        }
    }

    var viewBox: Rect?
        get() = try {
            Stats.onNativeCall()
            _nGetViewBox(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            require(value != null) { "Can't set viewBox with value == null" }
            setViewBox(value)
        }
    
    fun setViewBox(viewBox: Rect): SVGSVG {
        return try {
            Stats.onNativeCall()
            _nSetViewBox(_ptr, viewBox.left, viewBox.top, viewBox.right, viewBox.bottom)
            this
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun getIntrinsicSize(lc: SVGLengthContext): Point {
        return try {
            Stats.onNativeCall()
            _nGetIntrinsicSize(_ptr, lc.width, lc.height, lc.dpi)
        } finally {
            reachabilityBarrier(this)
        }
    }
}