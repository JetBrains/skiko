package org.jetbrains.skia.paragraph

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.ManagedString
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.*

class ParagraphBuilder(style: ParagraphStyle?, fc: FontCollection?) :
    Managed(_nMake(getPtr(style), getPtr(fc)), _FinalizerHolder.PTR) {
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
        _nPopStyle(_ptr)
        return this
    }

    fun addText(text: String): ParagraphBuilder {
        Stats.onNativeCall()
        interopScope {
            _nAddText(_ptr, toInterop(text))
        }
        if (_text == null) _text = ManagedString(text) else _text!!.append(text)
        return this
    }

    fun addPlaceholder(style: PlaceholderStyle): ParagraphBuilder {
        Stats.onNativeCall()
        _nAddPlaceholder(
            _ptr,
            style.width,
            style.height,
            style.alignment.ordinal,
            style.baselineMode.ordinal,
            style.baseline
        )
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

    init {
        Stats.onNativeCall()
        reachabilityBarrier(style)
        reachabilityBarrier(fc)
    }
}

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphBuilder__1nGetFinalizer")
private external fun ParagraphBuilder_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphBuilder__1nMake")
private external fun _nMake(paragraphStylePtr: NativePointer, fontCollectionPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphBuilder__1nPushStyle")
private external fun _nPushStyle(ptr: NativePointer, textStylePtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphBuilder__1nPopStyle")
private external fun _nPopStyle(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphBuilder__1nAddText")
private external fun _nAddText(ptr: NativePointer, text: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphBuilder__1nAddPlaceholder")
private external fun _nAddPlaceholder(
    ptr: NativePointer,
    width: Float,
    height: Float,
    alignment: Int,
    baselineMode: Int,
    baseline: Float
)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphBuilder__1nBuild")
private external fun _nBuild(ptr: NativePointer): NativePointer
