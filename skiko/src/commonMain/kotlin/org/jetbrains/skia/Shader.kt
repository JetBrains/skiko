@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.RefCnt
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import kotlin.jvm.JvmStatic

class Shader internal constructor(ptr: Long) : RefCnt(ptr) {
    companion object {
        // Linear
        fun makeLinearGradient(p0: Point, p1: Point, colors: IntArray): Shader {
            return makeLinearGradient(p0.x, p0.y, p1.x, p1.y, colors)
        }

        fun makeLinearGradient(p0: Point, p1: Point, colors: IntArray, positions: FloatArray?): Shader {
            return makeLinearGradient(p0.x, p0.y, p1.x, p1.y, colors, positions)
        }

        fun makeLinearGradient(
            p0: Point,
            p1: Point,
            colors: IntArray,
            positions: FloatArray?,
            style: GradientStyle
        ): Shader {
            return makeLinearGradient(p0.x, p0.y, p1.x, p1.y, colors, positions, style)
        }

        fun makeLinearGradient(
            x0: Float,
            y0: Float,
            x1: Float,
            y1: Float,
            colors: IntArray,
            positions: FloatArray? = null,
            style: GradientStyle = GradientStyle.Companion.DEFAULT
        ): Shader {
            require(positions == null || colors.size == positions.size) { "colors.length " + colors.size + "!= positions.length " + positions!!.size }
            Stats.onNativeCall()
            return Shader(
                _nMakeLinearGradient(
                    x0,
                    y0,
                    x1,
                    y1,
                    colors,
                    positions,
                    style.tileMode.ordinal,
                    style._getFlags(),
                    style._getMatrixArray()
                )
            )
        }

        fun makeLinearGradient(
            p0: Point,
            p1: Point,
            colors: Array<Color4f>,
            cs: ColorSpace?,
            positions: FloatArray?,
            style: GradientStyle
        ): Shader {
            return makeLinearGradient(p0.x, p0.y, p1.x, p1.y, colors, cs, positions, style)
        }

        fun makeLinearGradient(
            x0: Float,
            y0: Float,
            x1: Float,
            y1: Float,
            colors: Array<Color4f>,
            cs: ColorSpace?,
            positions: FloatArray?,
            style: GradientStyle
        ): Shader {
            return try {
                require(positions == null || colors.size == positions.size) { "colors.length " + colors.size + "!= positions.length " + positions!!.size }
                Stats.onNativeCall()
                Shader(
                    _nMakeLinearGradientCS(
                        x0,
                        y0,
                        x1,
                        y1,
                        Color4f.flattenArray(colors),
                        getPtr(cs),
                        positions,
                        style.tileMode.ordinal,
                        style._getFlags(),
                        style._getMatrixArray()
                    )
                )
            } finally {
                reachabilityBarrier(cs)
            }
        }

        // Radial
        fun makeRadialGradient(center: Point, r: Float, colors: IntArray): Shader {
            return makeRadialGradient(center.x, center.y, r, colors)
        }

        fun makeRadialGradient(center: Point, r: Float, colors: IntArray, positions: FloatArray?): Shader {
            return makeRadialGradient(center.x, center.y, r, colors, positions)
        }

        fun makeRadialGradient(
            center: Point,
            r: Float,
            colors: IntArray,
            positions: FloatArray?,
            style: GradientStyle
        ): Shader {
            return makeRadialGradient(center.x, center.y, r, colors, positions, style)
        }

        fun makeRadialGradient(
            x: Float,
            y: Float,
            r: Float,
            colors: IntArray,
            positions: FloatArray? = null,
            style: GradientStyle = GradientStyle.Companion.DEFAULT
        ): Shader {
            require(positions == null || colors.size == positions.size) { "colors.length " + colors.size + "!= positions.length " + positions!!.size }
            Stats.onNativeCall()
            return Shader(
                _nMakeRadialGradient(
                    x,
                    y,
                    r,
                    colors,
                    positions,
                    style.tileMode.ordinal,
                    style._getFlags(),
                    style._getMatrixArray()
                )
            )
        }

        fun makeRadialGradient(
            center: Point,
            r: Float,
            colors: Array<Color4f>,
            cs: ColorSpace?,
            positions: FloatArray?,
            style: GradientStyle
        ): Shader {
            return makeRadialGradient(center.x, center.y, r, colors, cs, positions, style)
        }

        fun makeRadialGradient(
            x: Float,
            y: Float,
            r: Float,
            colors: Array<Color4f>,
            cs: ColorSpace?,
            positions: FloatArray?,
            style: GradientStyle
        ): Shader {
            return try {
                require(positions == null || colors.size == positions.size) { "colors.length " + colors.size + "!= positions.length " + positions!!.size }
                Stats.onNativeCall()
                Shader(
                    _nMakeRadialGradientCS(
                        x,
                        y,
                        r,
                        Color4f.flattenArray(colors),
                        getPtr(cs),
                        positions,
                        style.tileMode.ordinal,
                        style._getFlags(),
                        style._getMatrixArray()
                    )
                )
            } finally {
                reachabilityBarrier(cs)
            }
        }

        // Two-point Conical
        fun makeTwoPointConicalGradient(p0: Point, r0: Float, p1: Point, r1: Float, colors: IntArray): Shader {
            return makeTwoPointConicalGradient(p0.x, p0.y, r0, p1.x, p1.y, r1, colors)
        }

        fun makeTwoPointConicalGradient(
            p0: Point,
            r0: Float,
            p1: Point,
            r1: Float,
            colors: IntArray,
            positions: FloatArray?
        ): Shader {
            return makeTwoPointConicalGradient(p0.x, p0.y, r0, p1.x, p1.y, r1, colors, positions)
        }

        fun makeTwoPointConicalGradient(
            p0: Point,
            r0: Float,
            p1: Point,
            r1: Float,
            colors: IntArray,
            positions: FloatArray?,
            style: GradientStyle
        ): Shader {
            return makeTwoPointConicalGradient(p0.x, p0.y, r0, p1.x, p1.y, r1, colors, positions, style)
        }

        fun makeTwoPointConicalGradient(
            x0: Float,
            y0: Float,
            r0: Float,
            x1: Float,
            y1: Float,
            r1: Float,
            colors: IntArray,
            positions: FloatArray? = null,
            style: GradientStyle = GradientStyle.Companion.DEFAULT
        ): Shader {
            require(positions == null || colors.size == positions.size) { "colors.length " + colors.size + "!= positions.length " + positions!!.size }
            Stats.onNativeCall()
            return Shader(
                _nMakeTwoPointConicalGradient(
                    x0,
                    y0,
                    r0,
                    x1,
                    y1,
                    r1,
                    colors,
                    positions,
                    style.tileMode.ordinal,
                    style._getFlags(),
                    style._getMatrixArray()
                )
            )
        }

        fun makeTwoPointConicalGradient(
            p0: Point,
            r0: Float,
            p1: Point,
            r1: Float,
            colors: Array<Color4f>,
            cs: ColorSpace?,
            positions: FloatArray?,
            style: GradientStyle
        ): Shader {
            return makeTwoPointConicalGradient(p0.x, p0.y, r0, p1.x, p1.y, r1, colors, cs, positions, style)
        }

        fun makeTwoPointConicalGradient(
            x0: Float,
            y0: Float,
            r0: Float,
            x1: Float,
            y1: Float,
            r1: Float,
            colors: Array<Color4f>,
            cs: ColorSpace?,
            positions: FloatArray?,
            style: GradientStyle
        ): Shader {
            return try {
                require(positions == null || colors.size == positions.size) { "colors.length " + colors.size + "!= positions.length " + positions!!.size }
                Stats.onNativeCall()
                Shader(
                    _nMakeTwoPointConicalGradientCS(
                        x0,
                        y0,
                        r0,
                        x1,
                        y1,
                        r1,
                        Color4f.flattenArray(colors),
                        getPtr(cs),
                        positions,
                        style.tileMode.ordinal,
                        style._getFlags(),
                        style._getMatrixArray()
                    )
                )
            } finally {
                reachabilityBarrier(cs)
            }
        }

        // Sweep
        fun makeSweepGradient(center: Point, colors: IntArray): Shader {
            return makeSweepGradient(center.x, center.y, colors)
        }

        fun makeSweepGradient(x: Float, y: Float, colors: IntArray): Shader {
            return makeSweepGradient(x, y, 0f, 360f, colors, null, GradientStyle.Companion.DEFAULT)
        }

        fun makeSweepGradient(center: Point, colors: IntArray, positions: FloatArray?): Shader {
            return makeSweepGradient(center.x, center.y, colors, positions)
        }

        fun makeSweepGradient(x: Float, y: Float, colors: IntArray, positions: FloatArray?): Shader {
            return makeSweepGradient(x, y, 0f, 360f, colors, positions, GradientStyle.Companion.DEFAULT)
        }

        fun makeSweepGradient(center: Point, colors: IntArray, positions: FloatArray?, style: GradientStyle): Shader {
            return makeSweepGradient(center.x, center.y, colors, positions, style)
        }

        fun makeSweepGradient(
            x: Float,
            y: Float,
            colors: IntArray,
            positions: FloatArray?,
            style: GradientStyle
        ): Shader {
            return makeSweepGradient(x, y, 0f, 360f, colors, positions, style)
        }

        fun makeSweepGradient(
            center: Point,
            startAngle: Float,
            endAngle: Float,
            colors: IntArray,
            positions: FloatArray?,
            style: GradientStyle
        ): Shader {
            return makeSweepGradient(center.x, center.y, startAngle, endAngle, colors, positions, style)
        }

        fun makeSweepGradient(
            x: Float,
            y: Float,
            startAngle: Float,
            endAngle: Float,
            colors: IntArray,
            positions: FloatArray?,
            style: GradientStyle
        ): Shader {
            require(positions == null || colors.size == positions.size) { "colors.length " + colors.size + "!= positions.length " + positions!!.size }
            Stats.onNativeCall()
            return Shader(
                _nMakeSweepGradient(
                    x,
                    y,
                    startAngle,
                    endAngle,
                    colors,
                    positions,
                    style.tileMode.ordinal,
                    style._getFlags(),
                    style._getMatrixArray()
                )
            )
        }

        fun makeSweepGradient(
            center: Point,
            startAngle: Float,
            endAngle: Float,
            colors: Array<Color4f>,
            cs: ColorSpace?,
            positions: FloatArray?,
            style: GradientStyle
        ): Shader {
            return makeSweepGradient(center.x, center.y, startAngle, endAngle, colors, cs, positions, style)
        }

        fun makeSweepGradient(
            x: Float,
            y: Float,
            startAngle: Float,
            endAngle: Float,
            colors: Array<Color4f>,
            cs: ColorSpace?,
            positions: FloatArray?,
            style: GradientStyle
        ): Shader {
            return try {
                require(positions == null || colors.size == positions.size) { "colors.length " + colors.size + "!= positions.length " + positions!!.size }
                Stats.onNativeCall()
                Shader(
                    _nMakeSweepGradientCS(
                        x,
                        y,
                        startAngle,
                        endAngle,
                        Color4f.flattenArray(colors),
                        getPtr(cs),
                        positions,
                        style.tileMode.ordinal,
                        style._getFlags(),
                        style._getMatrixArray()
                    )
                )
            } finally {
                reachabilityBarrier(cs)
            }
        }

        //
        fun makeEmpty(): Shader {
            Stats.onNativeCall()
            return Shader(_nMakeEmpty())
        }

        fun makeColor(color: Int): Shader {
            Stats.onNativeCall()
            return Shader(_nMakeColor(color))
        }

        fun makeColor(color: Color4f, space: ColorSpace?): Shader {
            return try {
                Stats.onNativeCall()
                Shader(
                    _nMakeColorCS(
                        color.r,
                        color.g,
                        color.b,
                        color.a,
                        getPtr(space)
                    )
                )
            } finally {
                reachabilityBarrier(space)
            }
        }

        fun makeBlend(mode: BlendMode, dst: Shader?, src: Shader?): Shader {
            return try {
                Stats.onNativeCall()
                Shader(
                    _nMakeBlend(
                        mode.ordinal,
                        getPtr(dst),
                        getPtr(src)
                    )
                )
            } finally {
                reachabilityBarrier(dst)
                reachabilityBarrier(src)
            }
        }

        @JvmStatic external fun _nMakeWithColorFilter(ptr: Long, colorFilterPtr: Long): Long
        @JvmStatic external fun _nMakeLinearGradient(
            x0: Float,
            y0: Float,
            x1: Float,
            y1: Float,
            colors: IntArray?,
            positions: FloatArray?,
            tileType: Int,
            flags: Int,
            matrix: FloatArray?
        ): Long

        @JvmStatic external fun _nMakeLinearGradientCS(
            x0: Float,
            y0: Float,
            x1: Float,
            y1: Float,
            colors: FloatArray?,
            colorSpacePtr: Long,
            positions: FloatArray?,
            tileType: Int,
            flags: Int,
            matrix: FloatArray?
        ): Long

        @JvmStatic external fun _nMakeRadialGradient(
            x: Float,
            y: Float,
            r: Float,
            colors: IntArray?,
            positions: FloatArray?,
            tileType: Int,
            flags: Int,
            matrix: FloatArray?
        ): Long

        @JvmStatic external fun _nMakeRadialGradientCS(
            x: Float,
            y: Float,
            r: Float,
            colors: FloatArray?,
            colorSpacePtr: Long,
            positions: FloatArray?,
            tileType: Int,
            flags: Int,
            matrix: FloatArray?
        ): Long

        @JvmStatic external fun _nMakeTwoPointConicalGradient(
            x0: Float,
            y0: Float,
            r0: Float,
            x1: Float,
            y1: Float,
            r1: Float,
            colors: IntArray?,
            positions: FloatArray?,
            tileType: Int,
            flags: Int,
            matrix: FloatArray?
        ): Long

        @JvmStatic external fun _nMakeTwoPointConicalGradientCS(
            x0: Float,
            y0: Float,
            r0: Float,
            x1: Float,
            y1: Float,
            r1: Float,
            colors: FloatArray?,
            colorSpacePtr: Long,
            positions: FloatArray?,
            tileType: Int,
            flags: Int,
            matrix: FloatArray?
        ): Long

        @JvmStatic external fun _nMakeSweepGradient(
            x: Float,
            y: Float,
            startAngle: Float,
            endAngle: Float,
            colors: IntArray?,
            positions: FloatArray?,
            tileType: Int,
            flags: Int,
            matrix: FloatArray?
        ): Long

        @JvmStatic external fun _nMakeSweepGradientCS(
            x: Float,
            y: Float,
            startAngle: Float,
            endAngle: Float,
            colors: FloatArray?,
            colorSpacePtr: Long,
            positions: FloatArray?,
            tileType: Int,
            flags: Int,
            matrix: FloatArray?
        ): Long

        @JvmStatic external fun _nMakeEmpty(): Long
        @JvmStatic external fun _nMakeColor(color: Int): Long
        @JvmStatic external fun _nMakeColorCS(r: Float, g: Float, b: Float, a: Float, colorSpacePtr: Long): Long
        @JvmStatic external fun _nMakeBlend(blendMode: Int, dst: Long, src: Long): Long

        init {
            staticLoad()
        }
    }

    fun makeWithColorFilter(f: ColorFilter?): Shader {
        return try {
            Shader(_nMakeWithColorFilter(_ptr, getPtr(f)))
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(f)
        }
    }
}