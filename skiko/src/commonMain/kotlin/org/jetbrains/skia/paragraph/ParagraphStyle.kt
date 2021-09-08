@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia.paragraph

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.getPtr
import kotlin.jvm.JvmStatic

class ParagraphStyle : Managed(_nMake(), _FinalizerHolder.PTR) {
    companion object {
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ParagraphStyle__1nGetFinalizer")
        external fun _nGetFinalizer(): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ParagraphStyle__1nMake")
        external fun _nMake(): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ParagraphStyle__1nEquals")
        external fun _nEquals(ptr: NativePointer, otherPtr: NativePointer): Boolean
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ParagraphStyle__1nGetStrutStyle")
        external fun _nGetStrutStyle(ptr: NativePointer): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ParagraphStyle__1nSetStrutStyle")
        external fun _nSetStrutStyle(ptr: NativePointer, stylePtr: NativePointer)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ParagraphStyle__1nGetTextStyle")
        external fun _nGetTextStyle(ptr: NativePointer): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ParagraphStyle__1nSetTextStyle")
        external fun _nSetTextStyle(ptr: NativePointer, textStylePtr: NativePointer)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ParagraphStyle__1nGetDirection")
        external fun _nGetDirection(ptr: NativePointer): Int
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ParagraphStyle__1nSetDirection")
        external fun _nSetDirection(ptr: NativePointer, direction: Int)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ParagraphStyle__1nGetAlignment")
        external fun _nGetAlignment(ptr: NativePointer): Int
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ParagraphStyle__1nSetAlignment")
        external fun _nSetAlignment(ptr: NativePointer, align: Int)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ParagraphStyle__1nGetMaxLinesCount")
        external fun _nGetMaxLinesCount(ptr: NativePointer): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ParagraphStyle__1nSetMaxLinesCount")
        external fun _nSetMaxLinesCount(ptr: NativePointer, maxLines: NativePointer)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ParagraphStyle__1nGetEllipsis")
        external fun _nGetEllipsis(ptr: NativePointer): String
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ParagraphStyle__1nSetEllipsis")
        external fun _nSetEllipsis(ptr: NativePointer, ellipsis: String?)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ParagraphStyle__1nGetHeight")
        external fun _nGetHeight(ptr: NativePointer): Float
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ParagraphStyle__1nSetHeight")
        external fun _nSetHeight(ptr: NativePointer, height: Float)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ParagraphStyle__1nGetHeightMode")
        external fun _nGetHeightMode(ptr: NativePointer): Int
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ParagraphStyle__1nSetHeightMode")
        external fun _nSetHeightMode(ptr: NativePointer, v: Int)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ParagraphStyle__1nGetEffectiveAlignment")
        external fun _nGetEffectiveAlignment(ptr: NativePointer): Int
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ParagraphStyle__1nIsHintingEnabled")
        external fun _nIsHintingEnabled(ptr: NativePointer): Boolean
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ParagraphStyle__1nDisableHinting")
        external fun _nDisableHinting(ptr: NativePointer)

        init {
            staticLoad()
        }
    }

    override fun _nativeEquals(other: Native?): Boolean {
        return try {
            Stats.onNativeCall()
            _nEquals(_ptr, getPtr(other))
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(other)
        }
    }

    var strutStyle: StrutStyle
        get() = try {
            Stats.onNativeCall()
            StrutStyle(_nGetStrutStyle(_ptr))
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setStrutStyle(value)
        }
    
    fun setStrutStyle(s: StrutStyle?): ParagraphStyle {
        return try {
            Stats.onNativeCall()
            _nSetStrutStyle(_ptr, getPtr(s))
            this
        } finally {
            reachabilityBarrier(s)
        }
    }

    var textStyle: TextStyle
        get() = try {
            Stats.onNativeCall()
            TextStyle(_nGetTextStyle(_ptr))
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setTextStyle(value)
        }
    
    fun setTextStyle(style: TextStyle?): ParagraphStyle {
        return try {
            Stats.onNativeCall()
            _nSetTextStyle(_ptr, getPtr(style))
            this
        } finally {
            reachabilityBarrier(style)
        }
    }

    var direction: Direction
        get() = try {
            Stats.onNativeCall()
            Direction.values()[_nGetDirection(_ptr)]
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setDirection(value)
        }

    fun setDirection(style: Direction): ParagraphStyle {
        Stats.onNativeCall()
        _nSetDirection(_ptr, style.ordinal)
        return this
    }

    var alignment: Alignment
        get() = try {
            Stats.onNativeCall()
            Alignment.values()[_nGetAlignment(_ptr)]
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setAlignment(value)
        }

    fun setAlignment(alignment: Alignment): ParagraphStyle {
        Stats.onNativeCall()
        _nSetAlignment(_ptr, alignment.ordinal)
        return this
    }

    var maxLinesCount: NativePointer
        get() = try {
            Stats.onNativeCall()
            _nGetMaxLinesCount(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setMaxLinesCount(value)
        }

    fun setMaxLinesCount(count: NativePointer): ParagraphStyle {
        Stats.onNativeCall()
        _nSetMaxLinesCount(_ptr, count)
        return this
    }

    var ellipsis: String
        get() = try {
            Stats.onNativeCall()
            _nGetEllipsis(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setEllipsis(value)
        }

    fun setEllipsis(ellipsis: String?): ParagraphStyle {
        Stats.onNativeCall()
        _nSetEllipsis(_ptr, ellipsis)
        return this
    }

    var height: Float
        get() = try {
            Stats.onNativeCall()
            _nGetHeight(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setHeight(value)
        }

    fun setHeight(height: Float): ParagraphStyle {
        Stats.onNativeCall()
        _nSetHeight(_ptr, height)
        return this
    }

    var heightMode: HeightMode
        get() = try {
            Stats.onNativeCall()
            HeightMode.values()[_nGetHeightMode(_ptr)]
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setHeightMode(value)
        }

    fun setHeightMode(behavior: HeightMode): ParagraphStyle {
        Stats.onNativeCall()
        _nSetHeightMode(_ptr, behavior.ordinal)
        return this
    }

    val effectiveAlignment: Alignment
        get() = try {
            Stats.onNativeCall()
            Alignment.values()[_nGetEffectiveAlignment(_ptr)]
        } finally {
            reachabilityBarrier(this)
        }

    val isHintingEnabled: Boolean
        get() = try {
            Stats.onNativeCall()
            _nIsHintingEnabled(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    fun disableHinting(): ParagraphStyle {
        Stats.onNativeCall()
        _nDisableHinting(_ptr)
        return this
    }

    internal object _FinalizerHolder {
        val PTR = _nGetFinalizer()
    }

    init {
        Stats.onNativeCall()
    }
}