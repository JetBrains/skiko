package org.jetbrains.skia.paragraph

import org.jetbrains.skia.Typeface
import org.jetbrains.skia.impl.use
import org.jetbrains.skia.tests.assertCloseEnough
import org.jetbrains.skia.tests.makeFromResource
import org.jetbrains.skiko.tests.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParagraphBuilderTest {
    @Test
    fun placeholderDimensionsArePreservedInLayout() = runTest {
        Typeface.makeFromResource("./fonts/JetBrainsMono-Regular.ttf").use { typeface ->
            TypefaceFontProvider().use { manager ->
                manager.registerTypeface(typeface, "Placeholder Mono")
                FontCollection().use { collection ->
                    collection.setDefaultFontManager(manager, "Placeholder Mono")
                    ParagraphStyle().use { style ->
                        ParagraphBuilder(style, collection).use { builder ->
                            builder.addText("a")
                            builder.addPlaceholder(
                                PlaceholderStyle(30f, 20f, PlaceholderAlignment.BASELINE, BaselineMode.ALPHABETIC, 15f)
                            )
                            builder.addText("b")
                            builder.build()
                        }.use { paragraph ->
                            paragraph.layout(300f)
                            val boxes = paragraph.rectsForPlaceholders
                            assertEquals(1, boxes.size)
                            assertCloseEnough(30f, boxes[0].rect.width)
                            assertCloseEnough(20f, boxes[0].rect.height)
                            assertTrue(boxes[0].rect.left > 0f)
                            assertEquals(Direction.LTR, boxes[0].direction)
                        }
                    }
                }
            }
        }
    }
}
