package org.jetbrains.skia.paragraph

import org.jetbrains.skiko.SkikoFeatureFlags

/**
 * Creates a [ParagraphBuilder] for the given [text].
 *
 * Text segmentation is routed through the browser-native `Intl.Segmenter` instead of the ICU
 * data embedded into the Skiko binary when either
 * - [SkikoFeatureFlags.useBrowserSegmenter] is enabled, or
 * - the binary was built without ICU (see [SkikoUnicodeBackend.isIcuAvailable]), in which case
 *   this is the only supported path,
 *
 * and the engine supports `Intl.Segmenter`. Otherwise the standard ICU based engine is used.
 *
 * The [text] is not added to the builder, it is only used to pre-compute the segmentation,
 * so it has to be passed to [ParagraphBuilder.addText] as usual.
 */
fun ParagraphBuilder.Companion.createForText(
    style: ParagraphStyle?,
    fontCollection: FontCollection?,
    text: String,
    locale: String? = null
): ParagraphBuilder {
    val icuAvailable = SkikoUnicodeBackend.isIcuAvailable
    val useClient = SkikoFeatureFlags.useBrowserSegmenter || !icuAvailable
    val unicode = if (useClient && SkUnicodeClient.isSupported) {
        SkUnicodeClient.create(text, locale)
    } else {
        check(icuAvailable) {
            "ICU is excluded from this Skiko binary and `Intl.Segmenter` is not supported by " +
                "this engine, so text cannot be laid out."
        }
        null
    }
    return ParagraphBuilder(style, fontCollection, unicode)
}
