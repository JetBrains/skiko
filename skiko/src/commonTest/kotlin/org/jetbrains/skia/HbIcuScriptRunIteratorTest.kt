package org.jetbrains.skia

import org.jetbrains.skia.impl.use
import org.jetbrains.skia.shaper.HbIcuScriptRunIterator
import org.jetbrains.skia.shaper.ScriptRun
import kotlin.test.*

class HbIcuScriptRunIteratorTest {
    @Test
    fun returnsScriptChangesAtUtf16Boundaries() {
        HbIcuScriptRunIterator("ééЖЖ").use { iterator ->
            assertTrue(iterator.hasNext())
            val latin = iterator.next()
            assertEquals(ScriptRun(2, "Latn"), latin)
            assertEquals("Latn", latin.script)
            assertTrue(iterator.hasNext())
            val cyrillic = iterator.next()
            assertEquals(ScriptRun(4, "Cyrl"), cyrillic)
            assertEquals("Cyrl", cyrillic.script)
            assertFalse(iterator.hasNext())
        }
    }

    @Test
    fun emptyTextHasNoRuns() {
        HbIcuScriptRunIterator("").use { iterator ->
            assertFalse(iterator.hasNext())
        }
    }
}
