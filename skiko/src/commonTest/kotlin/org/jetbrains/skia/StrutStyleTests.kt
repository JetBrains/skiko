package org.jetbrains.skia

import org.jetbrains.skia.impl.use
import org.jetbrains.skia.paragraph.StrutStyle
import org.jetbrains.skiko.tests.SkipJsTarget
import org.jetbrains.skiko.tests.SkipNativeTarget
import kotlin.test.Test
import kotlin.test.assertEquals


class StrutStyleTests {

    @Test
    @SkipJsTarget
    @SkipNativeTarget
    fun strutStyleTest() {
        StrutStyle().use { strutStyle ->
            assertEquals(FontStyle(400, 5, FontSlant.UPRIGHT), strutStyle.fontStyle)
        }
    }
}