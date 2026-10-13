package org.jetbrains.skia.paragraph

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.Library.Companion.staticLoad

/**
 * Describes the Unicode support compiled into the current Skiko binary.
 *
 * Skiko for the web can be built without ICU (`-Pskiko.wasm.useIcu=false`). Such a binary is
 * significantly smaller, but it has no Unicode Character Database and no ICU break iterators:
 * text segmentation has to come from the browser (`Intl.Segmenter`, see [SkUnicodeClient]) and
 * BiDi resolution is done by Skiko's own embedded UAX #9 implementation.
 */
object SkikoUnicodeBackend {
    init {
        staticLoad()
    }

    /**
     * `true` when ICU is linked into this Skiko binary.
     *
     * When `false`:
     * - `org.jetbrains.skia.BreakIterator.make*Instance()` throws [UnsupportedOperationException];
     * - `ParagraphBuilder` requires an explicitly provided [SkUnicode], which
     *   [ParagraphBuilder.Companion.createForText] does automatically;
     * - `org.jetbrains.skia.icu.CharProperties` returns approximated values only.
     */
    val isIcuAvailable: Boolean by lazy { Unicode_nIsIcuAvailable() }
}

@ExternalSymbolName("org_jetbrains_skia_icu_Unicode__1nIsIcuAvailable")
private external fun Unicode_nIsIcuAvailable(): Boolean
