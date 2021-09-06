@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia.paragraph

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.ManagedString
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.ExternalSymbolName
import kotlin.jvm.JvmStatic

class ParagraphBuilder(style: ParagraphStyle?, fc: FontCollection?) :
    Managed(_nMake(getPtr(style), getPtr(fc)), _FinalizerHolder.PTR) {
    companion object {
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ParagraphBuilder__1nMake")
        external fun _nMake(paragraphStylePtr: Long, fontCollectionPtr: Long): Long
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ParagraphBuilder__1nGetFinalizer")
        external fun _nGetFinalizer(): Long
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ParagraphBuilder__1nPushStyle")
        external fun _nPushStyle(ptr: Long, textStylePtr: Long)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ParagraphBuilder__1nPopStyle")
        external fun _nPopStyle(ptr: Long)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ParagraphBuilder__1nAddText")
        external fun _nAddText(ptr: Long, text: String?)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ParagraphBuilder__1nAddPlaceholder")
        external fun _nAddPlaceholder(
            ptr: Long,
            width: Float,
            height: Float,
            alignment: Int,
            baselineMode: Int,
            baseline: Float
        )

        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ParagraphBuilder__1nSetParagraphStyle")
        external fun _nSetParagraphStyle(ptr: Long, stylePtr: Long)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ParagraphBuilder__1nBuild")
        external fun _nBuild(ptr: Long): Long

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
        _nAddText(_ptr, text)
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

    fun setParagraphStyle(style: ParagraphStyle?): ParagraphBuilder {
        return try {
            Stats.onNativeCall()
            _nSetParagraphStyle(_ptr, getPtr(style))
            this
        } finally {
            reachabilityBarrier(style)
        }
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
        val PTR = _nGetFinalizer()
    }

    init {
        Stats.onNativeCall()
        reachabilityBarrier(style)
        reachabilityBarrier(fc)
    }
}