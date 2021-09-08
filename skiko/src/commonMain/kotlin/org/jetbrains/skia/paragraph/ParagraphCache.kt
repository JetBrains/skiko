@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia.paragraph

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.NULLPNTR
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.getPtr
import kotlin.jvm.JvmStatic

class ParagraphCache internal constructor(owner: FontCollection, ptr: NativePointer) : Native(ptr) {
    companion object {
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ParagraphCache__1nAbandon")
        external fun _nAbandon(ptr: NativePointer)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ParagraphCache__1nReset")
        external fun _nReset(ptr: NativePointer)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ParagraphCache__1nUpdateParagraph")
        external fun _nUpdateParagraph(ptr: NativePointer, paragraphPtr: NativePointer): Boolean
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ParagraphCache__1nFindParagraph")
        external fun _nFindParagraph(ptr: NativePointer, paragraphPtr: NativePointer): Boolean
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ParagraphCache__1nPrintStatistics")
        external fun _nPrintStatistics(ptr: NativePointer)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ParagraphCache__1nSetEnabled")
        external fun _nSetEnabled(ptr: NativePointer, value: Boolean)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ParagraphCache__1nGetCount")
        external fun _nGetCount(ptr: NativePointer): Int

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
            check(getPtr(_owner) != NULLPNTR) { "ParagraphCache needs owning FontCollection to be alive" }
        } finally {
            reachabilityBarrier(_owner)
        }
    }

    init {
        _owner = owner
    }
}