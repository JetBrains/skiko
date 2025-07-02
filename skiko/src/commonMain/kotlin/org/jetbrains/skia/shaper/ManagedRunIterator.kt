package org.jetbrains.skia.shaper

import org.jetbrains.skia.ManagedString
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.reachabilityBarrier

abstract class ManagedRunIterator<T> internal constructor(
    ptr: NativePointer,
    text: ManagedString?,
    manageText: Boolean
) : Managed(ptr, _FinalizerHolder.PTR), MutableIterator<T> {
    companion object {
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
            ManagedRunIterator_nGetEndOfCurrentRun(_ptr, getPtr(_text))
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(_text)
        }
    }

    override fun hasNext(): Boolean {
        return try {
            !ManagedRunIterator_nIsAtEnd(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    internal object _FinalizerHolder {
        val PTR = ManagedRunIterator_nGetFinalizer()
    }

    init {
        _text = if (manageText) text else null
    }
}