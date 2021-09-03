package org.jetbrains.skia.paragraph

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.*
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.impl.Stats
import java.lang.ref.Reference

class StrutStyle internal constructor(ptr: Long) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        @JvmStatic external fun _nGetFinalizer(): Long
        @JvmStatic external fun _nMake(): Long
        @JvmStatic external fun _nEquals(ptr: Long, otherPtr: Long): Boolean
        @JvmStatic external fun _nGetFontFamilies(ptr: Long): Array<String>
        @JvmStatic external fun _nSetFontFamilies(ptr: Long, families: Array<String?>?)
        @JvmStatic external fun _nGetFontStyle(ptr: Long): Int
        @JvmStatic external fun _nSetFontStyle(ptr: Long, value: Int)
        @JvmStatic external fun _nGetFontSize(ptr: Long): Float
        @JvmStatic external fun _nSetFontSize(ptr: Long, value: Float)
        @JvmStatic external fun _nGetHeight(ptr: Long): Float
        @JvmStatic external fun _nSetHeight(ptr: Long, value: Float)
        @JvmStatic external fun _nGetLeading(ptr: Long): Float
        @JvmStatic external fun _nSetLeading(ptr: Long, value: Float)
        @JvmStatic external fun _nIsEnabled(ptr: Long): Boolean
        @JvmStatic external fun _nSetEnabled(ptr: Long, value: Boolean)
        @JvmStatic external fun _nIsHeightForced(ptr: Long): Boolean
        @JvmStatic external fun _nSetHeightForced(ptr: Long, value: Boolean)
        @JvmStatic external fun _nIsHeightOverridden(ptr: Long): Boolean
        @JvmStatic external fun _nSetHeightOverridden(ptr: Long, value: Boolean)

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
            _nEquals(_ptr, Native.getPtr(other))
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(other)
        }
    }

    val fontFamilies: Array<String>
        get() = try {
            Stats.onNativeCall()
            _nGetFontFamilies(_ptr)
        } finally {
            Reference.reachabilityFence(this)
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
            Reference.reachabilityFence(this)
        }
        set(value) {
            Stats.onNativeCall()
            _nSetFontStyle(_ptr, value._value)
        }

    var fontSize: Float
        get() = try {
            Stats.onNativeCall()
            _nGetFontSize(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
        set(value) {
            Stats.onNativeCall()
            _nSetFontSize(_ptr, value)
        }

    var height: Float
        get() = try {
            Stats.onNativeCall()
            _nGetHeight(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
        set(value) {
            Stats.onNativeCall()
            _nSetHeight(_ptr, value)
        }

    var leading: Float
        get() = try {
            Stats.onNativeCall()
            _nGetLeading(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
        set(value) {
            Stats.onNativeCall()
            _nSetLeading(_ptr, value)
        }

    var isEnabled: Boolean
        get() = try {
            Stats.onNativeCall()
            _nIsEnabled(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
        set(value) {
            Stats.onNativeCall()
            _nSetEnabled(_ptr, value)
        }

    var isHeightForced: Boolean
        get() = try {
            Stats.onNativeCall()
            _nIsHeightForced(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
        set(value) {
            Stats.onNativeCall()
            _nSetHeightForced(_ptr, value)
        }

    var isHeightOverridden: Boolean
        get() = try {
            Stats.onNativeCall()
            _nIsHeightOverridden(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
        set(value) {
            Stats.onNativeCall()
            _nSetHeightOverridden(_ptr, value)
        }

    private object _FinalizerHolder {
        val PTR = _nGetFinalizer()
    }
}