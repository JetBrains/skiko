@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import kotlin.jvm.JvmStatic

/**
 * Kotlin mirror of std::vector&lt;jchar&gt; (UTF-16)
 */
class U16String internal constructor(ptr: Long) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        @JvmStatic external fun _nMake(s: String?): Long
        @JvmStatic external fun _nGetFinalizer(): Long
        @JvmStatic external fun _nToString(ptr: Long): String

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