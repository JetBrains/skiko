@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.impl.NativePointer

class ManagedString internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
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


@ExternalSymbolName("org_jetbrains_skia_ManagedString__1nMake")
private external fun _nMake(s: String?): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ManagedString__1nGetFinalizer")
private external fun _nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ManagedString__1nToString")
private external fun _nToString(ptr: NativePointer): String

@ExternalSymbolName("org_jetbrains_skia_ManagedString__1nInsert")
private external fun _nInsert(ptr: NativePointer, offset: Int, s: String?)

@ExternalSymbolName("org_jetbrains_skia_ManagedString__1nAppend")
private external fun _nAppend(ptr: NativePointer, s: String?)

@ExternalSymbolName("org_jetbrains_skia_ManagedString__1nRemoveSuffix")
private external fun _nRemoveSuffix(ptr: NativePointer, from: Int)

@ExternalSymbolName("org_jetbrains_skia_ManagedString__1nRemove")
private external fun _nRemove(ptr: NativePointer, from: Int, length: Int)
