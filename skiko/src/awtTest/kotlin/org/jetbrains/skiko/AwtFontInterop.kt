package org.jetbrains.skiko

import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.impl.use
import org.jetbrains.skia.paragraph.*
import org.jetbrains.skiko.tests.runTest
import org.junit.Assert.assertTrue
import org.junit.Assume
import org.junit.Test
import java.awt.Font
import java.awt.GraphicsEnvironment
import kotlin.concurrent.thread
import kotlin.system.measureNanoTime

class AwtFontInterop {
    private val fontManager = AwtFontManager()

    private fun assumeOk() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless())
        Assume.assumeTrue(hostOs != OS.Linux)
    }

    @OptIn(DelicateSkikoApi::class)
    @Test
    fun canFindAvailableFont() = fontManager.whenAllFontsCachedBlocking {
        assumeOk()
        val font = Font("Verdana", Font.BOLD, 12)
        val path = fontManager.findAvailableFontFile(font)
        assertTrue("Font must be found", path != null)
        path!!
        assertTrue("Font must be file", path.exists() && path.isFile)
    }

    @Test
    fun canFindFont() {
        runTest {
            assumeOk()
            val font = Font("Verdana", Font.BOLD, 12)
            val path = fontManager.findFontFile(font)
            assertTrue("Font must be found", path != null)
            path!!
            assertTrue("Font must be file", path.exists() && path.isFile)
        }
    }

    @Test
    fun canFindFamily() {
        runTest {
            assumeOk()
            val path = fontManager.findFontFamilyFile("Verdana")
            assertTrue("Font must be found", path != null)
            path!!
            assertTrue("Font must be file", path.exists() && path.isFile)
        }
    }

    @OptIn(DelicateSkikoApi::class)
    @Test
    fun nonExistentFont() = fontManager.whenAllFontsCachedBlocking {
        assumeOk()
        val font = Font("XXXYYY745", Font.BOLD, 12)
        val path = fontManager.findAvailableFontFile(font)
        assertTrue("Font must not be found", path == null)
    }

    @Test
    fun makeSkikoTypeface() {
        runTest {
            assumeOk()
            Assume.assumeFalse(GraphicsEnvironment.isHeadless())
            val font = Font("Verdana", Font.BOLD, 12)
            val skikoTypeface = font.toSkikoTypeface()
            assertTrue("Skiko typeface must work", skikoTypeface != null)
            skikoTypeface!!
            assertTrue("Skiko typeface name is incorrect: ${skikoTypeface.familyName}", skikoTypeface.familyName == "Verdana")
        }
    }

    @Test
    fun listAllFonts() {
        runTest {
            assumeOk()
            val fontFiles = fontManager.listFontFiles()
            assertTrue("There must be fonts", fontFiles.isNotEmpty())
        }
    }

    @Test
    fun addCustomPath() {
        runTest {
            assumeOk()
            val resDir = System.getProperty("skiko.test.font.dir")!!
            fontManager.addCustomPath(resDir)
            fontManager.invalidate()
            val path = fontManager.findFontFamilyFile("JetBrains Mono")
            assertTrue("Custom font must be found", path != null)
            path!!
            assertTrue("Font must be file", path.exists() && path.isFile)
        }
    }

    // This test is disabled due to convoluted setup of tests.
    // @Test
    fun addCustomResource() {
        runTest {
            assumeOk()
            val fontManager = AwtFontManager()
            assertTrue("Custom resource must be found",
                fontManager.addResourceFont("/fonts/JetBrainsMono-Bold.ttf", Library.javaClass.classLoader))
            val path = fontManager.findFontFamilyFile("JetBrains Mono")
            assertTrue("Custom font must be found", path != null)
            path!!
            assertTrue("Font must be file", path.exists() && path.isFile)
        }
    }

//    @Test
    fun `concurrent read access leads to segfault`() {
        val fontCollection = FontCollection().setDefaultFontManager(FontMgr.default)
        val text = ("x".repeat(1000) + "\n").repeat(42)
        val threads = (0..10_000).map {
            thread {
                val para = ParagraphBuilder(ParagraphStyle(), fontCollection).use {
                    it.addText(text)
                    it.build()
                }.layout(Float.POSITIVE_INFINITY)

                val t1 = thread(start = false) {
                    val rects = para.getRectsForRange(2, 8, RectHeightMode.MAX, RectWidthMode.MAX)
                    for (rect in rects) {
                        rect.rect.left
                        rect.rect.right
                        rect.rect.top
                        rect.rect.bottom
                    }
                }
                val t2 = thread(start = false) {
                    val rects = para.getRectsForRange(20, 40, RectHeightMode.MAX, RectWidthMode.MAX)
                    for (rect in rects) {
                        rect.rect.left
                        rect.rect.right
                        rect.rect.top
                        rect.rect.bottom
                    }
                }
                t1.start()
                t2.start()
                t1.join()
                t2.join()
            }
        }
        threads.forEach {
            it.join()
        }
    }

}