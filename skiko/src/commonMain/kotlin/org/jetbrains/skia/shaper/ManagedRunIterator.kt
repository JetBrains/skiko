@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia.shaper

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.ManagedString
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.ExternalSymbolName
import kotlin.jvm.JvmStatic

abstract class ManagedRunIterator<T> internal constructor(
    ptr: Long,
    text: ManagedString?,
    manageText: Boolean
) : Managed(ptr, _FinalizerHolder.PTR), MutableIterator<T> {
    companion object {
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ManagedRunIterator__1nGetFinalizer")
        external fun _nGetFinalizer(): Long
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ManagedRunIterator__1nConsume")
        external fun _nConsume(ptr: Long)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ManagedRunIterator__1nGetEndOfCurrentRun")
        external fun _nGetEndOfCurrentRun(ptr: Long, textPtr: Long): Int
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ManagedRunIterator__1nIsAtEnd")
        external fun _nIsAtEnd(ptr: Long): Boolean

        init {
            staticLoad()
        }
    }

    internal val _text: ManagedString?
    override fun close() {
        super.close()
        _text?.close()
    }

    internal fun _getEndOfCurrentRun(): Int {
        return try {
            _nGetEndOfCurrentRun(_ptr, getPtr(_text))
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(_text)
        }
    }

    override fun hasNext(): Boolean {
        return try {
            !_nIsAtEnd(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    internal object _FinalizerHolder {
        val PTR = _nGetFinalizer()
    }

    init {
        _text = if (manageText) text else null
    }
}