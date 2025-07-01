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