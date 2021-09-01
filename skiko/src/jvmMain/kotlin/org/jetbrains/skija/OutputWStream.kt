package org.jetbrains.skija

import org.jetbrains.skija.impl.Library.Companion.staticLoad
import org.jetbrains.skija.impl.Stats
import java.io.OutputStream

class OutputWStream(out: OutputStream?) : WStream(_nMake(out), _FinalizerHolder.PTR) {
    companion object {
        @JvmStatic external fun _nGetFinalizer(): Long
        @JvmStatic external fun _nMake(out: OutputStream?): Long

        init {
            staticLoad()
        }
    }

    private val _out: OutputStream?

    internal object _FinalizerHolder {
        val PTR = _nGetFinalizer()
    }

    init {
        Stats.onNativeCall()
        _out = out
    }
}