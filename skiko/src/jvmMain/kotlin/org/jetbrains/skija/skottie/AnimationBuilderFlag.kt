package org.jetbrains.skija.skottie


enum class AnimationBuilderFlag(internal val _flag: Int) {
    /**
     * Normally, all static image frames are resolved at
     * load time via ImageAsset::getFrame(0).  With this flag,
     * frames are only resolved when needed, at seek() time.
     */
    DEFER_IMAGE_LOADING(0x01),

    /**
     * Attempt to use the embedded fonts (glyph paths,
     * normally used as fallback) over native Skia typefaces.
     */
    PREFER_EMBEDDED_FONTS(0x02);
}