package org.jetbrains.skia.pdf

import org.jetbrains.skia.Color
import org.jetbrains.skia.OutputWStream
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import java.io.ByteArrayOutputStream
import kotlin.test.*

class PDFDocumentTest {

    @Test
    fun makeWithNullMetadata() {
        val metadata = PDFMetadata(producer = null, compressionLevel = PDFCompressionLevel.NONE)
        val baos = ByteArrayOutputStream()
        PDFDocument.make(OutputWStream(baos), metadata).use { doc ->
            assertNotNull(doc.beginPage(100f, 250f), "Canvas is null.")
            doc.endPage()
        }
        val pdf = baos.toString(Charsets.UTF_8)
        assertDoesNotContain(pdf, "/Title")
        assertDoesNotContain(pdf, "/Author")
        assertDoesNotContain(pdf, "/Subject")
        assertDoesNotContain(pdf, "/Keywords")
        assertDoesNotContain(pdf, "/Creator")
        assertDoesNotContain(pdf, "/Producer")
        assertDoesNotContain(pdf, "/CreationDate")
        assertDoesNotContain(pdf, "/ModDate")
        assertDoesNotContain(pdf, "/Lang")
    }

    @Test
    fun makeWithNonNullMetadata() {
        val metadata = PDFMetadata(
            title = "My Novel",
            author = "Johann Wolfgang von Goethe",
            subject = "Literature",
            keywords = "Some,Important,Keywords",
            creator = "Skiko Test Suite",
            producer = "Skia",
            creation = PDFDateTime(2023, 7, 26, 13, 37, 42),
            modified = PDFDateTime(2024, 5, 12, 10, 20, 30, 150),
            lang = "de-DE",
            compressionLevel = PDFCompressionLevel.NONE
        )
        val baos = ByteArrayOutputStream()
        PDFDocument.make(OutputWStream(baos), metadata).use { doc ->
            assertNotNull(doc.beginPage(100f, 250f), "Canvas is null.")
            doc.endPage()
        }
        val pdf = baos.toString(Charsets.UTF_8)
        assertContains(pdf, "/Title (${metadata.title})")
        assertContains(pdf, "/Author (${metadata.author})")
        assertContains(pdf, "/Subject (${metadata.subject})")
        assertContains(pdf, "/Keywords (${metadata.keywords})")
        assertContains(pdf, "/Creator (${metadata.creator})")
        assertContains(pdf, "/Producer (${metadata.producer})")
        assertContains(pdf, "/CreationDate (D:20230726133742+00'00')")
        assertContains(pdf, "/ModDate (D:20240512102030+02'30')")
        assertContains(pdf, "/Lang (${metadata.lang})")
    }

    @Test
    fun draw() {
        val metadata = PDFMetadata(compressionLevel = PDFCompressionLevel.NONE)
        val baos = ByteArrayOutputStream()
        PDFDocument.make(OutputWStream(baos), metadata).use { doc ->
            val canvas = assertNotNull(doc.beginPage(100f, 250f), "Canvas is null.")
            canvas.drawRect(Rect(10f, 20f, 35f, 50f), Paint().apply { color = Color.RED })
            doc.endPage()
        }
        val pdf = baos.toString(Charsets.UTF_8)
        assertContains(pdf, "/MediaBox [0 0 100 250]")
        // Assert that the PDF contains some operations we would expect for our red rect drawing operation.
        assertContains(pdf, "1 0 0 rg")
        assertContains(pdf, "10 20 25 30 re")
    }

    @Test
    fun drawWithContentRect() {
        val metadata = PDFMetadata(compressionLevel = PDFCompressionLevel.NONE)
        val baos = ByteArrayOutputStream()
        PDFDocument.make(OutputWStream(baos), metadata).use { doc ->
            val canvas = assertNotNull(doc.beginPage(100f, 250f, Rect(60f, 40f, 90f, 220f)), "Canvas is null.")
            canvas.drawRect(Rect(10f, 20f, 35f, 50f), Paint().apply { color = Color.RED })
            doc.endPage()
        }
        val pdf = baos.toString(Charsets.UTF_8)
        // Assert that the PDF contains the content rect somewhere.
        assertContains(pdf, "60 40 30 180 re")
    }

    @Test
    fun beginInvalidPage() {
        val doc = PDFDocument.make(OutputWStream(ByteArrayOutputStream()))
        assertFailsWith<IllegalArgumentException> {
            doc.beginPage(-10f, -20f)
        }
    }

    private fun assertDoesNotContain(charSequence: CharSequence, other: CharSequence) {
        assertTrue(
            other !in charSequence,
            "Expected the char sequence to not contain the substring.\n" +
                    "CharSequence <$charSequence>, substring <$other>."
        )
    }

}
