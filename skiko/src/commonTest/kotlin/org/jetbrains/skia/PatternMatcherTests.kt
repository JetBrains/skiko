package org.jetbrains.skia

import org.jetbrains.skiko.tests.runTest
import kotlin.test.*

class PatternMatcherTests {

    @Test
    fun canSplit() = runTest {
        val _splitPattern = compilePattern("\\s+")

        val result = _splitPattern.split("a b   cd 1   23 -7.0  4   5")!!

        assertContentEquals(
            expected = arrayOf("a", "b", "cd", "1", "23", "-7.0", "4", "5"),
            actual = result
        )
    }

    @Test
    fun matchesTrue() = runTest {
        val pattern = compilePattern("[0-9]{3}-[a-z]{3}")

        val result = pattern.matcher("123-abc")
        assertTrue(result.matches())
    }

    @Test
    fun matchesFalse() = runTest {
        val pattern = compilePattern("[0-9]{3}-[a-z]{3}")

        val result = pattern.matcher("1c23-abc")
        assertFalse(result.matches())
    }

    @Test
    fun findGroup() = runTest {
        val pattern = compilePattern("([-+])?([a-z0-9]{4})(?:\\[(\\d+)?:(\\d+)?\\])?(?:=(\\d+))?")

        val matcher1 = pattern.matcher("+abcd[1234:567891234]=10101010101")
        assertTrue(matcher1.matches())

        assertEquals("+", matcher1.group(1))
        assertEquals("abcd", matcher1.group(2))
        assertEquals("1234", matcher1.group(3))
        assertEquals("567891234", matcher1.group(4))
        assertEquals("10101010101", matcher1.group(5))

        val matcher2 = pattern.matcher("abcd[:567891234]=10101010101")
        assertTrue(matcher2.matches())

        assertEquals(null, matcher2.group(1))
        assertEquals("abcd", matcher2.group(2))
        assertEquals(null, matcher2.group(3))
        assertEquals("567891234", matcher2.group(4))
        assertEquals("10101010101", matcher2.group(5))
    }

    @Test
    fun fontFeatureCanBeParsed() = runTest {
        val f1 = FontFeature.parseOne("abcd[5:15]=42")
        assertEquals("abcd", f1.tag)
        assertEquals(5u, f1.start)
        assertEquals(15u, f1.end)
        assertEquals(42, f1.value)

        val f2 = FontFeature.parseOne("abcd[:15]=42")
        assertEquals("abcd", f2.tag)
        assertEquals(FontFeature.GLOBAL_START, f2.start)
        assertEquals(15u, f2.end)
        assertEquals(42, f2.value)

        val f3 = FontFeature.parseOne("abcd=42")
        assertEquals("abcd", f3.tag)
        assertEquals(FontFeature.GLOBAL_START, f3.start)
        assertEquals(FontFeature.GLOBAL_END, f3.end)
        assertEquals(42, f3.value)

        val f4 = FontFeature.parseOne("abcd[:]=42")
        assertEquals("abcd", f4.tag)
        assertEquals(FontFeature.GLOBAL_START, f4.start)
        assertEquals(FontFeature.GLOBAL_END, f4.end)
        assertEquals(42, f4.value)
    }

    @Test
    fun fontVariationCanBeParsed() = runTest {
        val f1 = FontVariation.parseOne("abcd=111")

        assertEquals("abcd", f1.tag)
        assertEquals(111f, f1.value)

        val f2 = FontVariation.parseOne("a1c2=0")

        assertEquals("a1c2", f2.tag)
        assertEquals(0f, f2.value)

        assertFailsWith<IllegalArgumentException> {
           FontVariation.parseOne("acdd=")
        }

        assertFailsWith<IllegalArgumentException> {
            FontVariation.parseOne("=123")
        }
    }
}
