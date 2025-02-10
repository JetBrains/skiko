package org.jetbrains.skia.pdf

enum class PDFCompressionLevel(internal val skiaRepresentation: Int) {
    DEFAULT(-1),
    NONE(0),
    LOW_BUT_FAST(1),
    AVERAGE(6),
    HIGH_BUT_SLOW(9);
}
