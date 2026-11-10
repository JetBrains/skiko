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
    fun pushStyle(style: TextStyle?): ParagraphBuilder = nativeCall(style) { style ->
        _nPushStyle(_ptr, style.pointerOrNullPointer)
        this
    }

    fun popStyle(): ParagraphBuilder = nativeCall {
        _nPopStyle(_ptr, NullPointer)
        this
    }

    fun addText(text: String): ParagraphBuilder = nativeCall {
        interopScope {
            _nAddText(_ptr, toInterop(text))
        }
        if (_text == null) _text = ManagedString(text) else _text!!.append(text)
        this
    }

    fun addPlaceholder(style: PlaceholderStyle): ParagraphBuilder {
        check(!style.width.isNaN())
        check(!style.height.isNaN())
        check(!style.baseline.isNaN())
        return nativeCall {
            _nAddPlaceholder(
                _ptr,
                style.width,
                style.height,
                style.alignment.ordinal,
                style.baselineMode.ordinal,
                style.baseline
            )
            this
        }
    }

    fun build(): Paragraph = nativeCall {
        val paragraph = Paragraph(_nBuild(_ptr), _text)
        _text = null
        paragraph
    }

    internal object _FinalizerHolder {
        val PTR = ParagraphBuilder_nGetFinalizer()
    }
}

private fun makeParagraphBuilder(
    style: ParagraphStyle?,
    fc: FontCollection?
): NativePointer = nativeCallFromStatic(style, fc) { style, fc ->
    _nMake(style.pointerOrNullPointer, fc.pointerOrNullPointer)
}

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphBuilder__1nGetFinalizer")
// No ForceNativeThreadState: returns the address of the delete function without calling it, so it is short and cannot
// block.
// Native call path: deleteParagraphBuilder
private external fun ParagraphBuilder_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphBuilder__1nMake")
// Needs ForceNativeThreadState: on Windows, _wfopen opens icudtl.dat and CreateFileMapping maps it into memory. It also
// copies the paragraph style.
// Native call path: SkUnicodes::ICU::Make -> SkLoadICU -> win_mmap -> _wfopen
@ForceNativeThreadState
private external fun _nMake(paragraphStylePtr: NativePointer, fontCollectionPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphBuilder__1nPushStyle")
// Needs ForceNativeThreadState: copies a text style and compares its font family, shadow and feature lists, so its time
// grows with their lengths.
// Native call path: ParagraphBuilderImpl::pushStyle -> TextStyle::equals
@ForceNativeThreadState
private external fun _nPushStyle(ptr: NativePointer, textStylePtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphBuilder__1nPopStyle")
// Needs ForceNativeThreadState: removing a style can release the last reference to a font typeface or paint shader.
// Native call path: ParagraphBuilderImpl::pop -> ~TextStyle
@ForceNativeThreadState
private external fun _nPopStyle(ptr: NativePointer, textStylePtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphBuilder__1nAddText")
// Needs ForceNativeThreadState: copies the whole input text into a string buffer, so its time grows with the text
// length.
// Native call path: ParagraphBuilderImpl::addText -> SkString::append
@ForceNativeThreadState
private external fun _nAddText(ptr: NativePointer, text: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphBuilder__1nAddPlaceholder")
// Needs ForceNativeThreadState: copies the current style, including its font family and feature lists, so its time
// grows with how many entries they hold.
// Native call path: ParagraphBuilderImpl::addPlaceholder -> TextStyle::cloneForPlaceholder
@ForceNativeThreadState
private external fun _nAddPlaceholder(
    ptr: NativePointer,
    width: Float,
    height: Float,
    alignment: Int,
    baselineMode: Int,
    baseline: Float
)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphBuilder__1nBuild")
// Needs ForceNativeThreadState: builds the paragraph by copying the text, styled blocks and placeholders, so its time
// grows with the input size.
// Native call path: ParagraphBuilderImpl::Build -> make_unique<ParagraphImpl>
@ForceNativeThreadState
private external fun _nBuild(ptr: NativePointer): NativePointer
