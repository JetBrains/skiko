package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.interopScope

/**
 * Kotlin mirror of std::vector&lt;jchar&gt; (UTF-16)
 */
class U16String internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        init {
            staticLoad()
        }
    }

    constructor(s: String?) : this(interopScope { U16String_nMake(toInterop(s), s?.length ?: 0) }) {
        Stats.onNativeCall()
    }

    override fun toString(): String {
        return try {
            Stats.onNativeCall()
            U16String_nToString(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    private object _FinalizerHolder {
        val PTR = U16String_nGetFinalizer()
    }
}


@ExternalSymbolName("org_jetbrains_skia_U16String__1nMake")
private external fun U16String_nMake(s: InteropPointer, len: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_U16String__1nGetFinalizer")
private external fun U16String_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_U16String__1nToString")
private external fun U16String_nToString(ptr: NativePointer): String
