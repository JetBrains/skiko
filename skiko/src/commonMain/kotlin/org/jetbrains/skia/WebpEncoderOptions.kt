package org.jetbrains.skia

/**
 * Options to encode [Image] to [Data] as WebP format.
 */
class WebpEncoderOptions(
    /**
     * Must be between 0 and 100.
     *
     * If [compression] is [WebpCompression.LOSSLESS], the quality is used as compression effort.
     * Lower value will compress faster into larger image, while higher value will compress into smaller image.
     *
     * If [compression] is [WebpCompression.LOSSY], the quality corresponds to the visual quality for the encoding.
     * Lower value will produce a smaller encoded image.
     */
    val quality: Int = 100,
    /**
     * Whether the encoder use lossy or lossless compression.
     */
    val compression: WebpCompression = WebpCompression.LOSSY,
) {
    init {
        require(quality in 0..100) { "quality must be between 0 and 100" }
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is WebpEncoderOptions) return false
        if (quality != other.quality) return false
        return compression == other.compression
    }

    override fun hashCode(): Int {
        val prime = 59
        var result = 1
        result = result * prime + quality.hashCode()
        result = result * prime + compression.hashCode()
        return result
    }

    override fun toString(): String {
        return "WebpEncoderOptions(quality=$quality, compression=$compression)"
    }
}
