package org.jetbrains.skija.paragraph

import org.jetbrains.skija.impl.Library.Companion.staticLoad
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.skija.*
import org.jetbrains.skija.impl.Managed
import org.jetbrains.skija.impl.Native
import org.jetbrains.skija.impl.Stats
import java.lang.ref.Reference

class TextStyle @ApiStatus.Internal constructor(ptr: Long) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        @JvmStatic external fun _nGetFinalizer(): Long
        @JvmStatic external fun _nMake(): Long
        @JvmStatic external fun _nEquals(ptr: Long, otherPtr: Long): Boolean
        @JvmStatic external fun _nAttributeEquals(ptr: Long, attribute: Int, otherPtr: Long): Boolean
        @JvmStatic external fun _nGetColor(ptr: Long): Int
        @JvmStatic external fun _nSetColor(ptr: Long, color: Int)
        @JvmStatic external fun _nGetForeground(ptr: Long): Long
        @JvmStatic external fun _nSetForeground(ptr: Long, paintPtr: Long)
        @JvmStatic external fun _nGetBackground(ptr: Long): Long
        @JvmStatic external fun _nSetBackground(ptr: Long, paintPtr: Long)
        @JvmStatic external fun _nGetDecorationStyle(ptr: Long): DecorationStyle
        @JvmStatic external fun _nSetDecorationStyle(
            ptr: Long,
            underline: Boolean,
            overline: Boolean,
            lineThrough: Boolean,
            gaps: Boolean,
            color: Int,
            style: Int,
            thicknessMultiplier: Float
        )

        @JvmStatic external fun _nGetFontStyle(ptr: Long): Int
        @JvmStatic external fun _nSetFontStyle(ptr: Long, fontStyle: Int)
        @JvmStatic external fun _nGetShadows(ptr: Long): Array<Shadow>
        @JvmStatic external fun _nAddShadow(ptr: Long, color: Int, offsetX: Float, offsetY: Float, blurSigma: Double)
        @JvmStatic external fun _nClearShadows(ptr: Long)
        @JvmStatic external fun _nGetFontFeatures(ptr: Long): Array<FontFeature>
        @JvmStatic external fun _nAddFontFeature(ptr: Long, name: String?, value: Int)
        @JvmStatic external fun _nClearFontFeatures(ptr: Long)
        @JvmStatic external fun _nGetFontSize(ptr: Long): Float
        @JvmStatic external fun _nSetFontSize(ptr: Long, size: Float)
        @JvmStatic external fun _nGetFontFamilies(ptr: Long): Array<String>
        @JvmStatic external fun _nSetFontFamilies(ptr: Long, families: Array<String>?)
        @JvmStatic external fun _nGetHeight(ptr: Long): Float?
        @JvmStatic external fun _nSetHeight(ptr: Long, override: Boolean, height: Float)
        @JvmStatic external fun _nGetLetterSpacing(ptr: Long): Float
        @JvmStatic external fun _nSetLetterSpacing(ptr: Long, letterSpacing: Float)
        @JvmStatic external fun _nGetWordSpacing(ptr: Long): Float
        @JvmStatic external fun _nSetWordSpacing(ptr: Long, wordSpacing: Float)
        @JvmStatic external fun _nGetTypeface(ptr: Long): Long
        @JvmStatic external fun _nSetTypeface(ptr: Long, typefacePtr: Long)
        @JvmStatic external fun _nGetLocale(ptr: Long): String
        @JvmStatic external fun _nSetLocale(ptr: Long, locale: String?)
        @JvmStatic external fun _nGetBaselineMode(ptr: Long): Int
        @JvmStatic external fun _nSetBaselineMode(ptr: Long, mode: Int)
        @JvmStatic external fun _nGetFontMetrics(ptr: Long): FontMetrics
        @JvmStatic external fun _nIsPlaceholder(ptr: Long): Boolean
        @JvmStatic external fun _nSetPlaceholder(ptr: Long)

        init {
            staticLoad()
        }
    }

    constructor() : this(_nMake()) {
        Stats.onNativeCall()
    }

    @ApiStatus.Internal
    override fun _nativeEquals(other: Native?): Boolean {
        return try {
            Stats.onNativeCall()
            _nEquals(
                _ptr,
                Native.Companion.getPtr(other)
            )
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(other)
        }
    }

    fun equals(attribute: TextStyleAttribute, other: TextStyle?): Boolean {
        return try {
            Stats.onNativeCall()
            _nAttributeEquals(
                _ptr,
                attribute.ordinal,
                Native.getPtr(other)
            )
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(other)
        }
    }

    val color: Int
        get() = try {
            Stats.onNativeCall()
            _nGetColor(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setColor(color: Int): TextStyle {
        Stats.onNativeCall()
        _nSetColor(_ptr, color)
        return this
    }

    val foreground: Paint?
        get() = try {
            Stats.onNativeCall()
            val ptr = _nGetForeground(_ptr)
            if (ptr == 0L) null else org.jetbrains.skija.Paint(ptr, true)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setForeground(paint: Paint?): TextStyle {
        return try {
            Stats.onNativeCall()
            _nSetForeground(
                _ptr,
                Native.Companion.getPtr(paint)
            )
            this
        } finally {
            Reference.reachabilityFence(paint)
        }
    }

    val background: Paint?
        get() = try {
            Stats.onNativeCall()
            val ptr = _nGetBackground(_ptr)
            if (ptr == 0L) null else org.jetbrains.skija.Paint(ptr, true)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setBackground(paint: Paint?): TextStyle {
        return try {
            Stats.onNativeCall()
            _nSetBackground(
                _ptr,
                Native.Companion.getPtr(paint)
            )
            this
        } finally {
            Reference.reachabilityFence(paint)
        }
    }

    val decorationStyle: org.jetbrains.skija.paragraph.DecorationStyle
        get() = try {
            Stats.onNativeCall()
            _nGetDecorationStyle(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setDecorationStyle(d: DecorationStyle): TextStyle {
        Stats.onNativeCall()
        _nSetDecorationStyle(
            _ptr,
            d._underline,
            d._overline,
            d._lineThrough,
            d._gaps,
            d.color,
            d._lineStyle.ordinal,
            d.thicknessMultiplier
        )
        return this
    }

    val fontStyle: FontStyle
        get() = try {
            Stats.onNativeCall()
            org.jetbrains.skija.FontStyle(_nGetFontStyle(_ptr))
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setFontStyle(s: FontStyle): TextStyle {
        Stats.onNativeCall()
        _nSetFontStyle(_ptr, s._value)
        return this
    }

    val shadows: Array<org.jetbrains.skija.paragraph.Shadow>
        get() = try {
            Stats.onNativeCall()
            _nGetShadows(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun addShadow(s: Shadow): TextStyle {
        Stats.onNativeCall()
        _nAddShadow(_ptr, s.color, s.offsetX, s.offsetY, s.blurSigma)
        return this
    }

    fun addShadows(shadows: Array<Shadow>): TextStyle {
        for (s in shadows) addShadow(s)
        return this
    }

    fun clearShadows(): TextStyle {
        Stats.onNativeCall()
        _nClearShadows(_ptr)
        return this
    }

    val fontFeatures: Array<FontFeature>
        get() = try {
            Stats.onNativeCall()
            _nGetFontFeatures(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun addFontFeature(f: FontFeature): TextStyle {
        Stats.onNativeCall()
        _nAddFontFeature(_ptr, f.tag, f.value)
        return this
    }

    fun addFontFeatures(FontFeatures: Array<FontFeature>): TextStyle {
        for (s in FontFeatures) addFontFeature(s)
        return this
    }

    fun clearFontFeatures(): TextStyle {
        Stats.onNativeCall()
        _nClearFontFeatures(_ptr)
        return this
    }

    val fontSize: Float
        get() = try {
            Stats.onNativeCall()
            _nGetFontSize(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setFontSize(size: Float): TextStyle {
        Stats.onNativeCall()
        _nSetFontSize(_ptr, size)
        return this
    }

    val fontFamilies: Array<String>
        get() = try {
            Stats.onNativeCall()
            _nGetFontFamilies(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setFontFamily(family: String): TextStyle {
        return setFontFamilies(arrayOf(family))
    }

    fun setFontFamilies(families: Array<String>?): TextStyle {
        Stats.onNativeCall()
        _nSetFontFamilies(_ptr, families)
        return this
    }

    val height: Float?
        get() = try {
            Stats.onNativeCall()
            _nGetHeight(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setHeight(height: Float?): TextStyle {
        Stats.onNativeCall()
        if (height == null) _nSetHeight(_ptr, false, 0f) else _nSetHeight(_ptr, true, height)
        return this
    }

    val letterSpacing: Float
        get() = try {
            Stats.onNativeCall()
            _nGetLetterSpacing(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setLetterSpacing(letterSpacing: Float): TextStyle {
        Stats.onNativeCall()
        _nSetLetterSpacing(_ptr, letterSpacing)
        return this
    }

    val wordSpacing: Float
        get() = try {
            Stats.onNativeCall()
            _nGetWordSpacing(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setWordSpacing(wordSpacing: Float): TextStyle {
        Stats.onNativeCall()
        _nSetWordSpacing(_ptr, wordSpacing)
        return this
    }

    val typeface: Typeface?
        get() = try {
            Stats.onNativeCall()
            val ptr = _nGetTypeface(_ptr)
            if (ptr == 0L) null else Typeface(ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setTypeface(typeface: Typeface?): TextStyle {
        return try {
            Stats.onNativeCall()
            _nSetTypeface(
                _ptr,
                Native.Companion.getPtr(typeface)
            )
            this
        } finally {
            Reference.reachabilityFence(typeface)
        }
    }

    val locale: String
        get() = try {
            Stats.onNativeCall()
            _nGetLocale(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setLocale(locale: String?): TextStyle {
        Stats.onNativeCall()
        _nSetLocale(_ptr, locale)
        return this
    }

    val baselineMode: org.jetbrains.skija.paragraph.BaselineMode
        get() = try {
            Stats.onNativeCall()
            BaselineMode._values.get(_nGetBaselineMode(_ptr))
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setBaselineMode(baseline: BaselineMode): TextStyle {
        Stats.onNativeCall()
        _nSetBaselineMode(_ptr, baseline.ordinal)
        return this
    }

    val fontMetrics: FontMetrics
        get() = try {
            Stats.onNativeCall()
            _nGetFontMetrics(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    val isPlaceholder: Boolean
        get() = try {
            Stats.onNativeCall()
            _nIsPlaceholder(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setPlaceholder(): TextStyle {
        Stats.onNativeCall()
        _nSetPlaceholder(_ptr)
        return this
    }

    @ApiStatus.Internal
    object _FinalizerHolder {
        val PTR = _nGetFinalizer()
    }
}