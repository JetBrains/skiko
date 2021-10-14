package org.jetbrains.skiko

import org.jetbrains.skia.Data
import org.jetbrains.skia.FontFamilyName
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.FontVariation
import org.jetbrains.skia.FontVariationAxis
import org.jetbrains.skia.Typeface
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.jetbrains.skia.tests.makeFromResource

class TypefaceTest {

    @Test
    fun typefaceTest() {
        val inter = Typeface.makeFromResource("InterHinted-Regular.ttf")
        val interV = Typeface.makeFromResource("Inter-V.ttf")
        val jbMono = Typeface.makeFromResource("JetBrainsMono-Regular.ttf")
        val jbMonoBold = Typeface.makeFromData(Data.makeFromResource("JetBrainsMono-Bold.ttf"))

        assertEquals(FontStyle.NORMAL, inter.fontStyle)
        assertFalse(inter.isBold)
        assertFalse(inter.isItalic)
        assertEquals(FontStyle.BOLD, jbMonoBold.fontStyle)

        assertTrue(jbMonoBold.isBold)
        assertFalse(jbMonoBold.isItalic)
        assertFalse(inter.isFixedPitch)
        assertTrue(jbMono.isFixedPitch)
        assertNull(inter.variationAxes)
        assertNull(inter.variations)
        val axes = arrayOf(
            FontVariationAxis("wght", 100f, 400f, 900f),
            FontVariationAxis("slnt", -10f, 0f, 0f)
        )
        assertContentEquals(axes, interV.variationAxes)

        val inter500: Typeface = interV.makeClone(FontVariation("wght", 500f))
        assertNotEquals(inter500, interV)
        assertContentEquals(FontVariation.parse("wght=500 slnt=0"), inter500.variations)

        assertNotEquals(inter.uniqueId, interV.uniqueId)
        assertNotEquals(inter, interV)
        assertNotNull(Typeface.makeDefault())


        val Skia = intArrayOf(83, 107, 105, 97)
        assertContentEquals(shortArrayOf(394, 713, 677, 503), inter.getUTF32Glyphs(Skia))
        assertContentEquals(shortArrayOf(394, 713, 677, 503), inter.getStringGlyphs("Skia"))
        assertEquals(394, inter.getUTF32Glyph(83))
        assertEquals(2548, interV.glyphsCount)
        assertEquals(17, inter.tablesCount)

        assertContentEquals(
            arrayOf(
                "GDEF",
                "GPOS",
                "GSUB",
                "OS/2",
                "cmap",
                "cvt ",
                "fpgm",
                "gasp",
                "glyf",
                "head",
                "hhea",
                "hmtx",
                "loca",
                "maxp",
                "name",
                "post",
                "prep"
            ), inter.tableTags
        )

        assertTrue(inter.getTableData("loca")!!.size > 0)
        assertEquals(2816, inter.unitsPerEm)

        assertNull(jbMono.getKerningPairAdjustments(null))
        assertNull(jbMono.getKerningPairAdjustments(jbMono.getStringGlyphs("TAV")))

        assertContentEquals(arrayOf(FontFamilyName("Inter", "en-US")), interV.familyNames)
        assertEquals("Inter", interV.familyName)
    }
}