package org.jetbrains.skia.svg

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.*
import org.jetbrains.skia.impl.Stats
import java.lang.ref.Reference

class SVGSVG internal constructor(ptr: Long) : SVGContainer(ptr) {
    companion object {
        @JvmStatic external fun _nGetX(ptr: Long): SVGLength
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

    val x: SVGLength
        get() = try {
            Stats.onNativeCall()
            _nGetX(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    val y: SVGLength
        get() = try {
            Stats.onNativeCall()
            _nGetY(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    val width: SVGLength
        get() = try {
            Stats.onNativeCall()
            _nGetWidth(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    val height: SVGLength
        get() = try {
            Stats.onNativeCall()
            _nGetHeight(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    val preserveAspectRatio: SVGPreserveAspectRatio
        get() = try {
            Stats.onNativeCall()
            _nGetPreserveAspectRatio(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    val viewBox: Rect?
        get() = try {
            Stats.onNativeCall()
            _nGetViewBox(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun getIntrinsicSize(lc: SVGLengthContext): Point {
        return try {
            Stats.onNativeCall()
            _nGetIntrinsicSize(_ptr, lc.width, lc.height, lc.dpi)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun setX(length: SVGLength): SVGSVG {
        return try {
            Stats.onNativeCall()
            _nSetX(_ptr, length.value, length._unit.ordinal)
            this
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun setY(length: SVGLength): SVGSVG {
        return try {
            Stats.onNativeCall()
            _nSetY(_ptr, length.value, length._unit.ordinal)
            this
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun setWidth(length: SVGLength): SVGSVG {
        return try {
            Stats.onNativeCall()
            _nSetWidth(_ptr, length.value, length._unit.ordinal)
            this
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun setHeight(length: SVGLength): SVGSVG {
        return try {
            Stats.onNativeCall()
            _nSetHeight(_ptr, length.value, length._unit.ordinal)
            this
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun setPreserveAspectRatio(ratio: SVGPreserveAspectRatio): SVGSVG {
        return try {
            Stats.onNativeCall()
            _nSetPreserveAspectRatio(_ptr, ratio._align._value, ratio._scale.ordinal)
            this
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun setViewBox(viewBox: Rect): SVGSVG {
        return try {
            Stats.onNativeCall()
            _nSetViewBox(_ptr, viewBox.left, viewBox.top, viewBox.right, viewBox.bottom)
            this
        } finally {
            Reference.reachabilityFence(this)
        }
    }
}