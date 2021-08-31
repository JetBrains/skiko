package org.jetbrains.skija.paragraph

import org.jetbrains.skija.impl.Library.Companion.staticLoad
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.skija.impl.Native
import org.jetbrains.skija.impl.Stats
import java.lang.ref.Reference

class ParagraphCache @ApiStatus.Internal constructor(owner: FontCollection, ptr: Long) : Native(ptr) {
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
            Reference.reachabilityFence(this)
        }
    }

    fun reset() {
        try {
            _validate()
            Stats.onNativeCall()
            _nReset(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun updateParagraph(paragraph: Paragraph?): Boolean {
        return try {
            _validate()
            Stats.onNativeCall()
            _nUpdateParagraph(_ptr, Native.getPtr(paragraph))
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(paragraph)
        }
    }

    fun findParagraph(paragraph: Paragraph?): Boolean {
        return try {
            _validate()
            Stats.onNativeCall()
            _nFindParagraph(_ptr, Native.Companion.getPtr(paragraph))
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(paragraph)
        }
    }

    fun printStatistics() {
        try {
            _validate()
            Stats.onNativeCall()
            _nPrintStatistics(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun setEnabled(value: Boolean) {
        try {
            _validate()
            Stats.onNativeCall()
            _nSetEnabled(_ptr, value)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    val count: Int
        get() = try {
            _validate()
            Stats.onNativeCall()
            _nGetCount(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    @ApiStatus.Internal
    val _owner: FontCollection
    @ApiStatus.Internal
    fun _validate() {
        try {
            check(Native.Companion.getPtr(_owner) != 0L) { "ParagraphCache needs owning FontCollection to be alive" }
        } finally {
            Reference.reachabilityFence(_owner)
        }
    }

    init {
        _owner = owner
    }
}