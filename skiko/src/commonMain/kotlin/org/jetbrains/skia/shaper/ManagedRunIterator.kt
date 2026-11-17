package org.jetbrains.skia.shaper

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ForceNativeThreadState
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
// No ForceNativeThreadState: only returns the address of a delete function, so it is short and cannot block.
// Native call path: deleteRunIterator
private external fun _nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_ManagedRunIterator__1nConsume")
// Needs ForceNativeThreadState: maps each character to a glyph through CoreText, a platform API.
// Native call path: FontRunIterator::consume -> SkTypeface::unicharToGlyph -> SkTypeface_Mac::onCharsToGlyphs
@ForceNativeThreadState
internal external fun _nConsume(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_shaper_ManagedRunIterator__1nGetEndOfCurrentRun")
// Needs ForceNativeThreadState: rescans the UTF-8 text from its start on every call, so its time grows with the text
// length.
// Native call path: UtfIndicesConverter::from8To16
@ForceNativeThreadState
private external fun _nGetEndOfCurrentRun(ptr: NativePointer, textPtr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_shaper_ManagedRunIterator__1nIsAtEnd")
// Needs ForceNativeThreadState: checking whether the iterator is at its end calls a Kotlin callback.
// Native call path: SkikoRunIterator::atEnd -> KCallback::operator() -> callBooleanCallback
@ForceNativeThreadState
private external fun _nIsAtEnd(ptr: NativePointer): Boolean
