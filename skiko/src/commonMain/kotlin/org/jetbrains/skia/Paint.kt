@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import kotlin.jvm.JvmStatic
import kotlin.math.round

class Paint : Managed {
    companion object {
        @JvmStatic
        external fun _nGetFinalizer(): Long
        @JvmStatic
        external fun _nMake(): Long
        @JvmStatic
        external fun _nMakeClone(ptr: Long): Long
        @JvmStatic
        external fun _nEquals(ptr: Long, otherPtr: Long): Boolean
        @JvmStatic
        external fun _nReset(ptr: Long)
        @JvmStatic
        external fun _nIsAntiAlias(ptr: Long): Boolean
        @JvmStatic
        external fun _nSetAntiAlias(ptr: Long, value: Boolean)
        @JvmStatic
        external fun _nIsDither(ptr: Long): Boolean
        @JvmStatic
        external fun _nSetDither(ptr: Long, value: Boolean)
        @JvmStatic
        external fun _nGetMode(ptr: Long): Int
        @JvmStatic
        external fun _nSetMode(ptr: Long, value: Int)
        @JvmStatic
        external fun _nGetColor(ptr: Long): Int
        @JvmStatic
        external fun _nGetColor4f(ptr: Long): Color4f
        @JvmStatic
        external fun _nSetColor(ptr: Long, argb: Int)
        @JvmStatic
        external fun _nSetColor4f(ptr: Long, r: Float, g: Float, b: Float, a: Float, colorSpacePtr: Long)
        @JvmStatic
        external fun _nGetStrokeWidth(ptr: Long): Float
        @JvmStatic
        external fun _nSetStrokeWidth(ptr: Long, value: Float)
        @JvmStatic
        external fun _nGetStrokeMiter(ptr: Long): Float
        @JvmStatic
        external fun _nSetStrokeMiter(ptr: Long, value: Float)
        @JvmStatic
        external fun _nGetStrokeCap(ptr: Long): Int
        @JvmStatic
        external fun _nSetStrokeCap(ptr: Long, value: Int)
        @JvmStatic
        external fun _nGetStrokeJoin(ptr: Long): Int
        @JvmStatic
        external fun _nSetStrokeJoin(ptr: Long, value: Int)
        @JvmStatic
        external fun _nGetFillPath(ptr: Long, path: Long, resScale: Float): Long
        @JvmStatic
        external fun _nGetFillPathCull(
            ptr: Long,
            path: Long,
            left: Float,
            top: Float,
            right: Float,
            bottom: Float,
            resScale: Float
        ): Long

        @JvmStatic
        external fun _nGetShader(ptr: Long): Long
        @JvmStatic
        external fun _nSetShader(ptr: Long, shaderPtr: Long)
        @JvmStatic
        external fun _nGetColorFilter(ptr: Long): Long
        @JvmStatic
        external fun _nSetColorFilter(ptr: Long, colorFilterPtr: Long)
        @JvmStatic
        external fun _nGetBlendMode(ptr: Long): Int
        @JvmStatic
        external fun _nSetBlendMode(ptr: Long, mode: Int)
        @JvmStatic
        external fun _nGetPathEffect(ptr: Long): Long
        @JvmStatic
        external fun _nSetPathEffect(ptr: Long, pathEffectPtr: Long)
        @JvmStatic
        external fun _nGetMaskFilter(ptr: Long): Long
        @JvmStatic
        external fun _nSetMaskFilter(ptr: Long, filterPtr: Long)
        @JvmStatic
        external fun _nGetImageFilter(ptr: Long): Long
        @JvmStatic
        external fun _nSetImageFilter(ptr: Long, filterPtr: Long)
        @JvmStatic
        external fun _nHasNothingToDraw(ptr: Long): Boolean

        init {
            staticLoad()
        }
    }

    internal object _FinalizerHolder {
        val PTR = _nGetFinalizer()
    }

    internal constructor(ptr: Long, managed: Boolean) : super(ptr, _FinalizerHolder.PTR, managed)

    /**
     * Constructs SkPaint with default values.
     *
     * @see [https://fiddle.skia.org/c/@Paint_empty_constructor](https://fiddle.skia.org/c/@Paint_empty_constructor)
     */
    constructor() : super(_nMake(), _FinalizerHolder.PTR) {
        Stats.onNativeCall()
    }

    /**
     *
     * Makes a shallow copy of Paint. PathEffect, Shader,
     * MaskFilter, ColorFilter, and ImageFilter are shared
     * between the original paint and the copy.
     *
     *
     * The referenced objects PathEffect, Shader, MaskFilter, ColorFilter,
     * and ImageFilter cannot be modified after they are created.
     *
     * @return  shallow copy of paint
     *
     * @see [https://fiddle.skia.org/c/@Paint_copy_const_SkPaint](https://fiddle.skia.org/c/@Paint_copy_const_SkPaint)
     */
    fun makeClone(): Paint {
        return try {
            Stats.onNativeCall()
            Paint(_nMakeClone(_ptr), true)
        } finally {
            reachabilityBarrier(this)
        }
    }

    override fun _nativeEquals(other: Native?): Boolean {
        return try {
            _nEquals(_ptr, getPtr(other))
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(other)
        }
    }

    /**
     * Sets all Paint contents to their initial values. This is equivalent to replacing
     * Paint with the result of Paint().
     *
     * @see [https://fiddle.skia.org/c/@Paint_reset](https://fiddle.skia.org/c/@Paint_reset)
     */
    fun reset(): Paint {
        Stats.onNativeCall()
        _nReset(_ptr)
        return this
    }

    /**
     * Returns true if pixels on the active edges of Path may be drawn with partial transparency.
     *
     * @return  antialiasing state
     */
    var isAntiAlias: Boolean
        get() = try {
            Stats.onNativeCall()
            _nIsAntiAlias(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setAntiAlias(value)
        }

    /**
     * Requests, but does not require, that edge pixels draw opaque or with partial transparency.
     *
     * @param value  setting for antialiasing
     */
    fun setAntiAlias(value: Boolean): Paint {
        Stats.onNativeCall()
        _nSetAntiAlias(_ptr, value)
        return this
    }

    /**
     * @return  true if color error may be distributed to smooth color transition.
     */
    val isDither: Boolean
        get() = try {
            Stats.onNativeCall()
            _nIsDither(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * Requests, but does not require, to distribute color error.
     *
     * @param value  setting for ditering
     * @return       this
     */
    fun setDither(value: Boolean): Paint {
        Stats.onNativeCall()
        _nSetDither(_ptr, value)
        return this
    }

    /**
     * @return  whether the geometry is filled, stroked, or filled and stroked.
     */
    var mode: PaintMode
        get() = try {
            Stats.onNativeCall()
            PaintMode.values().get(_nGetMode(_ptr))
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setMode(value)
        }

    /**
     * Sets whether the geometry is filled, stroked, or filled and stroked.
     *
     * @see [https://fiddle.skia.org/c/@Paint_setStyle](https://fiddle.skia.org/c/@Paint_setStyle)
     *
     * @see [https://fiddle.skia.org/c/@Stroke_Width](https://fiddle.skia.org/c/@Stroke_Width)
     */
    fun setMode(style: PaintMode): Paint {
        require(style != null) { "Paint::setMode expected style != null" }
        Stats.onNativeCall()
        _nSetMode(_ptr, style.ordinal)
        return this
    }

    /**
     * Set paint's mode to STROKE if true, or FILL if false.
     *
     * @param value  stroke or fill
     * @return       this
     */
    fun setStroke(value: Boolean): Paint {
        return setMode(if (value) PaintMode.STROKE else PaintMode.FILL)
    }

    /**
     * Retrieves alpha and RGB, unpremultiplied, packed into 32 bits.
     * Use helpers [Color.getA], [Color.getR], [Color.getG], and [Color.getB] to extract
     * a color component.
     *
     * @return  unpremultiplied ARGB
     */
    var color: Int
        get() = try {
            Stats.onNativeCall()
            _nGetColor(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setColor(value)
        }

    /**
     * Retrieves alpha and RGB, unpremultiplied, as four floating point values. RGB are
     * extended sRGB values (sRGB gamut, and encoded with the sRGB transfer function).
     *
     * @return  unpremultiplied RGBA
     */
    var color4f: Color4f
        get() = try {
            Stats.onNativeCall()
            _nGetColor4f(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setColor4f(value)
        }

    /**
     * Sets alpha and RGB used when stroking and filling. The color is a 32-bit value,
     * unpremultiplied, packing 8-bit components for alpha, red, blue, and green.
     *
     * @param color  unpremultiplied ARGB
     *
     * @see [https://fiddle.skia.org/c/@Paint_setColor](https://fiddle.skia.org/c/@Paint_setColor)
     */
    fun setColor(color: Int): Paint {
        Stats.onNativeCall()
        _nSetColor(_ptr, color)
        return this
    }

    /**
     * Sets alpha and RGB used when stroking and filling. The color is four floating
     * point values, unpremultiplied. The color values are interpreted as being in sRGB.
     *
     * @param color       unpremultiplied RGBA
     * @return            this
     */
    fun setColor4f(color: Color4f): Paint {
        return setColor4f(color, null)
    }

    /**
     * Sets alpha and RGB used when stroking and filling. The color is four floating
     * point values, unpremultiplied. The color values are interpreted as being in
     * the colorSpace. If colorSpace is nullptr, then color is assumed to be in the
     * sRGB color space.
     *
     * @param color       unpremultiplied RGBA
     * @param colorSpace  SkColorSpace describing the encoding of color
     * @return            this
     */
    fun setColor4f(color: Color4f, colorSpace: ColorSpace?): Paint {
        return try {
            require(color != null) { "Paint::setColor4f expected color != null" }
            Stats.onNativeCall()
            _nSetColor4f(
                _ptr,
                color.r,
                color.g,
                color.b,
                color.a,
                getPtr(colorSpace)
            )
            this
        } finally {
            reachabilityBarrier(colorSpace)
        }
    }

    /**
     * Retrieves alpha from the color used when stroking and filling.
     *
     * @return  alpha ranging from 0f, fully transparent, to 1f, fully opaque
     */
    val alphaf: Float
        get() = color4f.a

    /**
     * Retrieves alpha from the color used when stroking and filling.
     *
     * @return  alpha ranging from 0, fully transparent, to 255, fully opaque
     */
    val alpha: Int
        get() = round(alphaf * 255f).toInt()

    /**
     *
     * Replaces alpha, leaving RGB unchanged. An out of range value triggers
     * an assert in the debug build. a is a value from 0f to 1f.
     *
     *
     * a set to zero makes color fully transparent; a set to 1.0 makes color
     * fully opaque.
     *
     * @param a  alpha component of color
     * @return   this
     */
    fun setAlphaf(a: Float): Paint {
        return setColor4f(color4f.withA(a))
    }

    /**
     *
     * Replaces alpha, leaving RGB unchanged. An out of range value triggers
     * an assert in the debug build. a is a value from 0 to 255.
     *
     *
     * a set to zero makes color fully transparent; a set to 255 makes color
     * fully opaque.
     *
     * @param a  alpha component of color
     * @return   this
     */
    fun setAlpha(a: Int): Paint {
        return setAlphaf(a / 255f)
    }

    /**
     * Sets color used when drawing solid fills. The color components range from 0 to 255.
     * The color is unpremultiplied; alpha sets the transparency independent of RGB.
     *
     * @param a  amount of alpha, from fully transparent (0) to fully opaque (255)
     * @param r  amount of red, from no red (0) to full red (255)
     * @param g  amount of green, from no green (0) to full green (255)
     * @param b  amount of blue, from no blue (0) to full blue (255)
     *
     * @see [https://fiddle.skia.org/c/@Paint_setARGB](https://fiddle.skia.org/c/@Paint_setARGB)
     */
    fun setARGB(a: Int, r: Int, g: Int, b: Int): Paint {
        Stats.onNativeCall()
        _nSetColor4f(_ptr, r / 255f, g / 255f, b / 255f, a / 255f, 0)
        return this
    }

    /**
     * Returns the thickness of the pen used by Paint to outline the shape.
     *
     * @return  zero for hairline, greater than zero for pen thickness
     */
    var strokeWidth: Float
        get() = try {
            Stats.onNativeCall()
            _nGetStrokeWidth(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setStrokeWidth(value)
        }

    /**
     * Sets the thickness of the pen used by the paint to outline the shape.
     * A stroke-width of zero is treated as "hairline" width. Hairlines are always exactly one
     * pixel wide in device space (their thickness does not change as the canvas is scaled).
     * Negative stroke-widths are invalid; setting a negative width will have no effect.
     *
     * @param width  zero thickness for hairline; greater than zero for pen thickness
     *
     * @see [https://fiddle.skia.org/c/@Miter_Limit](https://fiddle.skia.org/c/@Miter_Limit)
     *
     * @see [https://fiddle.skia.org/c/@Paint_setStrokeWidth](https://fiddle.skia.org/c/@Paint_setStrokeWidth)
     */
    fun setStrokeWidth(width: Float): Paint {
        Stats.onNativeCall()
        _nSetStrokeWidth(_ptr, width)
        return this
    }

    /**
     * Returns the limit at which a sharp corner is drawn beveled.
     *
     * @return  zero and greater miter limit
     */
    var strokeMiter: Float
        get() = try {
            Stats.onNativeCall()
            _nGetStrokeMiter(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setStrokeMiter(value)
        }

    /**
     * Sets the limit at which a sharp corner is drawn beveled.
     * Valid values are zero and greater.
     * Has no effect if miter is less than zero.
     *
     * @param miter  zero and greater miter limit
     * @return       this
     *
     * @see [https://fiddle.skia.org/c/@Paint_setStrokeMiter](https://fiddle.skia.org/c/@Paint_setStrokeMiter)
     */
    fun setStrokeMiter(miter: Float): Paint {
        Stats.onNativeCall()
        _nSetStrokeMiter(_ptr, miter)
        return this
    }

    /**
     * @return  the geometry drawn at the beginning and end of strokes.
     */
    var strokeCap: PaintStrokeCap
        get() = try {
            Stats.onNativeCall()
            PaintStrokeCap.values().get(_nGetStrokeCap(_ptr))
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setStrokeCap(value)
        }

    /**
     * Sets the geometry drawn at the beginning and end of strokes.
     *
     * @return  this
     *
     * @see [https://fiddle.skia.org/c/@Paint_setStrokeCap_a](https://fiddle.skia.org/c/@Paint_setStrokeCap_a)
     *
     * @see [https://fiddle.skia.org/c/@Paint_setStrokeCap_b](https://fiddle.skia.org/c/@Paint_setStrokeCap_b)
     */
    fun setStrokeCap(cap: PaintStrokeCap): Paint {
        require(cap != null) { "Paint::setStrokeCap expected cap != null" }
        Stats.onNativeCall()
        _nSetStrokeCap(_ptr, cap.ordinal)
        return this
    }

    /**
     * @return  the geometry drawn at the corners of strokes.
     */
    var strokeJoin: PaintStrokeJoin
        get() = try {
            Stats.onNativeCall()
            PaintStrokeJoin.values().get(_nGetStrokeJoin(_ptr))
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setStrokeJoin(value)
        }

    /**
     * Sets the geometry drawn at the corners of strokes.
     *
     * @return  this
     *
     * @see [https://fiddle.skia.org/c/@Paint_setStrokeJoin](https://fiddle.skia.org/c/@Paint_setStrokeJoin)
     */
    fun setStrokeJoin(join: PaintStrokeJoin): Paint {
        require(join != null) { "Paint::setStrokeJoin expected join != null" }
        Stats.onNativeCall()
        _nSetStrokeJoin(_ptr, join.ordinal)
        return this
    }

    /**
     * Returns the filled equivalent of the stroked path.
     *
     * @param src       Path read to create a filled version
     * @return          resulting Path
     */
    fun getFillPath(src: Path): Path {
        return getFillPath(src, null, 1f)
    }

    /**
     * Returns the filled equivalent of the stroked path.
     *
     * @param src       Path read to create a filled version
     * @param cull      Optional limit passed to PathEffect
     * @param resScale  if &gt; 1, increase precision, else if (0 &lt; resScale &lt; 1) reduce precision
     * to favor speed and size
     * @return          resulting Path
     */
    fun getFillPath(src: Path, cull: Rect?, resScale: Float): Path {
        return try {
            require(src != null) { "Paint::getFillPath expected src != null" }
            Stats.onNativeCall()
            if (cull == null) org.jetbrains.skia.Path(
                _nGetFillPath(
                    _ptr,
                    getPtr(src),
                    resScale
                )
            ) else org.jetbrains.skia.Path(
                _nGetFillPathCull(
                    _ptr,
                    getPtr(src),
                    cull.left,
                    cull.top,
                    cull.right,
                    cull.bottom,
                    resScale
                )
            )
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(src)
        }
    }

    /**
     * @return  [Shader] or null
     * @see [https://fiddle.skia.org/c/@Paint_refShader](https://fiddle.skia.org/c/@Paint_refShader)
     */
    var shader: Shader?
        get() = try {
            Stats.onNativeCall()
            val shaderPtr = _nGetShader(_ptr)
            if (shaderPtr == 0L) null else Shader(shaderPtr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setShader(value)
        }

    /**
     * @param shader  how geometry is filled with color; if null, color is used instead
     *
     * @see [https://fiddle.skia.org/c/@Color_Filter_Methods](https://fiddle.skia.org/c/@Color_Filter_Methods)
     *
     * @see [https://fiddle.skia.org/c/@Paint_setShader](https://fiddle.skia.org/c/@Paint_setShader)
     */
    fun setShader(shader: Shader?): Paint {
        return try {
            Stats.onNativeCall()
            _nSetShader(_ptr, getPtr(shader))
            this
        } finally {
            reachabilityBarrier(shader)
        }
    }

    /**
     * @return  [ColorFilter] or null
     * @see [https://fiddle.skia.org/c/@Paint_refColorFilter](https://fiddle.skia.org/c/@Paint_refColorFilter)
     */
    var colorFilter: ColorFilter?
        get() = try {
            Stats.onNativeCall()
            val colorFilterPtr = _nGetColorFilter(_ptr)
            if (colorFilterPtr == 0L) null else ColorFilter(colorFilterPtr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setColorFilter(value)
        }

    /**
     * @param colorFilter [ColorFilter] to apply to subsequent draw
     *
     * @see [https://fiddle.skia.org/c/@Blend_Mode_Methods](https://fiddle.skia.org/c/@Blend_Mode_Methods)
     *
     * @see [https://fiddle.skia.org/c/@Paint_setColorFilter](https://fiddle.skia.org/c/@Paint_setColorFilter)
     */
    fun setColorFilter(colorFilter: ColorFilter?): Paint {
        return try {
            Stats.onNativeCall()
            _nSetColorFilter(
                _ptr,
                getPtr(colorFilter)
            )
            this
        } finally {
            reachabilityBarrier(colorFilter)
        }
    }

    /**
     * Returns BlendMode. By default, returns [BlendMode.SRC_OVER].
     *
     * @return  mode used to combine source color with destination color
     */
    var blendMode: BlendMode
        get() = try {
            Stats.onNativeCall()
            BlendMode.values().get(_nGetBlendMode(_ptr))
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setBlendMode(value)
        }

    /**
     * @return  true if BlendMode is BlendMode.SRC_OVER, the default.
     */
    val isSrcOver: Boolean
        get() = blendMode == BlendMode.SRC_OVER

    /**
     * Sets SkBlendMode to mode. Does not check for valid input.
     *
     * @param mode  BlendMode used to combine source color and destination
     * @return      this
     */
    fun setBlendMode(mode: BlendMode): Paint {
        Stats.onNativeCall()
        _nSetBlendMode(_ptr, mode.ordinal)
        return this
    }

    /**
     * @return  [PathEffect] or null
     * @see [https://fiddle.skia.org/c/@Paint_refPathEffect](https://fiddle.skia.org/c/@Paint_refPathEffect)
     */
    var pathEffect: PathEffect?
        get() = try {
            Stats.onNativeCall()
            val pathEffectPtr = _nGetPathEffect(_ptr)
            if (pathEffectPtr == 0L) null else PathEffect(pathEffectPtr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setPathEffect(value)
        }

    /**
     * @param p  replace [Path] with a modification when drawn
     *
     * @see [https://fiddle.skia.org/c/@Mask_Filter_Methods](https://fiddle.skia.org/c/@Mask_Filter_Methods)
     *
     * @see [https://fiddle.skia.org/c/@Paint_setPathEffect](https://fiddle.skia.org/c/@Paint_setPathEffect)
     */
    fun setPathEffect(p: PathEffect?): Paint {
        return try {
            Stats.onNativeCall()
            _nSetPathEffect(_ptr, getPtr(p))
            this
        } finally {
            reachabilityBarrier(p)
        }
    }

    /**
     * @return  [MaskFilter] if previously set, null otherwise
     * @see [https://fiddle.skia.org/c/@Paint_refMaskFilter](https://fiddle.skia.org/c/@Paint_refMaskFilter)
     */
    val maskFilter: MaskFilter?
        get() = try {
            Stats.onNativeCall()
            val maskFilterPtr = _nGetMaskFilter(_ptr)
            if (maskFilterPtr == 0L) null else MaskFilter(maskFilterPtr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * @param maskFilter  modifies clipping mask generated from drawn geometry
     * @return            this
     *
     * @see [https://fiddle.skia.org/c/@Paint_setMaskFilter](https://fiddle.skia.org/c/@Paint_setMaskFilter)
     *
     * @see [https://fiddle.skia.org/c/@Typeface_Methods](https://fiddle.skia.org/c/@Typeface_Methods)
     */
    fun setMaskFilter(maskFilter: MaskFilter?): Paint {
        return try {
            Stats.onNativeCall()
            _nSetMaskFilter(
                _ptr,
                getPtr(maskFilter)
            )
            this
        } finally {
            reachabilityBarrier(maskFilter)
        }
    }

    /**
     * @return  [ImageFilter] or null
     * @see [https://fiddle.skia.org/c/@Paint_refImageFilter](https://fiddle.skia.org/c/@Paint_refImageFilter)
     */
    val imageFilter: ImageFilter?
        get() = try {
            Stats.onNativeCall()
            val imageFilterPtr = _nGetImageFilter(_ptr)
            if (imageFilterPtr == 0L) null else org.jetbrains.skia.ImageFilter(imageFilterPtr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * @param imageFilter  how SkImage is sampled when transformed
     *
     * @see [https://fiddle.skia.org/c/@Draw_Looper_Methods](https://fiddle.skia.org/c/@Draw_Looper_Methods)
     *
     * @see [https://fiddle.skia.org/c/@Paint_setImageFilter](https://fiddle.skia.org/c/@Paint_setImageFilter)
     */
    fun setImageFilter(imageFilter: ImageFilter?): Paint {
        return try {
            Stats.onNativeCall()
            _nSetImageFilter(
                _ptr,
                getPtr(imageFilter)
            )
            this
        } finally {
            reachabilityBarrier(imageFilter)
        }
    }

    /**
     *
     * Returns true if Paint prevents all drawing;
     * otherwise, the Paint may or may not allow drawing.
     *
     *
     * Returns true if, for example, BlendMode combined with alpha computes a
     * new alpha of zero.
     *
     * @return  true if Paint prevents all drawing
     *
     * @see [https://fiddle.skia.org/c/@Paint_nothingToDraw](https://fiddle.skia.org/c/@Paint_nothingToDraw)
     */
    fun hasNothingToDraw(): Boolean {
        return try {
            Stats.onNativeCall()
            _nHasNothingToDraw(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }
}