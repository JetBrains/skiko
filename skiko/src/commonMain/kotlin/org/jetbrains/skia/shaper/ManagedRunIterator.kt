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


// Runnable: returns the address of deleteRunIterator only
@ExternalSymbolName("org_jetbrains_skia_shaper_ManagedRunIterator__1nGetFinalizer")
private external fun _nGetFinalizer(): NativePointer

// Native: FontRunIterator::consume -> SkTypeface::unicharToGlyph -> SkTypeface_Mac::onCharsToGlyphs -> CoreText
@ExternalSymbolName("org_jetbrains_skia_shaper_ManagedRunIterator__1nConsume")
@ForceNativeThreadState
internal external fun _nConsume(ptr: NativePointer)

// Native: UtfIndicesConverter::from8To16 rescans the UTF-8 text from its start on every call
@ExternalSymbolName("org_jetbrains_skia_shaper_ManagedRunIterator__1nGetEndOfCurrentRun")
@ForceNativeThreadState
private external fun _nGetEndOfCurrentRun(ptr: NativePointer, textPtr: NativePointer): Int

// Native: SkikoRunIterator::atEnd -> KCallback::operator() -> callBooleanCallback -> Kotlin
@ExternalSymbolName("org_jetbrains_skia_shaper_ManagedRunIterator__1nIsAtEnd")
@ForceNativeThreadState
private external fun _nIsAtEnd(ptr: NativePointer): Boolean
