package org.jetbrains.skia.pdf

/**
 * Optional metadata to be passed into the PDF factory function.
 *
 * @property title The document's title.
 * @property author The name of the person who created the document.
 * @property subject The subject of the document.
 * @property keywords Keywords associated with the document.
 *                    Commas may be used to delineate keywords within the string.
 * @property creator If the document was converted to PDF from another format,
 *                   the name of the conforming product that created the
 *                   original document from which it was converted.
 * @property producer The product that is converting this document to PDF.
 * @property creation The date and time the document was created.
 *                    The zero default value represents an unknown/unset time.
 * @property modified The date and time the document was most recently modified.
 *                    The zero default value represents an unknown/unset time.
 * @property lang The natural language of the text in the PDF.
 * @property rasterDPI The DPI (pixels-per-inch) at which features without native PDF support
 *                     will be rasterized (e.g. draw image with perspective, draw text with
 *                     perspective, ...). A larger DPI would create a PDF that reflects the
 *                     original intent with better fidelity, but it can make for larger PDF
 *                     files too, which would use more memory while rendering, and it would be
 *                     slower to be processed or sent online or to printer.
 * @property pdfA If true, include XMP metadata, a document UUID, and sRGB output intent
 *                information. This adds length to the document and makes it
 *                non-reproducible, but are necessary features for PDF/A-2b conformance
 * @property encodingQuality Encoding quality controls the trade-off between size and quality. By
 *                           default this is set to 101 percent, which corresponds to lossless
 *                           encoding. If this value is set to a value <= 100, and the image is
 *                           opaque, it will be encoded (using JPEG) with that quality setting.
 * @property compressionLevel PDF streams may be compressed to save space.
 *                            Use this to specify the desired compression vs time tradeoff.
 */
data class PDFMetadata(
    val title: String? = null,
    val author: String? = null,
    val subject: String? = null,
    val keywords: String? = null,
    val creator: String? = null,
    val producer: String? = "Skia/PDF",
    val creation: PDFDateTime? = null,
    val modified: PDFDateTime? = null,
    val lang: String? = null,
    val rasterDPI: Float = 72f,
    val pdfA: Boolean = false,
    val encodingQuality: Int = 101,
    val compressionLevel: PDFCompressionLevel = PDFCompressionLevel.DEFAULT
)
