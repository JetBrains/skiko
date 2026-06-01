package org.jetbrains.skia

import org.jetbrains.skia.impl.use
import org.jetbrains.skia.paragraph.StrutStyle
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals


class StrutStyleTests {

    @Test
    fun strutStyleTest() {
        StrutStyle().use { strutStyle ->
            assertEquals(FontStyle(FontWeight(400), FontWidth(5), FontSlant.UPRIGHT), strutStyle.fontStyle)

            strutStyle.fontStyle = FontStyle(FontWeight(300), FontWidth(4), FontSlant.ITALIC)

            assertEquals(FontStyle(FontWeight(300), FontWidth(4), FontSlant.ITALIC), strutStyle.fontStyle)


            strutStyle.setFontFamilies(arrayOf("MonacoShmonaco"))
            assertContentEquals(arrayOf("MonacoShmonaco"), strutStyle.fontFamilies)
        }
    }
}