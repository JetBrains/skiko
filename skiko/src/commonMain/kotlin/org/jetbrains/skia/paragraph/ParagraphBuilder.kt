package org.jetbrains.skia.paragraph

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.ManagedString
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ForceNativeThreadState
import org.jetbrains.skia.impl.*

class ParagraphBuilder(style: ParagraphStyle?, fc: FontCollection?) :
    Managed(makeParagraphBuilder(style, fc), _FinalizerHolder.PTR) {
    companion object {
        init {
            staticLoad()
        }
    }

    private var _text: ManagedString? = null
    fun pushStyle(style: TextStyle?): ParagraphBuilder {
        return try {
            Stats.onNativeCall()
            _nPushStyle(_ptr, getPtr(style))
            this
        } finally {
            reachabilityBarrier(style)
        }
    }

    fun popStyle(): ParagraphBuilder {
        Stats.onNativeCall()
        try {
            _nPopStyle(_ptr, NullPointer)
        } finally {
            reachabilityBarrier(this)
        }
        return this
    }

    fun addText(text: String): ParagraphBuilder {
        Stats.onNativeCall()
        try {
            interopScope {
                _nAddText(_ptr, toInterop(text))
            }
        } finally {
            reachabilityBarrier(this)
        }
        if (_text == null) _text = ManagedString(text) else _text!!.append(text)
        return this
    }

    fun addPlaceholder(style: PlaceholderStyle): ParagraphBuilder {
        check(!style.width.isNaN())
        check(!style.height.isNaN())
        check(!style.baseline.isNaN())
        try {
            Stats.onNativeCall()
            _nAddPlaceholder(
                _ptr,
                style.width,
                style.height,
                style.alignment.ordinal,
                style.baselineMode.ordinal,
                style.baseline
            )
        } finally {
            reachabilityBarrier(this)
        }
        return this
    }

    fun build(): Paragraph {
        return try {
            Stats.onNativeCall()
            val paragraph = Paragraph(_nBuild(_ptr), _text)
            _text = null
            paragraph
        } finally {
            reachabilityBarrier(this)
        }
    }

    internal object _FinalizerHolder {
        val PTR = ParagraphBuilder_nGetFinalizer()
    }
}

private fun makeParagraphBuilder(
    style: ParagraphStyle?,
    fc: FontCollection?
): NativePointer {
    Stats.onNativeCall()
    return try {
        _nMake(getPtr(style), getPtr(fc))
    } finally {
        reachabilityBarrier(style)
        reachabilityBarrier(fc)
    }
}

// Runnable: returns &deleteParagraphBuilder; the delete itself runs in Managed__invokeFinalizer
@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphBuilder__1nGetFinalizer")
private external fun ParagraphBuilder_nGetFinalizer(): NativePointer

// Native: SkUnicodes::ICU::Make -> SkLoadICU -> win_mmap reads icudtl.dat via _wfopen (Windows)
@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphBuilder__1nMake")
@ForceNativeThreadState
private external fun _nMake(paragraphStylePtr: NativePointer, fontCollectionPtr: NativePointer): NativePointer

// Native: ParagraphBuilderImpl::pushStyle copies TextStyle vectors and calls TextStyle::equals
@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphBuilder__1nPushStyle")
@ForceNativeThreadState
private external fun _nPushStyle(ptr: NativePointer, textStylePtr: NativePointer)

// Native: ParagraphBuilderImpl::pop -> ~TextStyle/~Block can drop the last typeface or shader ref
@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphBuilder__1nPopStyle")
@ForceNativeThreadState
private external fun _nPopStyle(ptr: NativePointer, textStylePtr: NativePointer)

// Native: ParagraphBuilderImpl::addText -> SkString append copies the whole text
@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphBuilder__1nAddText")
@ForceNativeThreadState
private external fun _nAddText(ptr: NativePointer, text: InteropPointer)

// Native: ParagraphBuilderImpl::addPlaceholder -> TextStyle::cloneForPlaceholder copies style vectors
@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphBuilder__1nAddPlaceholder")
@ForceNativeThreadState
private external fun _nAddPlaceholder(
    ptr: NativePointer,
    width: Float,
    height: Float,
    alignment: Int,
    baselineMode: Int,
    baseline: Float
)

// Native: ParagraphBuilderImpl::Build -> make_unique<ParagraphImpl> copies text, blocks and placeholders
@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphBuilder__1nBuild")
@ForceNativeThreadState
private external fun _nBuild(ptr: NativePointer): NativePointer
