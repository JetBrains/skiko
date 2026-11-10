package org.jetbrains.skia.paragraph

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.interopScope

/**
 * An [SkUnicode] backed by Skia's `SkUnicode_client`, fed with text segmentation boundaries
 * computed by the browser-native `Intl.Segmenter`.
 *
 * BiDi resolution (UAX #9) and character property lookups are served by the native side:
 * by the embedded ICU when it is available, and by Skiko's own UAX #9 implementation and
 * Skia's hardcoded character tables in an ICU-less binary
 * (see [SkikoUnicodeBackend.isIcuAvailable]).
 */
class SkUnicodeClient private constructor(ptr: NativePointer) : SkUnicode(ptr, _FinalizerHolder.PTR) {
    companion object {
        init {
            staticLoad()
        }

        /**
         * `true` when the current engine is able to provide text segmentation,
         * i.e. when [create] can be used.
         */
        val isSupported: Boolean
            get() = isIntlSegmenterSupported()

        /**
         * Creates an [SkUnicode] for [text] using the browser text segmentation.
         *
         * The returned instance is bound to [text] and must not be reused for another text.
         */
        fun create(text: String, locale: String? = null): SkUnicode {
            val provider = WebBreakIteratorProvider(locale)
            val graphemes = provider.computeGraphemeBreaks(text)
            val words = provider.computeWordBreaks(text)
            val lineBreaks = provider.computeLineBreaks(text)

            // Intl.Segmenter reports UTF-16 code unit offsets, so the text is passed as
            // UTF-16 code units to keep the indices aligned on the native side.
            val utf16 = ShortArray(text.length) { text[it].code.toShort() }

            Stats.onNativeCall()
            val ptr = interopScope {
                _nCreate(
                    toInterop(utf16), text.length,
                    toInterop(graphemes), graphemes.size,
                    toInterop(words), words.size,
                    toInterop(lineBreaks), lineBreaks.size
                )
            }
            require(ptr != 0) { "Failed to create SkUnicodeClient" }
            return SkUnicodeClient(ptr)
        }
    }

    internal object _FinalizerHolder {
        val PTR = SkUnicodeClient_nGetFinalizer()
    }
}

@ExternalSymbolName("org_jetbrains_skia_paragraph_SkUnicodeClient__1nGetFinalizer")
private external fun SkUnicodeClient_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_SkUnicodeClient__1nCreate")
private external fun _nCreate(
    utf16Text: InteropPointer,
    textLength: Int,
    graphemes: InteropPointer,
    graphemeCount: Int,
    words: InteropPointer,
    wordCount: Int,
    lineBreaks: InteropPointer,
    lineBreakCount: Int
): NativePointer
