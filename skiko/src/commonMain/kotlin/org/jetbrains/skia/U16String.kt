package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Managed
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

    private object _FinalizerHolder {
        val PTR = U16String_nGetFinalizer()
    }
}

@ExternalSymbolName("org_jetbrains_skia_U16String__1nGetFinalizer")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_U16String__1nGetFinalizer")
private external fun U16String_nGetFinalizer(): NativePointer