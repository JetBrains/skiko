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

class StrutStyle internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        init {
            staticLoad()
        }
    }

    constructor() : this(StrutStyle_nMake()) {
        Stats.onNativeCall()
    }

    override fun _nativeEquals(other: Native?): Boolean {
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
            _nGetFontFamilies(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    fun setFontFamilies(families: Array<String?>?): StrutStyle {
        Stats.onNativeCall()
        _nSetFontFamilies(_ptr, families)
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
    
    fun setFontStyle(style: FontStyle): StrutStyle {
        Stats.onNativeCall()
        _nSetFontStyle(_ptr, style._value)
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
        Stats.onNativeCall()
        _nSetFontSize(_ptr, value)
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
        Stats.onNativeCall()
        StrutStyle_nSetHeight(_ptr, value)
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
        Stats.onNativeCall()
        _nSetLeading(_ptr, value)
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
        Stats.onNativeCall()
        StrutStyle_nSetEnabled(_ptr, value)
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
        Stats.onNativeCall()
        _nSetHeightForced(_ptr, value)
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
        Stats.onNativeCall()
        _nSetHeightOverridden(_ptr, value)
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
private external fun _nGetFontFamilies(ptr: NativePointer): Array<String>

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nSetFontFamilies")
private external fun _nSetFontFamilies(ptr: NativePointer, families: Array<String?>?)

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nGetFontStyle")
private external fun _nGetFontStyle(ptr: NativePointer): Int

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
