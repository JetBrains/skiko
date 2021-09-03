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

    var strutStyle: StrutStyle
        get() = try {
            Stats.onNativeCall()
            StrutStyle(_nGetStrutStyle(_ptr))
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            try {
                Stats.onNativeCall()
                _nSetStrutStyle(_ptr, getPtr(value))
            } finally {
                reachabilityBarrier(value)
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
            try {
                Stats.onNativeCall()
                _nSetTextStyle(_ptr, getPtr(value))
            } finally {
                reachabilityBarrier(value)
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
            Stats.onNativeCall()
            _nSetDirection(_ptr, value.ordinal)
        }

    var alignment: Alignment
        get() = try {
            Stats.onNativeCall()
            Alignment.values()[_nGetAlignment(_ptr)]
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            Stats.onNativeCall()
            _nSetAlignment(_ptr, value.ordinal)
        }

    var maxLinesCount: Long
        get() = try {
            Stats.onNativeCall()
            _nGetMaxLinesCount(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            Stats.onNativeCall()
            _nSetMaxLinesCount(_ptr, value)
        }

    var ellipsis: String
        get() = try {
            Stats.onNativeCall()
            _nGetEllipsis(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            Stats.onNativeCall()
            _nSetEllipsis(_ptr, value)
        }

    var height: Float
        get() = try {
            Stats.onNativeCall()
            _nGetHeight(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            Stats.onNativeCall()
            _nSetHeight(_ptr, value)
        }

    var heightMode: HeightMode
        get() = try {
            Stats.onNativeCall()
            HeightMode.values()[_nGetHeightMode(_ptr)]
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            Stats.onNativeCall()
            _nSetHeightMode(_ptr, value.ordinal)
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