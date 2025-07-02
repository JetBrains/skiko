package org.jetbrains.skia.paragraph

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.getPtr

class ParagraphCache internal constructor(owner: FontCollection, ptr: NativePointer) : Native(ptr) {
    companion object {
        init {
            staticLoad()
        }
    }

    fun abandon() {
        try {
            _validate()
            Stats.onNativeCall()
            ParagraphCache_nAbandon(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun reset() {
        try {
            _validate()
            Stats.onNativeCall()
            ParagraphCache_nReset(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun updateParagraph(paragraph: Paragraph?): Boolean {
        return try {
            _validate()
            Stats.onNativeCall()
            ParagraphCache_nUpdateParagraph(_ptr, getPtr(paragraph))
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(paragraph)
        }
    }

    fun findParagraph(paragraph: Paragraph?): Boolean {
        return try {
            _validate()
            Stats.onNativeCall()
            ParagraphCache_nFindParagraph(_ptr, getPtr(paragraph))
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(paragraph)
        }
    }

    fun printStatistics() {
        try {
            _validate()
            Stats.onNativeCall()
            ParagraphCache_nPrintStatistics(_ptr, NullPointer)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun setEnabled(value: Boolean) {
        try {
            _validate()
            Stats.onNativeCall()
            ParagraphCache_nSetEnabled(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }
    }

    val count: Int
        get() = try {
            _validate()
            Stats.onNativeCall()
            ParagraphCache_nGetCount(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    internal val _owner: FontCollection
    internal fun _validate() {
        try {
            check(getPtr(_owner) != NullPointer) { "ParagraphCache needs owning FontCollection to be alive" }
        } finally {
            reachabilityBarrier(_owner)
        }
    }

    init {
        _owner = owner
    }
}