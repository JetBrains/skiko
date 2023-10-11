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

@ExternalSymbolName("org_jetbrains_skia_ManagedString__1nGetFinalizer")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_ManagedString__1nGetFinalizer")
internal external fun ManagedString_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ManagedString__1nMake")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_ManagedString__1nMake")
private external fun _nMake(s: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ManagedString__nStringSize")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_ManagedString__nStringSize")
private external fun _nStringSize(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_ManagedString__nStringData")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_ManagedString__nStringData")
private external fun _nStringData(ptr: NativePointer, result: InteropPointer, size: Int)

@ExternalSymbolName("org_jetbrains_skia_ManagedString__1nInsert")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_ManagedString__1nInsert")
private external fun _nInsert(ptr: NativePointer, offset: Int, s: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_ManagedString__1nAppend")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_ManagedString__1nAppend")
private external fun _nAppend(ptr: NativePointer, s: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_ManagedString__1nRemoveSuffix")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_ManagedString__1nRemoveSuffix")
private external fun _nRemoveSuffix(ptr: NativePointer, from: Int)

@ExternalSymbolName("org_jetbrains_skia_ManagedString__1nRemove")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_ManagedString__1nRemove")
private external fun _nRemove(ptr: NativePointer, from: Int, length: Int)
