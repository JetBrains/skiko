package org.jetbrains.skia

import org.jetbrains.skia.impl.Library
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import java.io.OutputStream

class OutputWStream(out: OutputStream?) : WStream(_nMake(out), _FinalizerHolder.PTR) {
    companion object {
        init {
            Library.staticLoad()
        }
    }

    private val _out: OutputStream?
    private object _FinalizerHolder {
        val PTR = OutputWStream_nGetFinalizer()
    }

    init {
        Stats.onNativeCall()
        _out = out
    }
}

@ExternalSymbolName("org_jetbrains_skia_OutputWStream__1nGetFinalizer")
@ModuleImport("skia", "org_jetbrains_skia_OutputWStream__1nGetFinalizer")
private external fun OutputWStream_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_OutputWStream__1nMake")
@ModuleImport("skia", "org_jetbrains_skia_OutputWStream__1nMake")
private external fun _nMake(out: OutputStream?): NativePointer