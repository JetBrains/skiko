package org.jetbrains.skiko

import org.jetbrains.skia.*
import org.jetbrains.skia.tests.makeFromResource
import org.jetbrains.skiko.tests.SkipJsTarget
import org.jetbrains.skiko.tests.SkipNativeTarget
import org.jetbrains.skiko.tests.runTest
import kotlin.test.*

class TypefaceTest {

    @Test
    fun typefaceTest() = runTest {
        val inter = Typeface.makeFromResource("./fonts/Inter-Hinted-Regular.ttf")
        val interV = Typeface.makeFromResource("./fonts/Inter-V.ttf")
        val jbMono = Typeface.makeFromResource("./fonts/JetBrainsMono-Regular.ttf")
        val jbMonoBold = Typeface.makeFromData(Data.makeFromResource("./fonts/JetBrainsMono-Bold.ttf"))

        assertEquals(FontStyle.NORMAL, inter.fontStyle)
        assertFalse(inter.isBold)
        assertFalse(inter.isItalic)
        assertEquals(FontStyle.BOLD, jbMonoBold.fontStyle)

        assertTrue(jbMonoBold.isBold)
        assertFalse(jbMonoBold.isItalic)
        assertFalse(inter.isFixedPitch)
        assertTrue(jbMono.isFixedPitch)

        assertNotEquals(inter.uniqueId, interV.uniqueId)
        assertNotEquals(inter, interV)
        assertNotNull(Typeface.makeDefault())

        assertEquals(394, inter.getUTF32Glyph(83))
        assertEquals(2548, interV.glyphsCount)
        assertEquals(17, inter.tablesCount)

        assertTrue(inter.getTableData("loca")!!.size > 0)
        assertEquals(2816, inter.unitsPerEm)
    }

    @Test
    fun fontVariationTest() = runTest {
        val inter = Typeface.makeFromResource("./fonts/Inter-Hinted-Regular.ttf")
        val interV = Typeface.makeFromResource("./fonts/Inter-V.ttf")
        val jbMono = Typeface.makeFromResource("./fonts/JetBrainsMono-Regular.ttf")

        assertNull(inter.variationAxes)
//        assertNull(inter.variations)
        val axes = arrayOf(
            FontVariationAxis("wght", 100f, 400f, 900f),
            FontVariationAxis("slnt", -10f, 0f, 0f)
        )
        assertContentEquals(axes, interV.variationAxes)
//
//        val inter500: Typeface = interV.makeClone(FontVariation("wght", 500f))
//        assertNotEquals(inter500, interV)
//        assertContentEquals(FontVariation.parse("wght=500 slnt=0"), inter500.variations)
//
//        val Skia = intArrayOf(83, 107, 105, 97)
//        assertContentEquals(shortArrayOf(394, 713, 677, 503), inter.getUTF32Glyphs(Skia))
//        assertContentEquals(shortArrayOf(394, 713, 677, 503), inter.getStringGlyphs("Skia"))
//
//        assertContentEquals(
//            arrayOf(
//                "GDEF",
//                "GPOS",
//                "GSUB",
//                "OS/2",
//                "cmap",
//                "cvt ",
//                "fpgm",
//                "gasp",
//                "glyf",
//                "head",
//                "hhea",
//                "hmtx",
//                "loca",
//                "maxp",
//                "name",
//                "post",
//                "prep"
//            ), inter.tableTags
//        )
//
//
//        assertNull(jbMono.getKerningPairAdjustments(null))
//        assertNull(jbMono.getKerningPairAdjustments(jbMono.getStringGlyphs("TAV")))
//
//        assertContentEquals(arrayOf(FontFamilyName("Inter", "en-US")), interV.familyNames)
//        assertEquals("Inter", interV.familyName)
    }

}