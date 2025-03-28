package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad

/**
 * High-level API for creating a document-based canvas. To use:
 *
 * 1. Create a document, e.g., via `PDFDocument.make(...)`.
 * 2. For each page of content:
 *    ```
 *    canvas = doc.beginPage(...)
 *    drawMyContent(canvas)
 *    doc.endPage()
 *    ```
 * 3. Close the document with `doc.close()`.
 */
class Document internal constructor(ptr: NativePointer, internal val _owner: Any) : RefCnt(ptr) {

    companion object {
        init {
            staticLoad()
        }
    }

    /**
     * Begins a new page for the document, returning the canvas that will draw
     * into the page. The document owns this canvas, and it will go out of
     * scope when endPage() or close() is called, or the document is deleted.
     * This will call endPage() if there is a currently active page.
     *
     * @throws IllegalArgumentException If no page can be created with the supplied arguments.
     */
    fun beginPage(width: Float, height: Float, content: Rect? = null): Canvas {
        Stats.onNativeCall()
        try {
            val ptr = interopScope {
                _nBeginPage(_ptr, width, height, toInterop(content?.serializeToFloatArray()))
            }
            require(ptr != NullPointer) { "Document page was created with invalid arguments." }
            return Canvas(ptr, false, this)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Call endPage() when the content for the current page has been drawn
     * (into the canvas returned by beginPage()). After this call the canvas
     * returned by beginPage() will be out-of-scope.
     */
    fun endPage() {
        Stats.onNativeCall()
        try {
            _nEndPage(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Call close() when all pages have been drawn. This will close the file
     * or stream holding the document's contents. After close() the document
     * can no longer add new pages. Deleting the document will automatically
     * call close() if need be.
     */
    override fun close() {
        // Deleting the document (which super.close() does) will automatically invoke SkDocument::close.
        super.close()
    }

}

@ExternalSymbolName("org_jetbrains_skia_Document__1nBeginPage")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Document__1nBeginPage")
private external fun _nBeginPage(
    ptr: NativePointer, width: Float, height: Float, content: InteropPointer
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Document__1nEndPage")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Document__1nEndPage")
private external fun _nEndPage(ptr: NativePointer)
