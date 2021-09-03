@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia.paragraph

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import kotlin.jvm.JvmStatic

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
            _nEquals(_ptr, getPtr(other))
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(other)
        }
    }

    val strutStyle: StrutStyle
        get() = try {
            Stats.onNativeCall()
            StrutStyle(_nGetStrutStyle(_ptr))
        } finally {
            reachabilityBarrier(this)
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

    val textStyle: TextStyle
        get() = try {
            Stats.onNativeCall()
            TextStyle(_nGetTextStyle(_ptr))
        } finally {
            reachabilityBarrier(this)
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

    val direction: Direction
        get() = try {
            Stats.onNativeCall()
            Direction.values()[_nGetDirection(_ptr)]
        } finally {
            reachabilityBarrier(this)
        }

    fun setDirection(style: Direction): ParagraphStyle {
        Stats.onNativeCall()
        _nSetDirection(_ptr, style.ordinal)
        return this
    }

    val alignment: Alignment
        get() = try {
            Stats.onNativeCall()
            Alignment.values()[_nGetAlignment(_ptr)]
        } finally {
            reachabilityBarrier(this)
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
            reachabilityBarrier(this)
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
            reachabilityBarrier(this)
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
            reachabilityBarrier(this)
        }

    fun setHeight(height: Float): ParagraphStyle {
        Stats.onNativeCall()
        _nSetHeight(_ptr, height)
        return this
    }

    val heightMode: HeightMode
        get() = try {
            Stats.onNativeCall()
            HeightMode.values()[_nGetHeightMode(_ptr)]
        } finally {
            reachabilityBarrier(this)
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