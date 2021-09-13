@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.impl.NativePointer

/**
 * Kotlin mirror of std::vector&lt;jchar&gt; (UTF-16)
 */
class U16String internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
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

    private object _FinalizerHolder {
        val PTR = _nGetFinalizer()
    }
}


@ExternalSymbolName("org_jetbrains_skia_U16String__1nMake")
private external fun _nMake(s: String?): NativePointer

@ExternalSymbolName("org_jetbrains_skia_U16String__1nGetFinalizer")
private external fun _nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_U16String__1nToString")
private external fun _nToString(ptr: NativePointer): String
