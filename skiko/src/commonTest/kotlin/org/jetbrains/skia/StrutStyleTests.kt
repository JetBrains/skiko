package org.jetbrains.skia

import org.jetbrains.skia.impl.use
import org.jetbrains.skia.paragraph.StrutStyle
import kotlin.test.Test
import kotlin.test.assertEquals


class StrutStyleTests {

    @Test
    fun strutStyleTest() {
        StrutStyle().use { strutStyle ->
            assertEquals(FontStyle(400, 5, FontSlant.UPRIGHT), strutStyle.fontStyle)
        }
    }
}