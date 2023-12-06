@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia.shaper

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ModuleImport
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


@ExternalSymbolName("org_jetbrains_skia_shaper_ManagedRunIterator__1nGetFinalizer")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_shaper_ManagedRunIterator__1nGetFinalizer")
private external fun _nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_ManagedRunIterator__1nConsume")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_shaper_ManagedRunIterator__1nConsume")
internal external fun _nConsume(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_shaper_ManagedRunIterator__1nGetEndOfCurrentRun")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_shaper_ManagedRunIterator__1nGetEndOfCurrentRun")
private external fun _nGetEndOfCurrentRun(ptr: NativePointer, textPtr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_shaper_ManagedRunIterator__1nIsAtEnd")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_shaper_ManagedRunIterator__1nIsAtEnd")
private external fun _nIsAtEnd(ptr: NativePointer): Boolean
