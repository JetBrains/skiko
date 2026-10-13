package org.jetbrains.skia.paragraph

import org.jetbrains.skiko.SkikoFeatureFlags

/**
 * Creates a [ParagraphBuilder] for the given [text].
 *
 * When [SkikoFeatureFlags.useBrowserSegmenter] is enabled and the engine supports
 * `Intl.Segmenter`, text segmentation is routed through the browser instead of the ICU data
 * embedded into the Skiko binary. Otherwise the standard ICU based engine is used.
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
    val unicode = if (SkikoFeatureFlags.useBrowserSegmenter && SkUnicodeClient.isSupported) {
        SkUnicodeClient.create(text, locale)
    } else {
        null
    }
    return ParagraphBuilder(style, fontCollection, unicode)
}
