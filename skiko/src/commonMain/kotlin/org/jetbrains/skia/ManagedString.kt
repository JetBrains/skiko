package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad

class ManagedString internal constructor(ptr: NativePointer, managed: Boolean = true) : Managed(ptr, _FinalizerHolder.PTR, managed) {
    companion object {
        init {
            staticLoad()
        }
    }

    constructor(s: String?) : this(
        interopScope {  _nMake(toInterop(s)) }
    ) {
        Stats.onNativeCall()
    }

    override fun toString(): String {
        return try {
            Stats.onNativeCall()
            val size = _nStringSize(_ptr)
            withResult(ByteArray(size)) {
                _nStringData(_ptr, it, size)
            }.decodeToString()
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun insert(offset: Int, s: String): ManagedString {
        Stats.onNativeCall()
        interopScope {
            _nInsert(_ptr, offset, toInterop(s))
        }
        return this
    }

    fun append(s: String): ManagedString {
        Stats.onNativeCall()
        interopScope {
            _nAppend(_ptr, toInterop(s))
        }
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
        val PTR = ManagedString_nGetFinalizer()
    }
}

// Runnable: returns the address of skikoMpp::finalizers::deleteString, no work here
@ExternalSymbolName("org_jetbrains_skia_ManagedString__1nGetFinalizer")
internal external fun ManagedString_nGetFinalizer(): NativePointer

// Native: skString(textStr) runs strlen and copies the whole text into a new SkString
@ExternalSymbolName("org_jetbrains_skia_ManagedString__1nMake")
@ForceNativeThreadState
private external fun _nMake(s: InteropPointer): NativePointer

// Runnable: SkString::size returns the stored length field
@ExternalSymbolName("org_jetbrains_skia_ManagedString__nStringSize")
private external fun _nStringSize(ptr: NativePointer): Int

// Native: memcpy copies the whole string into the caller buffer
@ExternalSymbolName("org_jetbrains_skia_ManagedString__nStringData")
@ForceNativeThreadState
private external fun _nStringData(ptr: NativePointer, result: InteropPointer, size: Int)

// Native: UtfIndicesConverter::from16To8 scans UTF-8 up to the offset, then SkString::insert copies
@ExternalSymbolName("org_jetbrains_skia_ManagedString__1nInsert")
@ForceNativeThreadState
private external fun _nInsert(ptr: NativePointer, offset: Int, s: InteropPointer)

// Native: skString(s) strlen-copies the argument, then SkString::append reallocates and copies
@ExternalSymbolName("org_jetbrains_skia_ManagedString__1nAppend")
@ForceNativeThreadState
private external fun _nAppend(ptr: NativePointer, s: InteropPointer)

// Native: UtfIndicesConverter::from16To8 scans UTF-8 to the offset, then SkString::remove shifts bytes
@ExternalSymbolName("org_jetbrains_skia_ManagedString__1nRemoveSuffix")
@ForceNativeThreadState
private external fun _nRemoveSuffix(ptr: NativePointer, from: Int)

// Native: UtfIndicesConverter::from16To8 scans UTF-8 to from and to, then SkString::remove shifts bytes
@ExternalSymbolName("org_jetbrains_skia_ManagedString__1nRemove")
@ForceNativeThreadState
private external fun _nRemove(ptr: NativePointer, from: Int, length: Int)
