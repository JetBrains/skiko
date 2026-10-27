package org.jetbrains.skia.encode

/**
 * Options to encode [Image][org.jetbrains.skia.Image] to [Data][org.jetbrains.skia.Data] as PNG format.
 */
class PngEncoderOptions(
    /**
     * Must be between 0 and 9.
     */
    val compressionLevel: Int = 6,
    /**
     * Filtering strategies to use.
     *
     * If a single filter is chosen, encoder will use that filter for every row.
     *
     * If multiple filters are chosen, encoder will use a heuristic to guess which filter will encode smallest,
     * then apply that filter. This happens on a per row basis, different rows can use different filters.
     *
     * Using a single filter (or less filters) is typically faster. Trying all of the filters may help
     * minimize the output size.
     */
    val filterFlags: PngFilterFlag = PngFilterFlag.ALL,
    /**
     * Comments to be written in the tEXt chunks of the PNG, as keyword to text pairs.
     *
     * Each keyword must be between 1 and 79 bytes in UTF-8.
     *
     * Note that tEXt chunks are defined as Latin-1, so non Latin-1 characters may not be displayed
     * correctly by other applications.
     */
    val comments: Map<String, String> = emptyMap(),
) {
    init {
        require(compressionLevel in 0..9) { "compressionLevel must be between 0 and 9" }
        require(comments.keys.all { it.encodeToByteArray().size in 1..79 }) {
            "comment keyword must be between 1 and 79 bytes"
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is PngEncoderOptions) return false
        if (compressionLevel != other.compressionLevel) return false
        if (filterFlags != other.filterFlags) return false
        return comments == other.comments
    }

    override fun hashCode(): Int {
        val prime = 59
        var result = 1
        result = prime * result + compressionLevel.hashCode()
        result = prime * result + filterFlags.hashCode()
        result = prime * result + comments.hashCode()
        return result
    }

    override fun toString(): String {
        return "PngEncoderOptions(compressionLevel=$compressionLevel, filterFlags=$filterFlags, comments=$comments)"
    }
}
