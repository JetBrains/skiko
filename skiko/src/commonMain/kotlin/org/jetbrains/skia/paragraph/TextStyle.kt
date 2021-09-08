@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia.paragraph

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.*
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.getPtr
import kotlin.jvm.JvmStatic

class TextStyle internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_TextStyle__1nGetFinalizer")
        external fun _nGetFinalizer(): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_TextStyle__1nMake")
        external fun _nMake(): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_TextStyle__1nEquals")
        external fun _nEquals(ptr: NativePointer, otherPtr: NativePointer): Boolean
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_TextStyle__1nAttributeEquals")
        external fun _nAttributeEquals(ptr: NativePointer, attribute: Int, otherPtr: NativePointer): Boolean
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_TextStyle__1nGetColor")
        external fun _nGetColor(ptr: NativePointer): Int
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_TextStyle__1nSetColor")
        external fun _nSetColor(ptr: NativePointer, color: Int)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_TextStyle__1nGetForeground")
        external fun _nGetForeground(ptr: NativePointer): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_TextStyle__1nSetForeground")
        external fun _nSetForeground(ptr: NativePointer, paintPtr: NativePointer)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_TextStyle__1nGetBackground")
        external fun _nGetBackground(ptr: NativePointer): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_TextStyle__1nSetBackground")
        external fun _nSetBackground(ptr: NativePointer, paintPtr: NativePointer)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_TextStyle__1nGetDecorationStyle")
        external fun _nGetDecorationStyle(ptr: NativePointer): DecorationStyle
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_TextStyle__1nSetDecorationStyle")
        external fun _nSetDecorationStyle(
            ptr: NativePointer,
            underline: Boolean,
            overline: Boolean,
            lineThrough: Boolean,
            gaps: Boolean,
            color: Int,
            style: Int,
            thicknessMultiplier: Float
        )

        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_TextStyle__1nGetFontStyle")
        external fun _nGetFontStyle(ptr: NativePointer): Int
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_TextStyle__1nSetFontStyle")
        external fun _nSetFontStyle(ptr: NativePointer, fontStyle: Int)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_TextStyle__1nGetShadows")
        external fun _nGetShadows(ptr: NativePointer): Array<Shadow>
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_TextStyle__1nAddShadow")
        external fun _nAddShadow(ptr: NativePointer, color: Int, offsetX: Float, offsetY: Float, blurSigma: Double)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_TextStyle__1nClearShadows")
        external fun _nClearShadows(ptr: NativePointer)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_TextStyle__1nGetFontFeatures")
        external fun _nGetFontFeatures(ptr: NativePointer): Array<FontFeature>
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_TextStyle__1nAddFontFeature")
        external fun _nAddFontFeature(ptr: NativePointer, name: String?, value: Int)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_TextStyle__1nClearFontFeatures")
        external fun _nClearFontFeatures(ptr: NativePointer)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_TextStyle__1nGetFontSize")
        external fun _nGetFontSize(ptr: NativePointer): Float
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_TextStyle__1nSetFontSize")
        external fun _nSetFontSize(ptr: NativePointer, size: Float)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_TextStyle__1nGetFontFamilies")
        external fun _nGetFontFamilies(ptr: NativePointer): Array<String>
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_TextStyle__1nSetFontFamilies")
        external fun _nSetFontFamilies(ptr: NativePointer, families: Array<String>?)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_TextStyle__1nGetHeight")
        external fun _nGetHeight(ptr: NativePointer): Float?
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_TextStyle__1nSetHeight")
        external fun _nSetHeight(ptr: NativePointer, override: Boolean, height: Float)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_TextStyle__1nGetLetterSpacing")
        external fun _nGetLetterSpacing(ptr: NativePointer): Float
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_TextStyle__1nSetLetterSpacing")
        external fun _nSetLetterSpacing(ptr: NativePointer, letterSpacing: Float)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_TextStyle__1nGetWordSpacing")
        external fun _nGetWordSpacing(ptr: NativePointer): Float
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_TextStyle__1nSetWordSpacing")
        external fun _nSetWordSpacing(ptr: NativePointer, wordSpacing: Float)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_TextStyle__1nGetTypeface")
        external fun _nGetTypeface(ptr: NativePointer): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_TextStyle__1nSetTypeface")
        external fun _nSetTypeface(ptr: NativePointer, typefacePtr: NativePointer)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_TextStyle__1nGetLocale")
        external fun _nGetLocale(ptr: NativePointer): String
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_TextStyle__1nSetLocale")
        external fun _nSetLocale(ptr: NativePointer, locale: String?)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_TextStyle__1nGetBaselineMode")
        external fun _nGetBaselineMode(ptr: NativePointer): Int
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_TextStyle__1nSetBaselineMode")
        external fun _nSetBaselineMode(ptr: NativePointer, mode: Int)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_TextStyle__1nGetFontMetrics")
        external fun _nGetFontMetrics(ptr: NativePointer): FontMetrics
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_TextStyle__1nIsPlaceholder")
        external fun _nIsPlaceholder(ptr: NativePointer): Boolean
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_TextStyle__1nSetPlaceholder")
        external fun _nSetPlaceholder(ptr: NativePointer)

        init {
            staticLoad()
        }
    }

    constructor() : this(_nMake()) {
        Stats.onNativeCall()
    }

    override fun _nativeEquals(other: Native?): Boolean {
        return try {
            Stats.onNativeCall()
            _nEquals(
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
            _nAttributeEquals(
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
            _nGetColor(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setColor(value)
        }
    
    fun setColor(color: Int): TextStyle {
        Stats.onNativeCall()
        _nSetColor(_ptr, color)
        return this
    }

    var foreground: Paint?
        get() = try {
            Stats.onNativeCall()
            val ptr = _nGetForeground(_ptr)
            if (ptr == NULLPNTR) null else Paint(ptr, true)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setForeground(value)
        }
    
    fun setForeground(paint: Paint?): TextStyle {
        return try {
            Stats.onNativeCall()
            _nSetForeground(
                _ptr,
                getPtr(paint)
            )
            this
        } finally {
            reachabilityBarrier(paint)
        }
    }

    var background: Paint?
        get() = try {
            Stats.onNativeCall()
            val ptr = _nGetBackground(_ptr)
            if (ptr == NULLPNTR) null else Paint(ptr, true)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setBackground(value)
        }

    fun setBackground(paint: Paint?): TextStyle {
        return try {
            Stats.onNativeCall()
            _nSetBackground(
                _ptr,
                getPtr(paint)
            )
            this
        } finally {
            reachabilityBarrier(paint)
        }
    }

    var decorationStyle: org.jetbrains.skia.paragraph.DecorationStyle
        get() = try {
            Stats.onNativeCall()
            _nGetDecorationStyle(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setDecorationStyle(value)
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

    var fontStyle: FontStyle
        get() = try {
            Stats.onNativeCall()
            FontStyle(_nGetFontStyle(_ptr))
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setFontStyle(value)
        }
    
    fun setFontStyle(s: FontStyle): TextStyle {
        Stats.onNativeCall()
        _nSetFontStyle(_ptr, s._value)
        return this
    }

    val shadows: Array<org.jetbrains.skia.paragraph.Shadow>
        get() = try {
            Stats.onNativeCall()
            _nGetShadows(_ptr)
        } finally {
            reachabilityBarrier(this)
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
            reachabilityBarrier(this)
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

    var fontSize: Float
        get() = try {
            Stats.onNativeCall()
            _nGetFontSize(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setFontSize(value)
        }
    
    fun setFontSize(size: Float): TextStyle {
        Stats.onNativeCall()
        _nSetFontSize(_ptr, size)
        return this
    }

    var fontFamilies: Array<String>
        get() = try {
            Stats.onNativeCall()
            _nGetFontFamilies(_ptr)
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
        _nSetFontFamilies(_ptr, families)
        return this
    }

    var height: Float?
        get() = try {
            Stats.onNativeCall()
            _nGetHeight(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setHeight(value)
        }
    
    fun setHeight(height: Float?): TextStyle {
        Stats.onNativeCall()
        if (height == null) _nSetHeight(_ptr, false, 0f) else _nSetHeight(_ptr, true, height)
        return this
    }

    var letterSpacing: Float
        get() = try {
            Stats.onNativeCall()
            _nGetLetterSpacing(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setLetterSpacing(value)
        }

    fun setLetterSpacing(letterSpacing: Float): TextStyle {
        Stats.onNativeCall()
        _nSetLetterSpacing(_ptr, letterSpacing)
        return this
    }

    var wordSpacing: Float
        get() = try {
            Stats.onNativeCall()
            _nGetWordSpacing(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setWordSpacing(value)
        }
    
    fun setWordSpacing(wordSpacing: Float): TextStyle {
        Stats.onNativeCall()
        _nSetWordSpacing(_ptr, wordSpacing)
        return this
    }

    var typeface: Typeface?
        get() = try {
            Stats.onNativeCall()
            val ptr = _nGetTypeface(_ptr)
            if (ptr == NULLPNTR) null else Typeface(ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setTypeface(value)
        }
    
    fun setTypeface(typeface: Typeface?): TextStyle {
        return try {
            Stats.onNativeCall()
            _nSetTypeface(
                _ptr,
                getPtr(typeface)
            )
            this
        } finally {
            reachabilityBarrier(typeface)
        }
    }

    var locale: String
        get() = try {
            Stats.onNativeCall()
            _nGetLocale(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setLocale(value)
        }

    fun setLocale(locale: String?): TextStyle {
        Stats.onNativeCall()
        _nSetLocale(_ptr, locale)
        return this
    }

    var baselineMode: BaselineMode
        get() = try {
            Stats.onNativeCall()
            BaselineMode.values().get(_nGetBaselineMode(_ptr))
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setBaselineMode(value)
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
            reachabilityBarrier(this)
        }

    val isPlaceholder: Boolean
        get() = try {
            Stats.onNativeCall()
            _nIsPlaceholder(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    fun setPlaceholder(): TextStyle {
        Stats.onNativeCall()
        _nSetPlaceholder(_ptr)
        return this
    }

    internal object _FinalizerHolder {
        val PTR = _nGetFinalizer()
    }
}