package org.jetbrains.skia

import org.jetbrains.skia.impl.use
import kotlin.test.Test
import kotlin.test.assertNotNull

class FontMgrWebTest {
    @Test
    fun matchFamilyStyleRobotoReturnsDefaultEmbeddedFont() {
        val typeface = FontMgr.default.matchFamilyStyle("Roboto", FontStyle.NORMAL)
        assertNotNull(typeface, "Expected default embedded 'Roboto' font on web")
        typeface.close()
    }
}
