package org.jetbrains.skia.paragraph

import org.jetbrains.skia.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.impl.withResult
import org.jetbrains.skia.impl.withStringResult

class TextStyle internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        init {
            staticLoad()
        }
    }

    constructor() : this(TextStyle_nMake()) {
        Stats.onNativeCall()
    }

    override fun nativeEquals(other: Native?): Boolean {
        return try {
            Stats.onNativeCall()
            TextStyle_nEquals(
                _ptr,
                getPtr(other)
            )
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(other)
        }
    }

    fun equals(attribute: TextStyleAttribute, other: TextStyle?): Boolean {
        return try {
            Stats.onNativeCall()
            TextStyle_nAttributeEquals(
                _ptr,
                attribute.ordinal,
                getPtr(other)
            )
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(other)
        }
    }

    var color: Int
        get() = try {
            Stats.onNativeCall()
            TextStyle_nGetColor(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setColor(value)
        }

    fun setColor(color: Int): TextStyle {
        try {
            Stats.onNativeCall()
            TextStyle_nSetColor(_ptr, color)
        } finally {
            reachabilityBarrier(this)
        }
        return this
    }

    var foreground: Paint?
        get() = try {
            Stats.onNativeCall()
            val ptr = TextStyle_nGetForeground(_ptr)
            if (ptr == NullPointer) null else Paint(ptr, true)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setForeground(value)
        }

    fun setForeground(paint: Paint?): TextStyle {
        return try {
            Stats.onNativeCall()
            TextStyle_nSetForeground(
                _ptr,
                getPtr(paint)
            )
            this
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(paint)
        }
    }

    var background: Paint?
        get() = try {
            Stats.onNativeCall()
            val ptr = TextStyle_nGetBackground(_ptr)
            if (ptr == NullPointer) null else Paint(ptr, true)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setBackground(value)
        }

    fun setBackground(paint: Paint?): TextStyle {
        return try {
            Stats.onNativeCall()
            TextStyle_nSetBackground(
                _ptr,
                getPtr(paint)
            )
            this
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(paint)
        }
    }

    var decorationStyle: DecorationStyle
        get() = try {
            Stats.onNativeCall()
            DecorationStyle.fromInteropPointer {
                TextStyle_nGetDecorationStyle(_ptr, it)
            }
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setDecorationStyle(value)
        }

    fun setDecorationStyle(d: DecorationStyle): TextStyle {
        try {
            Stats.onNativeCall()
            TextStyle_nSetDecorationStyle(
                _ptr,
                d._underline,
                d._overline,
                d._lineThrough,
                d._gaps,
                d.color,
                d._lineStyle.ordinal,
                d.thicknessMultiplier
            )
        } finally {
            reachabilityBarrier(this)
        }
        return this
    }

    var fontStyle: FontStyle
        get() = try {
            Stats.onNativeCall()
            FontStyle(TextStyle_nGetFontStyle(_ptr))
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setFontStyle(value)
        }

    fun setFontStyle(s: FontStyle): TextStyle {
        try {
            Stats.onNativeCall()
            TextStyle_nSetFontStyle(_ptr, s._value)
        } finally {
            reachabilityBarrier(this)
        }
        return this
    }

    val shadows: Array<Shadow>
        get() = try {
            Stats.onNativeCall()
            Shadow.fromInteropPointer(TextStyle_nGetShadowsCount(_ptr)) {
                TextStyle_nGetShadows(_ptr, it)
            }
        } finally {
            reachabilityBarrier(this)
        }

    fun addShadow(s: Shadow): TextStyle {
        try {
            Stats.onNativeCall()
            TextStyle_nAddShadow(_ptr, s.color, s.offsetX, s.offsetY, s.blurSigma)
        } finally {
            reachabilityBarrier(this)
        }
        return this
    }

    fun addShadows(shadows: Array<Shadow>): TextStyle {
        for (s in shadows) addShadow(s)
        return this
    }

    fun clearShadows(): TextStyle {
        try {
            Stats.onNativeCall()
            TextStyle_nClearShadows(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        return this
    }

    val fontFeatures: Array<FontFeature>
        get() = try {
            Stats.onNativeCall()
            val size = TextStyle_nGetFontFeaturesSize(_ptr)
            withResult(IntArray(size * 2)) {
                Stats.onNativeCall()
                TextStyle_nGetFontFeatures(_ptr, it)
            }.let {
                FontFeature.fromInteropEncodedBy2Ints(it)
            }
        } finally {
            reachabilityBarrier(this)
        }

    fun addFontFeature(f: FontFeature): TextStyle {
        try {
            Stats.onNativeCall()
            interopScope {
                TextStyle_nAddFontFeature(_ptr, toInterop(f.tag), f.value)
            }
        } finally {
            reachabilityBarrier(this)
        }
        return this
    }

    fun addFontFeatures(FontFeatures: Array<FontFeature>): TextStyle {
        for (s in FontFeatures) addFontFeature(s)
        return this
    }

    fun clearFontFeatures(): TextStyle {
        try {
            Stats.onNativeCall()
            TextStyle_nClearFontFeatures(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        return this
    }

    var fontSize: Float
        get() = try {
            Stats.onNativeCall()
            TextStyle_nGetFontSize(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setFontSize(value)
        }

    fun setFontSize(size: Float): TextStyle {
        check(!size.isNaN())
        try {
            Stats.onNativeCall()
            TextStyle_nSetFontSize(_ptr, size)
        } finally {
            reachabilityBarrier(this)
        }
        return this
    }

    var fontFamilies: Array<String>
        get() = try {
            Stats.onNativeCall()
            arrayDecoderScope({ ArrayDecoder(TextStyle_nGetFontFamilies(_ptr), ManagedString_nGetFinalizer()) }) { arrayDecoder ->
                (0 until arrayDecoder.size).map { i -> withStringResult(arrayDecoder.release(i)) }
            }.toTypedArray()
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setFontFamilies(value)
        }

    fun setFontFamily(family: String): TextStyle {
        return setFontFamilies(arrayOf(family))
    }

    fun setFontFamilies(families: Array<String>?): TextStyle {
        Stats.onNativeCall()
        interopScope {
            TextStyle_nSetFontFamilies(_ptr, toInterop(families), families?.size ?: 0)
        }
        return this
    }

    var height: Float?
        get() = try {
            Stats.onNativeCall()
            val height = TextStyle_nGetHeight(_ptr)
            if (height.isNaN()) null else height
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setHeight(value)
        }

    fun setHeight(height: Float?): TextStyle {
        if (height == null) {
            try {
                Stats.onNativeCall()
                TextStyle_nSetHeight(_ptr, false, 0f)
            } finally {
                reachabilityBarrier(this)
            }
        } else {
            check(!height.isNaN())
            try {
                Stats.onNativeCall()
                TextStyle_nSetHeight(_ptr, true, height)
            } finally {
                reachabilityBarrier(this)
            }
        }
        return this
    }

    @Deprecated("Replaced by topRatio")
    var isHalfLeading: Boolean
        get() = try {
            Stats.onNativeCall()
            TextStyle_nGetHalfLeading(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setHalfLeading(value)
        }

    // Same as topRatio = halfLeading ? 0.5f : -1.0f
    @Deprecated("Replaced by topRatio")
    fun setHalfLeading(value: Boolean): TextStyle {
        try {
            Stats.onNativeCall()
            TextStyle_nSetHalfLeading(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }
        return this
    }

    // [0..1]: the ratio of ascent to ascent+descent
    // -1: proportional to the ascent/descent
    var topRatio: Float
        get() = try {
            Stats.onNativeCall()
            TextStyle_nGetTopRatio(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setTopRatio(value)
        }

    fun setTopRatio(topRatio: Float): TextStyle {
        try {
            Stats.onNativeCall()
            TextStyle_nSetTopRatio(_ptr, topRatio)
        } finally {
            reachabilityBarrier(this)
        }
        return this
    }

    var letterSpacing: Float
        get() = try {
            Stats.onNativeCall()
            TextStyle_nGetLetterSpacing(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setLetterSpacing(value)
        }

    fun setLetterSpacing(letterSpacing: Float): TextStyle {
        check(!letterSpacing.isNaN())
        try {
            Stats.onNativeCall()
            TextStyle_nSetLetterSpacing(_ptr, letterSpacing)
        } finally {
            reachabilityBarrier(this)
        }
        return this
    }

    var baselineShift: Float
        get() = try {
            Stats.onNativeCall()
            TextStyle_nGetBaselineShift(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setBaselineShift(value)
        }

    fun setBaselineShift(baselineShift: Float): TextStyle {
        check(!baselineShift.isNaN())
        try {
            Stats.onNativeCall()
            TextStyle_nSetBaselineShift(_ptr, baselineShift)
        } finally {
            reachabilityBarrier(this)
        }
        return this
    }

    var wordSpacing: Float
        get() = try {
            Stats.onNativeCall()
            TextStyle_nGetWordSpacing(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setWordSpacing(value)
        }

    fun setWordSpacing(wordSpacing: Float): TextStyle {
        check(!wordSpacing.isNaN())
        try {
            Stats.onNativeCall()
            TextStyle_nSetWordSpacing(_ptr, wordSpacing)
        } finally {
            reachabilityBarrier(this)
        }
        return this
    }

    var typeface: Typeface?
        get() = try {
            Stats.onNativeCall()
            val ptr = TextStyle_nGetTypeface(_ptr)
            if (ptr == NullPointer) null else Typeface(ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setTypeface(value)
        }

    fun setTypeface(typeface: Typeface?): TextStyle {
        return try {
            Stats.onNativeCall()
            TextStyle_nSetTypeface(
                _ptr,
                getPtr(typeface)
            )
            this
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(typeface)
        }
    }

    var locale: String
        get() = try {
            Stats.onNativeCall()
            withStringResult {
                TextStyle_nGetLocale(_ptr)
            }
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setLocale(value)
        }

    fun setLocale(locale: String?): TextStyle {
        try {
            Stats.onNativeCall()
            interopScope {
                TextStyle_nSetLocale(_ptr, toInterop(locale))
            }
        } finally {
            reachabilityBarrier(this)
        }
        return this
    }

    var baselineMode: BaselineMode
        get() = try {
            Stats.onNativeCall()
            BaselineMode.entries[TextStyle_nGetBaselineMode(_ptr)]
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setBaselineMode(value)
        }

    fun setBaselineMode(baseline: BaselineMode): TextStyle {
        try {
            Stats.onNativeCall()
            TextStyle_nSetBaselineMode(_ptr, baseline.ordinal)
        } finally {
            reachabilityBarrier(this)
        }
        return this
    }


    val fontMetrics: FontMetrics
        get() = try {
            Stats.onNativeCall()
            FontMetrics.fromInteropPointer {
                TextStyle_nGetFontMetrics(_ptr, it)
            }
        } finally {
            reachabilityBarrier(this)
        }

    val isPlaceholder: Boolean
        get() = try {
            Stats.onNativeCall()
            TextStyle_nIsPlaceholder(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    fun setPlaceholder(): TextStyle {
        Stats.onNativeCall()
        TextStyle_nSetPlaceholder(_ptr)
        return this
    }

    internal object _FinalizerHolder {
        val PTR = TextStyle_nGetFinalizer()
    }
}