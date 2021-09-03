@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia.paragraph

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import kotlin.jvm.JvmStatic

class ParagraphCache internal constructor(owner: FontCollection, ptr: Long) : Native(ptr) {
    companion object {
        @JvmStatic external fun _nAbandon(ptr: Long)
        @JvmStatic external fun _nReset(ptr: Long)
        @JvmStatic external fun _nUpdateParagraph(ptr: Long, paragraphPtr: Long): Boolean
        @JvmStatic external fun _nFindParagraph(ptr: Long, paragraphPtr: Long): Boolean
        @JvmStatic external fun _nPrintStatistics(ptr: Long)
        @JvmStatic external fun _nSetEnabled(ptr: Long, value: Boolean)
        @JvmStatic external fun _nGetCount(ptr: Long): Int

        init {
            staticLoad()
        }
    }

    fun abandon() {
        try {
            _validate()
            Stats.onNativeCall()
            _nAbandon(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun reset() {
        try {
            _validate()
            Stats.onNativeCall()
            _nReset(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun updateParagraph(paragraph: Paragraph?): Boolean {
        return try {
            _validate()
            Stats.onNativeCall()
            _nUpdateParagraph(_ptr, getPtr(paragraph))
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(paragraph)
        }
    }

    fun findParagraph(paragraph: Paragraph?): Boolean {
        return try {
            _validate()
            Stats.onNativeCall()
            _nFindParagraph(_ptr, getPtr(paragraph))
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(paragraph)
        }
    }

    fun printStatistics() {
        try {
            _validate()
            Stats.onNativeCall()
            _nPrintStatistics(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun setEnabled(value: Boolean) {
        try {
            _validate()
            Stats.onNativeCall()
            _nSetEnabled(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }
    }

    val count: Int
        get() = try {
            _validate()
            Stats.onNativeCall()
            _nGetCount(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    internal val _owner: FontCollection
    internal fun _validate() {
        try {
            check(Native.getPtr(_owner) != 0L) { "ParagraphCache needs owning FontCollection to be alive" }
        } finally {
            reachabilityBarrier(_owner)
        }
    }

    init {
        _owner = owner
    }
}