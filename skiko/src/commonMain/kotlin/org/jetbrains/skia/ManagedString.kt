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

    override fun toString(): String = nativeCall {
        val size = _nStringSize(_ptr)
        withResult(ByteArray(size)) {
            _nStringData(_ptr, it, size)
        }.decodeToString()
    }

    fun insert(offset: Int, s: String): ManagedString = nativeCall {
        interopScope {
            _nInsert(_ptr, offset, toInterop(s))
        }
        this
    }

    fun append(s: String): ManagedString = nativeCall {
        interopScope {
            _nAppend(_ptr, toInterop(s))
        }
        this
    }

    fun remove(from: Int): ManagedString = nativeCall {
        _nRemoveSuffix(_ptr, from)
        this
    }

    fun remove(from: Int, length: Int): ManagedString = nativeCall {
        _nRemove(_ptr, from, length)
        this
    }

    internal object _FinalizerHolder {
        val PTR = ManagedString_nGetFinalizer()
    }
}

@ExternalSymbolName("org_jetbrains_skia_ManagedString__1nGetFinalizer")
// No ForceNativeThreadState: returns the address of a cleanup function without running it, so it is short and cannot
// block.
// Native call path: skikoMpp::finalizers::deleteString
internal external fun ManagedString_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ManagedString__1nMake")
// Needs ForceNativeThreadState: copies the whole text into a new string, so its time grows with the text length.
// Native call path: skString -> SkString::SkString
@ForceNativeThreadState
private external fun _nMake(s: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ManagedString__nStringSize")
// No ForceNativeThreadState: returns the stored length of the string, so it is short and cannot block.
// Native call path: SkString::size
private external fun _nStringSize(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_ManagedString__nStringData")
// Needs ForceNativeThreadState: copies the whole string into the caller's buffer, so its time grows with the string
// length.
// Native call path: memcpy
@ForceNativeThreadState
private external fun _nStringData(ptr: NativePointer, result: InteropPointer, size: Int)

@ExternalSymbolName("org_jetbrains_skia_ManagedString__1nInsert")
// Needs ForceNativeThreadState: scans the text up to the insert position and then moves the rest of the text, so its
// time grows with the text length.
// Native call path: UtfIndicesConverter::from16To8 -> SkString::insert
@ForceNativeThreadState
private external fun _nInsert(ptr: NativePointer, offset: Int, s: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_ManagedString__1nAppend")
// Needs ForceNativeThreadState: copies the appended text and reallocates the string, so its time grows with the
// combined text length.
// Native call path: skString -> SkString::append
@ForceNativeThreadState
private external fun _nAppend(ptr: NativePointer, s: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_ManagedString__1nRemoveSuffix")
// Needs ForceNativeThreadState: scans the text up to the cut position and then shifts the bytes after it, so its time
// grows with the text length.
// Native call path: UtfIndicesConverter::from16To8 -> SkString::remove
@ForceNativeThreadState
private external fun _nRemoveSuffix(ptr: NativePointer, from: Int)

@ExternalSymbolName("org_jetbrains_skia_ManagedString__1nRemove")
// Needs ForceNativeThreadState: scans the text up to both ends of the removed range and then shifts the rest, so its
// time grows with the text length.
// Native call path: UtfIndicesConverter::from16To8 -> SkString::remove
@ForceNativeThreadState
private external fun _nRemove(ptr: NativePointer, from: Int, length: Int)
