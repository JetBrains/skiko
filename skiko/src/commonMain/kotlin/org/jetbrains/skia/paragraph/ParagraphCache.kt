package org.jetbrains.skia.paragraph

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ModuleImport
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
            _nPrintStatistics(_ptr, NullPointer)
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
            check(getPtr(_owner) != NullPointer) { "ParagraphCache needs owning FontCollection to be alive" }
        } finally {
            reachabilityBarrier(_owner)
        }
    }

    init {
        _owner = owner
    }
}

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphCache__1nAbandon")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_ParagraphCache__1nAbandon")
private external fun _nAbandon(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphCache__1nReset")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_ParagraphCache__1nReset")
private external fun _nReset(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphCache__1nUpdateParagraph")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_ParagraphCache__1nUpdateParagraph")
private external fun _nUpdateParagraph(ptr: NativePointer, paragraphPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphCache__1nFindParagraph")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_ParagraphCache__1nFindParagraph")
private external fun _nFindParagraph(ptr: NativePointer, paragraphPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphCache__1nPrintStatistics")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_ParagraphCache__1nPrintStatistics")
private external fun _nPrintStatistics(ptr: NativePointer, paragraphPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphCache__1nSetEnabled")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_ParagraphCache__1nSetEnabled")
private external fun _nSetEnabled(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphCache__1nGetCount")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_ParagraphCache__1nGetCount")
private external fun _nGetCount(ptr: NativePointer): Int
