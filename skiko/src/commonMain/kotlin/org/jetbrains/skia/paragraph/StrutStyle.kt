package org.jetbrains.skia.paragraph

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.*
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skia.impl.withResult
import org.jetbrains.skia.impl.withStringResult

class StrutStyle internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        init {
            staticLoad()
        }
    }

    constructor() : this(StrutStyle_nMake()) {
        Stats.onNativeCall()
    }

    override fun nativeEquals(other: Native?): Boolean {
        return try {
            Stats.onNativeCall()
            StrutStyle_nEquals(_ptr, getPtr(other))
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(other)
        }
    }

    val fontFamilies: Array<String>
        get() = try {
            Stats.onNativeCall()
            arrayDecoderScope({ArrayDecoder(_nGetFontFamilies(_ptr), ManagedString_nGetFinalizer())}) { arrayDecoder ->
                (0 until arrayDecoder.size).map {  i -> withStringResult(arrayDecoder.release(i)) }.toTypedArray()
            }
        } finally {
            reachabilityBarrier(this)
        }

    fun setFontFamilies(families: Array<String>): StrutStyle {
        try {
            Stats.onNativeCall()
            interopScope {
                StrutStyle_nSetFontFamilies(_ptr, toInterop(families), families.size)
            }
        } finally {
            reachabilityBarrier(this)
        }
        return this
    }

    var fontStyle: FontStyle
        get() = try {
            Stats.onNativeCall()
            val fontStyleData = withResult(IntArray(3)) {
                _nGetFontStyle(_ptr, it)
            }
            FontStyle(fontStyleData[0], fontStyleData[1], FontSlant.entries[fontStyleData[2]])
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setFontStyle(value)
        }
    
    fun setFontStyle(style: FontStyle): StrutStyle {
        try {
            Stats.onNativeCall()
            _nSetFontStyle(_ptr, style._value)
        } finally {
            reachabilityBarrier(this)
        }
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

    fun setFontSize(value: Float): StrutStyle {
        check(!value.isNaN())
        try {
            Stats.onNativeCall()
            _nSetFontSize(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }
        return this
    }

    var height: Float
        get() = try {
            Stats.onNativeCall()
            StrutStyle_nGetHeight(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setHeight(value)
        }
    
    fun setHeight(value: Float): StrutStyle {
        check(!value.isNaN())
        try {
            Stats.onNativeCall()
            StrutStyle_nSetHeight(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }
        return this
    }

    var leading: Float
        get() = try {
            Stats.onNativeCall()
            _nGetLeading(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setLeading(value)
        }
    
    fun setLeading(value: Float): StrutStyle {
        check(!value.isNaN())
        try {
            Stats.onNativeCall()
            _nSetLeading(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }
        return this
    }

    var isEnabled: Boolean
        get() = try {
            Stats.onNativeCall()
            _nIsEnabled(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setEnabled(value)
        }
    
    fun setEnabled(value: Boolean): StrutStyle {
        try {
            Stats.onNativeCall()
            StrutStyle_nSetEnabled(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }
        return this
    }

    var isHeightForced: Boolean
        get() = try {
            Stats.onNativeCall()
            _nIsHeightForced(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setHeightForced(value)
        }
    
    fun setHeightForced(value: Boolean): StrutStyle {
        try {
            Stats.onNativeCall()
            _nSetHeightForced(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }
        return this
    }

    var isHeightOverridden: Boolean
        get() = try {
            Stats.onNativeCall()
            _nIsHeightOverridden(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setHeightOverridden(value)
        }
    
    fun setHeightOverridden(value: Boolean): StrutStyle {
        try {
            Stats.onNativeCall()
            _nSetHeightOverridden(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }
        return this
    }

    @Deprecated("Replaced by topRatio")
    var isHalfLeading: Boolean
        get() = try {
            Stats.onNativeCall()
            _nIsHalfLeading(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            @Suppress("DEPRECATION")
            setHalfLeading(value)
        }

    // Same as topRatio = halfLeading ? 0.5f : -1.0f
    @Deprecated("Replaced by topRatio")
    fun setHalfLeading(value: Boolean): StrutStyle {
        try {
            Stats.onNativeCall()
            _nSetHalfLeading(_ptr, value)
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
            StrutStyle_nGetTopRatio(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setTopRatio(value)
        }

    fun setTopRatio(topRatio: Float): StrutStyle {
        try {
            Stats.onNativeCall()
            StrutStyle_nSetTopRatio(_ptr, topRatio)
        } finally {
            reachabilityBarrier(this)
        }
        return this
    }

    private object _FinalizerHolder {
        val PTR = StrutStyle_nGetFinalizer()
    }
}


@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nGetFinalizer")
private external fun StrutStyle_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nMake")
private external fun StrutStyle_nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nEquals")
private external fun StrutStyle_nEquals(ptr: NativePointer, otherPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nGetHeight")
private external fun StrutStyle_nGetHeight(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nSetHeight")
private external fun StrutStyle_nSetHeight(ptr: NativePointer, value: Float)

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nSetEnabled")
private external fun StrutStyle_nSetEnabled(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nGetFontFamilies")
private external fun _nGetFontFamilies(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nSetFontFamilies")
private external fun StrutStyle_nSetFontFamilies(ptr: NativePointer, families: InteropPointer, familiesCount: Int)

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nGetFontStyle")
private external fun _nGetFontStyle(ptr: NativePointer, fontStyleData: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nSetFontStyle")
private external fun _nSetFontStyle(ptr: NativePointer, value: Int)

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nGetFontSize")
private external fun _nGetFontSize(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nSetFontSize")
private external fun _nSetFontSize(ptr: NativePointer, value: Float)

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nGetLeading")
private external fun _nGetLeading(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nSetLeading")
private external fun _nSetLeading(ptr: NativePointer, value: Float)

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nIsEnabled")
private external fun _nIsEnabled(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nIsHeightForced")
private external fun _nIsHeightForced(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nSetHeightForced")
private external fun _nSetHeightForced(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nIsHeightOverridden")
private external fun _nIsHeightOverridden(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nSetHeightOverridden")
private external fun _nSetHeightOverridden(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nIsHalfLeading")
private external fun _nIsHalfLeading(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nSetHalfLeading")
private external fun _nSetHalfLeading(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nGetTopRatio")
private external fun StrutStyle_nGetTopRatio(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nSetTopRatio")
private external fun StrutStyle_nSetTopRatio(ptr: NativePointer, value: Float)
