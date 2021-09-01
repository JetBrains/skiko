package org.jetbrains.skija.paragraph

import org.jetbrains.skija.impl.Library.Companion.staticLoad
import org.jetbrains.skija.impl.Managed
import org.jetbrains.skija.impl.Native
import org.jetbrains.skija.impl.Stats
import java.lang.ref.Reference

class ParagraphStyle : Managed(_nMake(), _FinalizerHolder.PTR) {
    companion object {
        @JvmStatic external fun _nGetFinalizer(): Long
        @JvmStatic external fun _nMake(): Long
        @JvmStatic external fun _nEquals(ptr: Long, otherPtr: Long): Boolean
        @JvmStatic external fun _nGetStrutStyle(ptr: Long): Long
        @JvmStatic external fun _nSetStrutStyle(ptr: Long, stylePtr: Long)
        @JvmStatic external fun _nGetTextStyle(ptr: Long): Long
        @JvmStatic external fun _nSetTextStyle(ptr: Long, textStylePtr: Long)
        @JvmStatic external fun _nGetDirection(ptr: Long): Int
        @JvmStatic external fun _nSetDirection(ptr: Long, direction: Int)
        @JvmStatic external fun _nGetAlignment(ptr: Long): Int
        @JvmStatic external fun _nSetAlignment(ptr: Long, align: Int)
        @JvmStatic external fun _nGetMaxLinesCount(ptr: Long): Long
        @JvmStatic external fun _nSetMaxLinesCount(ptr: Long, maxLines: Long)
        @JvmStatic external fun _nGetEllipsis(ptr: Long): String
        @JvmStatic external fun _nSetEllipsis(ptr: Long, ellipsis: String?)
        @JvmStatic external fun _nGetHeight(ptr: Long): Float
        @JvmStatic external fun _nSetHeight(ptr: Long, height: Float)
        @JvmStatic external fun _nGetHeightMode(ptr: Long): Int
        @JvmStatic external fun _nSetHeightMode(ptr: Long, v: Int)
        @JvmStatic external fun _nGetEffectiveAlignment(ptr: Long): Int
        @JvmStatic external fun _nIsHintingEnabled(ptr: Long): Boolean
        @JvmStatic external fun _nDisableHinting(ptr: Long)

        init {
            staticLoad()
        }
    }

    override fun _nativeEquals(other: Native?): Boolean {
        return try {
            Stats.onNativeCall()
            _nEquals(_ptr, Native.getPtr(other))
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(other)
        }
    }

    val strutStyle: StrutStyle
        get() = try {
            Stats.onNativeCall()
            StrutStyle(_nGetStrutStyle(_ptr))
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setStrutStyle(s: StrutStyle?): ParagraphStyle {
        return try {
            Stats.onNativeCall()
            _nSetStrutStyle(_ptr, Native.getPtr(s))
            this
        } finally {
            Reference.reachabilityFence(s)
        }
    }

    val textStyle: TextStyle
        get() = try {
            Stats.onNativeCall()
            TextStyle(_nGetTextStyle(_ptr))
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setTextStyle(style: TextStyle?): ParagraphStyle {
        return try {
            Stats.onNativeCall()
            _nSetTextStyle(_ptr, Native.Companion.getPtr(style))
            this
        } finally {
            Reference.reachabilityFence(style)
        }
    }

    val direction: Direction
        get() = try {
            Stats.onNativeCall()
            Direction.values().get(_nGetDirection(_ptr))
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setDirection(style: Direction): ParagraphStyle {
        Stats.onNativeCall()
        _nSetDirection(_ptr, style.ordinal)
        return this
    }

    val alignment: Alignment
        get() = try {
            Stats.onNativeCall()
            Alignment._values.get(_nGetAlignment(_ptr))
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setAlignment(alignment: Alignment): ParagraphStyle {
        Stats.onNativeCall()
        _nSetAlignment(_ptr, alignment.ordinal)
        return this
    }

    val maxLinesCount: Long
        get() = try {
            Stats.onNativeCall()
            _nGetMaxLinesCount(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setMaxLinesCount(count: Long): ParagraphStyle {
        Stats.onNativeCall()
        _nSetMaxLinesCount(_ptr, count)
        return this
    }

    val ellipsis: String
        get() = try {
            Stats.onNativeCall()
            _nGetEllipsis(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setEllipsis(ellipsis: String?): ParagraphStyle {
        Stats.onNativeCall()
        _nSetEllipsis(_ptr, ellipsis)
        return this
    }

    val height: Float
        get() = try {
            Stats.onNativeCall()
            _nGetHeight(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setHeight(height: Float): ParagraphStyle {
        Stats.onNativeCall()
        _nSetHeight(_ptr, height)
        return this
    }

    val heightMode: HeightMode
        get() = try {
            Stats.onNativeCall()
            HeightMode.values().get(_nGetHeightMode(_ptr))
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setHeightMode(behavior: HeightMode): ParagraphStyle {
        Stats.onNativeCall()
        _nSetHeightMode(_ptr, behavior.ordinal)
        return this
    }

    val effectiveAlignment: Alignment
        get() = try {
            Stats.onNativeCall()
            Alignment._values.get(_nGetEffectiveAlignment(_ptr))
        } finally {
            Reference.reachabilityFence(this)
        }
    val isHintingEnabled: Boolean
        get() = try {
            Stats.onNativeCall()
            _nIsHintingEnabled(_ptr)
        } finally {
            Reference.reachabilityFence(this)
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