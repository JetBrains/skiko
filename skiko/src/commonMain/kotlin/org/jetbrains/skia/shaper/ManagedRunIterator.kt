package org.jetbrains.skia.shaper

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ForceNativeThreadState
import org.jetbrains.skia.ManagedString
import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad

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

    internal val _text: ManagedString? = if (manageText) text else null
    override fun close() {
        super.close()
        _text?.close()
    }

    internal fun _getEndOfCurrentRun(): Int = nativeCall(_text) { text ->
        _nGetEndOfCurrentRun(_ptr, text.pointerOrNullPointer)
    }

    override fun hasNext(): Boolean = nativeCall { !_nIsAtEnd(_ptr) }

    internal object _FinalizerHolder {
        val PTR = _nGetFinalizer()
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
