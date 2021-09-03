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

    var x: SVGLength
        get() = try {
            Stats.onNativeCall()
            _nGetX(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
        set(value) {
            try {
                Stats.onNativeCall()
                _nSetX(_ptr, value.value, value._unit.ordinal)
            } finally {
                Reference.reachabilityFence(this)
            }
        }

    var y: SVGLength
        get() = try {
            Stats.onNativeCall()
            _nGetY(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
        set(value) {
            try {
                Stats.onNativeCall()
                _nSetY(_ptr, value.value, value._unit.ordinal)
            } finally {
                Reference.reachabilityFence(this)
            }
        }

    var width: SVGLength
        get() = try {
            Stats.onNativeCall()
            _nGetWidth(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
        set(value) {
            try {
                Stats.onNativeCall()
                _nSetWidth(_ptr, value.value, value._unit.ordinal)
            } finally {
                Reference.reachabilityFence(this)
            }
        }

    var height: SVGLength
        get() = try {
            Stats.onNativeCall()
            _nGetHeight(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
        set(value) {
            try {
                Stats.onNativeCall()
                _nSetHeight(_ptr, value.value, value._unit.ordinal)
            } finally {
                Reference.reachabilityFence(this)
            }
        }

    var preserveAspectRatio: SVGPreserveAspectRatio
        get() = try {
            Stats.onNativeCall()
            _nGetPreserveAspectRatio(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
        set(value) {
            try {
                Stats.onNativeCall()
                _nSetPreserveAspectRatio(_ptr, value._align._value, value._scale.ordinal)
            } finally {
                Reference.reachabilityFence(this)
            }
        }

    var viewBox: Rect?
        get() = try {
            Stats.onNativeCall()
            _nGetViewBox(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
        set(value) {
            try {
                assert(value != null) { "Can’t set viewBox with value == null" }
                Stats.onNativeCall()
                _nSetViewBox(_ptr, value!!.left, value.top, value.right, value.bottom)
            } finally {
                Reference.reachabilityFence(this)
            }
        }

    fun getIntrinsicSize(lc: SVGLengthContext): Point {
        return try {
            Stats.onNativeCall()
            _nGetIntrinsicSize(_ptr, lc.width, lc.height, lc.dpi)
        } finally {
            Reference.reachabilityFence(this)
        }
    }
}