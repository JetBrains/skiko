package org.jetbrains.skia.paragraph

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.ManagedString
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skia.impl.reachabilityBarrier

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
            ParagraphBuilder_nPushStyle(_ptr, getPtr(style))
            this
        } finally {
            reachabilityBarrier(style)
        }
    }

    fun popStyle(): ParagraphBuilder {
        Stats.onNativeCall()
        try {
            ParagraphBuilder_nPopStyle(_ptr, NullPointer)
        } finally {
            reachabilityBarrier(this)
        }
        return this
    }

    fun addText(text: String): ParagraphBuilder {
        Stats.onNativeCall()
        try {
            interopScope {
                ParagraphBuilder_nAddText(_ptr, toInterop(text))
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
            ParagraphBuilder_nAddPlaceholder(
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
            val paragraph = Paragraph(ParagraphBuilder_nBuild(_ptr), _text)
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
        ParagraphBuilder_nMake(getPtr(style), getPtr(fc))
    } finally {
        reachabilityBarrier(style)
        reachabilityBarrier(fc)
    }
}