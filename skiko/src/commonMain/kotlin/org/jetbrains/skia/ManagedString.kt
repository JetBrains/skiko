@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.NativePointer
import kotlin.jvm.JvmStatic

class ManagedString internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ManagedString__1nMake")
        external fun _nMake(s: String?): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ManagedString__1nGetFinalizer")
        external fun _nGetFinalizer(): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ManagedString__1nToString")
        external fun _nToString(ptr: NativePointer): String
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ManagedString__1nInsert")
        external fun _nInsert(ptr: NativePointer, offset: Int, s: String?)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ManagedString__1nAppend")
        external fun _nAppend(ptr: NativePointer, s: String?)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ManagedString__1nRemoveSuffix")
        external fun _nRemoveSuffix(ptr: NativePointer, from: Int)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ManagedString__1nRemove")
        external fun _nRemove(ptr: NativePointer, from: Int, length: Int)

        init {
            staticLoad()
        }
    }

    constructor(s: String?) : this(_nMake(s)) {
        Stats.onNativeCall()
    }

    override fun toString(): String {
        return try {
            Stats.onNativeCall()
            _nToString(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun insert(offset: Int, s: String): ManagedString {
        Stats.onNativeCall()
        _nInsert(_ptr, offset, s)
        return this
    }

    fun append(s: String): ManagedString {
        Stats.onNativeCall()
        _nAppend(_ptr, s)
        return this
    }

    fun remove(from: Int): ManagedString {
        Stats.onNativeCall()
        _nRemoveSuffix(_ptr, from)
        return this
    }

    fun remove(from: Int, length: Int): ManagedString {
        Stats.onNativeCall()
        _nRemove(_ptr, from, length)
        return this
    }

    internal object _FinalizerHolder {
        val PTR = _nGetFinalizer()
    }
}