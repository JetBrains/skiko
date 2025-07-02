package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.impl.withResult
import kotlin.math.round

class Paint : Managed {
    companion object {
        init {
            staticLoad()
        }
    }

    internal object _FinalizerHolder {
        val PTR = Paint_nGetFinalizer()
    }

    internal constructor(ptr: NativePointer, managed: Boolean) : super(ptr, _FinalizerHolder.PTR, managed)

    /**
     * Constructs SkPaint with default values.
     *
     * @see [https://fiddle.skia.org/c/@Paint_empty_constructor](https://fiddle.skia.org/c/@Paint_empty_constructor)
     */
    constructor() : super(Paint_nMake(), _FinalizerHolder.PTR) {
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
            Paint(Paint_nMakeClone(_ptr), true)
        } finally {
            reachabilityBarrier(this)
        }
    }

    override fun nativeEquals(other: Native?): Boolean {
        return try {
            Paint_nEquals(_ptr, getPtr(other))
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
        Paint_nReset(_ptr)
        return this
    }

    /**
     * Requests, but does not require, that edge pixels draw opaque or with partial transparency.
     *
     * Returns true if pixels on the active edges of Path may be drawn with partial transparency.
     *
     * @return  antialiasing state
     */
    var isAntiAlias: Boolean
        get() = try {
            Stats.onNativeCall()
            Paint_nIsAntiAlias(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            Paint_nSetAntiAlias(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * Requests, but does not require, to distribute color error.
     *
     * @return  true if color error may be distributed to smooth color transition.
     */
    var isDither: Boolean
        get() = try {
            Stats.onNativeCall()
            Paint_nIsDither(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value)  = try {
            Stats.onNativeCall()
            Paint_nSetDither(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * Sets whether the geometry is filled, stroked, or filled and stroked.
     *
     * @see [https://fiddle.skia.org/c/@Paint_setStyle](https://fiddle.skia.org/c/@Paint_setStyle)
     *
     * @see [https://fiddle.skia.org/c/@Stroke_Width](https://fiddle.skia.org/c/@Stroke_Width)
     *
     * @return  whether the geometry is filled, stroked, or filled and stroked.
     */
    var mode: PaintMode
        get() = try {
            Stats.onNativeCall()
            PaintMode.values().get(Paint_nGetMode(_ptr))
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            Paint_nSetMode(_ptr, value.ordinal)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * Set paint's mode to STROKE if true, or FILL if false.
     *
     * @param value  stroke or fill
     * @return       this
     */
    fun setStroke(value: Boolean): Paint {
        mode = (if (value) PaintMode.STROKE else PaintMode.FILL)
        return this
    }

    /**
     * Sets alpha and RGB used when stroking and filling. The color is a 32-bit value,
     * unpremultiplied, packing 8-bit components for alpha, red, blue, and green.
     *
     * @see [https://fiddle.skia.org/c/@Paint_setColor](https://fiddle.skia.org/c/@Paint_setColor)
     *
     * Retrieves alpha and RGB, unpremultiplied, packed into 32 bits.
     * Use helpers [Color.getA], [Color.getR], [Color.getG], and [Color.getB] to extract
     * a color component.
     *
     * @return  unpremultiplied ARGB
     */
    var color: Int
        get() = try {
            Stats.onNativeCall()
            Paint_nGetColor(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            Paint_nSetColor(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * Sets alpha and RGB used when stroking and filling. The color is four floating
     * point values, unpremultiplied. The color values are interpreted as being in sRGB.
     *
     * Retrieves alpha and RGB, unpremultiplied, as four floating point values. RGB are
     * extended sRGB values (sRGB gamut, and encoded with the sRGB transfer function).
     *
     * @return  unpremultiplied RGBA
     */
    var color4f: Color4f
        get() = try {
            Stats.onNativeCall()
            Color4f(withResult(FloatArray(4)) {
                Paint_nGetColor4f(_ptr, it)
            })
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setColor4f(value, null)
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
            Stats.onNativeCall()
            Paint_nSetColor4f(
                _ptr,
                color.r,
                color.g,
                color.b,
                color.a,
                getPtr(colorSpace)
            )
            this
        } finally {
            reachabilityBarrier(this)
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
    var alpha: Int
        get() = round(alphaf * 255f).toInt()
        set(value) { setAlphaf(value / 255f) }

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
        setColor4f(color4f.withA(a), null)
        return this
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
        Paint_nSetColor4f(_ptr, r / 255f, g / 255f, b / 255f, a / 255f, NullPointer)
        return this
    }

    /**
     * Sets the thickness of the pen used by the paint to outline the shape.
     * A stroke-width of zero is treated as "hairline" width. Hairlines are always exactly one
     * pixel wide in device space (their thickness does not change as the canvas is scaled).
     * Negative stroke-widths are invalid; setting a negative width will have no effect.
     *
     * @see [https://fiddle.skia.org/c/@Miter_Limit](https://fiddle.skia.org/c/@Miter_Limit)
     *
     * @see [https://fiddle.skia.org/c/@Paint_setStrokeWidth](https://fiddle.skia.org/c/@Paint_setStrokeWidth)
     *
     * Returns the thickness of the pen used by Paint to outline the shape.
     *
     * @return  zero for hairline, greater than zero for pen thickness
     */
    var strokeWidth: Float
        get() = try {
            Stats.onNativeCall()
            Paint_nGetStrokeWidth(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            Paint_nSetStrokeWidth(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }


    /**
     * Sets the limit at which a sharp corner is drawn beveled.
     * Valid values are zero and greater.
     * Has no effect if miter is less than zero.
     *
     * @see [https://fiddle.skia.org/c/@Paint_setStrokeMiter](https://fiddle.skia.org/c/@Paint_setStrokeMiter)
     *
     * Returns the limit at which a sharp corner is drawn beveled.
     *
     * @return  zero and greater miter limit
     */
    var strokeMiter: Float
        get() = try {
            Stats.onNativeCall()
            Paint_nGetStrokeMiter(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            Paint_nSetStrokeMiter(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }


    /**
     * Sets the geometry drawn at the beginning and end of strokes.
     *
     * @see [https://fiddle.skia.org/c/@Paint_setStrokeCap_a](https://fiddle.skia.org/c/@Paint_setStrokeCap_a)
     *
     * @see [https://fiddle.skia.org/c/@Paint_setStrokeCap_b](https://fiddle.skia.org/c/@Paint_setStrokeCap_b)
     *
     * @return  the geometry drawn at the beginning and end of strokes.
     */
    var strokeCap: PaintStrokeCap
        get() = try {
            Stats.onNativeCall()
            PaintStrokeCap.values().get(Paint_nGetStrokeCap(_ptr))
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            Paint_nSetStrokeCap(_ptr, value.ordinal)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * Sets the geometry drawn at the corners of strokes.
     *
     * @see [https://fiddle.skia.org/c/@Paint_setStrokeJoin](https://fiddle.skia.org/c/@Paint_setStrokeJoin)
     *
     * @return  the geometry drawn at the corners of strokes.
     */
    var strokeJoin: PaintStrokeJoin
        get() = try {
            Stats.onNativeCall()
            PaintStrokeJoin.values().get(Paint_nGetStrokeJoin(_ptr))
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            Paint_nSetStrokeJoin(_ptr, value.ordinal)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * @param shader  how geometry is filled with color; if null, color is used instead
     *
     * @see [https://fiddle.skia.org/c/@Color_Filter_Methods](https://fiddle.skia.org/c/@Color_Filter_Methods)
     *
     * @see [https://fiddle.skia.org/c/@Paint_setShader](https://fiddle.skia.org/c/@Paint_setShader)
     *
     * @return  [Shader] or null
     * @see [https://fiddle.skia.org/c/@Paint_refShader](https://fiddle.skia.org/c/@Paint_refShader)
     */
    var shader: Shader?
        get() = try {
            Stats.onNativeCall()
            val shaderPtr = Paint_nGetShader(_ptr)
            if (shaderPtr == NullPointer) null else Shader(shaderPtr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            Paint_nSetShader(_ptr, getPtr(value))
        } finally {
            reachabilityBarrier(value)
            reachabilityBarrier(this)
        }

    /**
     * @param colorFilter [ColorFilter] to apply to subsequent draw
     *
     * @see [https://fiddle.skia.org/c/@Blend_Mode_Methods](https://fiddle.skia.org/c/@Blend_Mode_Methods)
     *
     * @see [https://fiddle.skia.org/c/@Paint_setColorFilter](https://fiddle.skia.org/c/@Paint_setColorFilter)
     *
     * @return  [ColorFilter] or null
     * @see [https://fiddle.skia.org/c/@Paint_refColorFilter](https://fiddle.skia.org/c/@Paint_refColorFilter)
     */
    var colorFilter: ColorFilter?
        get() = try {
            Stats.onNativeCall()
            val colorFilterPtr = Paint_nGetColorFilter(_ptr)
            if (colorFilterPtr == NullPointer) null else ColorFilter(colorFilterPtr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            Paint_nSetColorFilter(
                _ptr,
                getPtr(value)
            )
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(value)
        }

    /**
     * Sets SkBlendMode to mode. Does not check for valid input.
     * BlendMode used to combine source color and destination.
     *
     * Returns BlendMode. By default, returns [BlendMode.SRC_OVER].
     *
     * @return  mode used to combine source color with destination color
     */
    var blendMode: BlendMode
        get() = try {
            Stats.onNativeCall()
            BlendMode.values().get(Paint_nGetBlendMode(_ptr))
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            Paint_nSetBlendMode(_ptr, value.ordinal)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * @return  true if BlendMode is BlendMode.SRC_OVER, the default.
     */
    val isSrcOver: Boolean
        get() = blendMode == BlendMode.SRC_OVER

    /**
     * Replace [Path] with a modification when drawn
     *
     * @see [https://fiddle.skia.org/c/@Mask_Filter_Methods](https://fiddle.skia.org/c/@Mask_Filter_Methods)
     *
     * @see [https://fiddle.skia.org/c/@Paint_setPathEffect](https://fiddle.skia.org/c/@Paint_setPathEffect)
     *
     * @return  [PathEffect] or null
     * @see [https://fiddle.skia.org/c/@Paint_refPathEffect](https://fiddle.skia.org/c/@Paint_refPathEffect)
     */
    var pathEffect: PathEffect?
        get() = try {
            Stats.onNativeCall()
            val pathEffectPtr = Paint_nGetPathEffect(_ptr)
            if (pathEffectPtr == NullPointer) null else PathEffect(pathEffectPtr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            Paint_nSetPathEffect(_ptr, getPtr(value))
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(value)
        }

    /**
     * maskFilter  modifies clipping mask generated from drawn geometry
     *
     * @see [https://fiddle.skia.org/c/@Paint_setMaskFilter](https://fiddle.skia.org/c/@Paint_setMaskFilter)
     *
     * @see [https://fiddle.skia.org/c/@Typeface_Methods](https://fiddle.skia.org/c/@Typeface_Methods)
     *
     * @return  [MaskFilter] if previously set, null otherwise
     * @see [https://fiddle.skia.org/c/@Paint_refMaskFilter](https://fiddle.skia.org/c/@Paint_refMaskFilter)
     */
    var maskFilter: MaskFilter?
        get() = try {
            Stats.onNativeCall()
            val maskFilterPtr = Paint_nGetMaskFilter(_ptr)
            if (maskFilterPtr == NullPointer) null else MaskFilter(maskFilterPtr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            Paint_nSetMaskFilter(
                _ptr,
                getPtr(value)
            )
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(value)
        }

    /**
     * imageFilter  how SkImage is sampled when transformed
     *
     * @see [https://fiddle.skia.org/c/@Draw_Looper_Methods](https://fiddle.skia.org/c/@Draw_Looper_Methods)
     *
     * @see [https://fiddle.skia.org/c/@Paint_setImageFilter](https://fiddle.skia.org/c/@Paint_setImageFilter)
     *
     * @return  [ImageFilter] or null
     * @see [https://fiddle.skia.org/c/@Paint_refImageFilter](https://fiddle.skia.org/c/@Paint_refImageFilter)
     */
    var imageFilter: ImageFilter?
        get() = try {
            Stats.onNativeCall()
            val imageFilterPtr = Paint_nGetImageFilter(_ptr)
            if (imageFilterPtr == NullPointer) null else org.jetbrains.skia.ImageFilter(imageFilterPtr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            Paint_nSetImageFilter(
                _ptr,
                getPtr(value)
            )
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(value)
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
            Paint_nHasNothingToDraw(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }
}