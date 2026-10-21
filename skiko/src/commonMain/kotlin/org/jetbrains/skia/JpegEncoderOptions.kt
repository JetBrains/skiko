package org.jetbrains.skia

/**
 * Options to encode [Image] to [Data] as JPEG format.
 */
class JpegEncoderOptions(
    /**
     * Encoding quality. Must be between 0 and 100.
     */
    val quality: Int = 100,
    /**
     * Downsampling factor for the U and V (chroma) components.
     *
     * This is ignored if the image is [ColorType.GRAY_8], since grayscale images are not encoded as YUV.
     */
    val downSample: JpegDownSample = JpegDownSample.K420,
    /**
     * How to handle input image with alpha.
     *
     * JPEG must be opaque. If [JpegAlphaOption.IGNORE], encoder ignores the alpha channel and
     * treat the image as opaque. If [JpegAlphaOption.BLEND_ON_BLACK], encoder blends pixels
     * into a black background before encoding.
     */
    val alphaOption: JpegAlphaOption = JpegAlphaOption.IGNORE,
    /**
     * Optional XMP metadata.
     */
    val xmpMetadata: Data? = null,
    /**
     * Optional origin to be written as EXIF orientation metadata.
     */
    val origin: EncodedOrigin? = null,
) {
    init {
        require(quality in 0..100) { "quality must be between 0 and 100" }
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is JpegEncoderOptions) return false
        if (quality != other.quality) return false
        if (downSample != other.downSample) return false
        if (alphaOption != other.alphaOption) return false
        if (xmpMetadata != other.xmpMetadata) return false
        return origin == other.origin
    }

    override fun hashCode(): Int {
        val prime = 59
        var result = 1
        result = prime * result + quality.hashCode()
        result = prime * result + downSample.hashCode()
        result = prime * result + alphaOption.hashCode()
        result = prime * result + xmpMetadata.hashCode()
        result = prime * result + origin.hashCode()
        return result
    }

    override fun toString(): String {
        return "JpegEncoderOptions(quality=$quality, downSample=$downSample, alphaOption=$alphaOption, xmpMetadata=$xmpMetadata, origin=$origin)"
    }
}
