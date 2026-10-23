package org.jetbrains.skia.paragraph

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ForceNativeThreadState
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
// Needs ForceNativeThreadState: clears the cache while holding its mutex, and freeing cached runs can drop the last
// reference to a font typeface.
// Native call path: ParagraphCache::abandon -> ParagraphCache::reset -> SkLRUCache::reset
@ForceNativeThreadState
private external fun _nAbandon(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphCache__1nReset")
// Needs ForceNativeThreadState: clears the cache while holding its mutex, and freeing cached runs can drop the last
// reference to a font typeface.
// Native call path: ParagraphCache::reset -> SkLRUCache::reset
@ForceNativeThreadState
private external fun _nReset(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphCache__1nUpdateParagraph")
// Needs ForceNativeThreadState: copies the text and styles into a cache key under a mutex, and inserting can evict old
// runs, which can free a typeface.
// Native call path: ParagraphCache::updateParagraph -> ParagraphCacheKey -> ParagraphCacheValue
@ForceNativeThreadState
private external fun _nUpdateParagraph(ptr: NativePointer, paragraphPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphCache__1nFindParagraph")
// Needs ForceNativeThreadState: copies text and styles into a lookup key under a mutex; clearing and copying the
// paragraph's stored text runs can free a font typeface.
// Native call path: ParagraphCache::findParagraph -> ParagraphCacheValue::updateTo
@ForceNativeThreadState
private external fun _nFindParagraph(ptr: NativePointer, paragraphPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphCache__1nPrintStatistics")
// Needs ForceNativeThreadState: prints statistics to stderr with a blocking write, which can wait on the operating
// system.
// Native call path: ParagraphCache::printStatistics -> SkDebugf -> SkLogVAList -> vfprintf
@ForceNativeThreadState
private external fun _nPrintStatistics(ptr: NativePointer, paragraphPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphCache__1nSetEnabled")
// No ForceNativeThreadState: stores an on or off flag for the cache, so it is short and cannot block.
// Native call path: ParagraphCache::turnOn
private external fun _nSetEnabled(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphCache__1nGetCount")
// No ForceNativeThreadState: returns the stored number of cache entries, so it is short and cannot block.
// Native call path: ParagraphCache::count
private external fun _nGetCount(ptr: NativePointer): Int
