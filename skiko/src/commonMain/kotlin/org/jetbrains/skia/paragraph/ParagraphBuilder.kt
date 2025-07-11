package org.jetbrains.skia.paragraph

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.ManagedString
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ModuleImport
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

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphBuilder__1nGetFinalizer")
@ModuleImport("org_jetbrains_skia_paragraph_ParagraphBuilder__1nGetFinalizer")
private external fun ParagraphBuilder_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphBuilder__1nMake")
@ModuleImport("org_jetbrains_skia_paragraph_ParagraphBuilder__1nMake")
private external fun _nMake(paragraphStylePtr: NativePointer, fontCollectionPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphBuilder__1nPushStyle")
@ModuleImport("org_jetbrains_skia_paragraph_ParagraphBuilder__1nPushStyle")
private external fun _nPushStyle(ptr: NativePointer, textStylePtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphBuilder__1nPopStyle")
@ModuleImport("org_jetbrains_skia_paragraph_ParagraphBuilder__1nPopStyle")
private external fun _nPopStyle(ptr: NativePointer, textStylePtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphBuilder__1nAddText")
@ModuleImport("org_jetbrains_skia_paragraph_ParagraphBuilder__1nAddText")
private external fun _nAddText(ptr: NativePointer, text: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphBuilder__1nAddPlaceholder")
@ModuleImport("org_jetbrains_skia_paragraph_ParagraphBuilder__1nAddPlaceholder")
private external fun _nAddPlaceholder(
    ptr: NativePointer,
    width: Float,
    height: Float,
    alignment: Int,
    baselineMode: Int,
    baseline: Float
)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphBuilder__1nBuild")
@ModuleImport("org_jetbrains_skia_paragraph_ParagraphBuilder__1nBuild")
private external fun _nBuild(ptr: NativePointer): NativePointer
