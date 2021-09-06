@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia.svg

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.*
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import kotlin.jvm.JvmStatic

class SVGSVG internal constructor(ptr: Long) : SVGContainer(ptr) {
    companion object {
        @JvmStatic
        external fun _nGetX(ptr: Long): SVGLength
        @JvmStatic external fun _nGetY(ptr: Long): SVGLength
        @JvmStatic external fun _nGetWidth(ptr: Long): SVGLength
        @JvmStatic external fun _nGetHeight(ptr: Long): SVGLength
        @JvmStatic external fun _nGetPreserveAspectRatio(ptr: Long): SVGPreserveAspectRatio
        @JvmStatic external fun _nGetViewBox(ptr: Long): Rect?
        @JvmStatic external fun _nGetIntrinsicSize(ptr: Long, width: Float, height: Float, dpi: Float): Point
        @JvmStatic external fun _nSetX(ptr: Long, value: Float, unit: Int)
        @JvmStatic external fun _nSetY(ptr: Long, value: Float, unit: Int)
        @JvmStatic external fun _nSetWidth(ptr: Long, value: Float, unit: Int)
        @JvmStatic external fun _nSetHeight(ptr: Long, value: Float, unit: Int)
        @JvmStatic external fun _nSetPreserveAspectRatio(ptr: Long, align: Int, scale: Int)
        @JvmStatic external fun _nSetViewBox(ptr: Long, l: Float, t: Float, r: Float, b: Float)

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