package org.jetbrains.skija.paragraph

import org.jetbrains.skija.impl.Library.Companion.staticLoad
import org.jetbrains.skija.ManagedString
import org.jetbrains.skija.impl.Managed
import org.jetbrains.skija.impl.Native
import org.jetbrains.skija.impl.Stats
import java.lang.ref.Reference

class ParagraphBuilder(style: ParagraphStyle?, fc: FontCollection?) :
    Managed(_nMake(Native.getPtr(style), Native.getPtr(fc)), _FinalizerHolder.PTR) {
    companion object {
        @JvmStatic external fun _nMake(paragraphStylePtr: Long, fontCollectionPtr: Long): Long
        @JvmStatic external fun _nGetFinalizer(): Long
        @JvmStatic external fun _nPushStyle(ptr: Long, textStylePtr: Long)
        @JvmStatic external fun _nPopStyle(ptr: Long)
        @JvmStatic external fun _nAddText(ptr: Long, text: String?)
        @JvmStatic external fun _nAddPlaceholder(
            ptr: Long,
            width: Float,
            height: Float,
            alignment: Int,
            baselineMode: Int,
            baseline: Float
        )

        @JvmStatic external fun _nSetParagraphStyle(ptr: Long, stylePtr: Long)
        @JvmStatic external fun _nBuild(ptr: Long): Long

        init {
            staticLoad()
        }
    }

    private var _text: ManagedString? = null
    fun pushStyle(style: TextStyle?): ParagraphBuilder {
        return try {
            Stats.onNativeCall()
            _nPushStyle(_ptr, Native.Companion.getPtr(style))
            this
        } finally {
            Reference.reachabilityFence(style)
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
            _nSetParagraphStyle(_ptr, Native.getPtr(style))
            this
        } finally {
            Reference.reachabilityFence(style)
        }
    }

    fun build(): Paragraph {
        return try {
            Stats.onNativeCall()
            val paragraph = Paragraph(_nBuild(_ptr), _text)
            _text = null
            paragraph
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    internal object _FinalizerHolder {
        val PTR = _nGetFinalizer()
    }

    init {
        Stats.onNativeCall()
        Reference.reachabilityFence(style)
        Reference.reachabilityFence(fc)
    }
}