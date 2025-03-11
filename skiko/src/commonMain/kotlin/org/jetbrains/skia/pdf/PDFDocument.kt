package org.jetbrains.skia.pdf

import org.jetbrains.skia.Document
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ModuleImport
import org.jetbrains.skia.WStream
import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Native.Companion.NullPointer

object PDFDocument {

    init {
        staticLoad()
    }

    /**
     * Creates a PDF-backed document, writing the results into a WStream.
     *
     * PDF pages are sized in point units. 1 pt == 1/72 inch == 127/360 mm.
     *
     * @param out A PDF document will be written to this stream. The document may write
     *            to the stream at anytime during its lifetime, until either close() is
     *            called or the document is deleted.
     * @param metadata A PDFMetadata object. Any fields may be left empty.
     * @throws IllegalArgumentException If no PDF document can be created with the supplied arguments.
     */
    fun make(out: WStream, metadata: PDFMetadata = PDFMetadata()): Document {
        Stats.onNativeCall()
        val ptr = try {
            interopScope {
                _nMakeDocument(
                    getPtr(out),
                    toInterop(metadata.title),
                    toInterop(metadata.author),
                    toInterop(metadata.subject),
                    toInterop(metadata.keywords),
                    toInterop(metadata.creator),
                    toInterop(metadata.producer),
                    toInterop(metadata.creation?.asArray()),
                    toInterop(metadata.modified?.asArray()),
                    toInterop(metadata.lang),
                    metadata.rasterDPI,
                    metadata.pdfA,
                    metadata.encodingQuality,
                    metadata.compressionLevel.skiaRepresentation
                )
            }
        } finally {
            reachabilityBarrier(out)
        }
        require(ptr != NullPointer) { "PDF document was created with invalid arguments." }
        return Document(ptr, out)
    }

}

@ExternalSymbolName("org_jetbrains_skia_pdf_PDFDocument__1nMakeDocument")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_pdf_PDFDocument__1nMakeDocument")
private external fun _nMakeDocument(
    wstreamPtr: NativePointer,
    title: InteropPointer,
    author: InteropPointer,
    subject: InteropPointer,
    keywords: InteropPointer,
    creator: InteropPointer,
    producer: InteropPointer,
    creation: InteropPointer,
    modified: InteropPointer,
    lang: InteropPointer,
    rasterDPI: Float,
    pdfA: Boolean,
    encodingQuality: Int,
    compressionLevel: Int
): NativePointer
