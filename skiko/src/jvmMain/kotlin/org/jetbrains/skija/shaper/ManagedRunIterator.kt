package org.jetbrains.skija.shaper

import org.jetbrains.skija.impl.Library.Companion.staticLoad
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.skija.ManagedString
import org.jetbrains.skija.impl.Managed
import org.jetbrains.skija.impl.Native
import java.lang.ref.Reference

abstract class ManagedRunIterator<T> @ApiStatus.Internal constructor(
    ptr: Long,
    text: ManagedString?,
    manageText: Boolean
) : Managed(ptr, _FinalizerHolder.PTR), MutableIterator<T> {
    companion object {
        @ApiStatus.Internal
        external fun _nGetFinalizer(): Long
        @ApiStatus.Internal
        external fun _nConsume(ptr: Long)
        @ApiStatus.Internal
        external fun _nGetEndOfCurrentRun(ptr: Long, textPtr: Long): Int
        @ApiStatus.Internal
        external fun _nIsAtEnd(ptr: Long): Boolean

        init {
            staticLoad()
        }
    }

    @ApiStatus.Internal
    val _text: ManagedString?
    override fun close() {
        super.close()
        _text?.close()
    }

    @ApiStatus.Internal
    fun _getEndOfCurrentRun(): Int {
        return try {
            _nGetEndOfCurrentRun(_ptr, Native.Companion.getPtr(_text))
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(_text)
        }
    }

    override fun hasNext(): Boolean {
        return try {
            !_nIsAtEnd(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    @ApiStatus.Internal
    object _FinalizerHolder {
        val PTR = _nGetFinalizer()
    }

    init {
        _text = if (manageText) text else null
    }
}