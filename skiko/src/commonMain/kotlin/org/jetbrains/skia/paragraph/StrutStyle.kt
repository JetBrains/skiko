package org.jetbrains.skia.paragraph

import org.jetbrains.skia.ArrayDecoder
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.FontSlant
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.ManagedString_nGetFinalizer
import org.jetbrains.skia.arrayDecoderScope
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
            arrayDecoderScope({
                ArrayDecoder(
                    StrutStyle_nGetFontFamilies(_ptr),
                    ManagedString_nGetFinalizer()
                )
            }) { arrayDecoder ->
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
                StrutStyle_nGetFontStyle(_ptr, it)
            }
            FontStyle(fontStyleData[0], fontStyleData[1], FontSlant.values()[fontStyleData[2]])
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setFontStyle(value)
        }
    
    fun setFontStyle(style: FontStyle): StrutStyle {
        try {
            Stats.onNativeCall()
            StrutStyle_nSetFontStyle(_ptr, style._value)
        } finally {
            reachabilityBarrier(this)
        }
        return this
    }

    var fontSize: Float
        get() = try {
            Stats.onNativeCall()
            StrutStyle_nGetFontSize(_ptr)
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
            StrutStyle_nSetFontSize(_ptr, value)
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
            StrutStyle_nGetLeading(_ptr)
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
            StrutStyle_nSetLeading(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }
        return this
    }

    var isEnabled: Boolean
        get() = try {
            Stats.onNativeCall()
            StrutStyle_nIsEnabled(_ptr)
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
            StrutStyle_nIsHeightForced(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setHeightForced(value)
        }
    
    fun setHeightForced(value: Boolean): StrutStyle {
        try {
            Stats.onNativeCall()
            StrutStyle_nSetHeightForced(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }
        return this
    }

    var isHeightOverridden: Boolean
        get() = try {
            Stats.onNativeCall()
            StrutStyle_nIsHeightOverridden(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setHeightOverridden(value)
        }
    
    fun setHeightOverridden(value: Boolean): StrutStyle {
        try {
            Stats.onNativeCall()
            StrutStyle_nSetHeightOverridden(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }
        return this
    }

    @Deprecated("Replaced by topRatio")
    var isHalfLeading: Boolean
        get() = try {
            Stats.onNativeCall()
            StrutStyle_nIsHalfLeading(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setHalfLeading(value)
        }

    // Same as topRatio = halfLeading ? 0.5f : -1.0f
    @Deprecated("Replaced by topRatio")
    fun setHalfLeading(value: Boolean): StrutStyle {
        try {
            Stats.onNativeCall()
            StrutStyle_nSetHalfLeading(_ptr, value)
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