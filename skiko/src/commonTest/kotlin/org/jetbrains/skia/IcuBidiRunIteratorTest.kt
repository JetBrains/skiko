package org.jetbrains.skia

import org.jetbrains.skia.impl.use
import org.jetbrains.skia.shaper.BidiRun
import org.jetbrains.skia.shaper.IcuBidiRunIterator
import kotlin.test.*

class IcuBidiRunIteratorTest {
    @Test
    fun returnsDirectionChangesAtUtf16Boundaries() {
        IcuBidiRunIterator("abאב", 0).use { iterator ->
            assertTrue(iterator.hasNext())
            assertEquals(BidiRun(2, 0), iterator.next())
            assertTrue(iterator.hasNext())
            assertEquals(BidiRun(4, 1), iterator.next())
            assertFalse(iterator.hasNext())
        }
    }

    @Test
    fun emptyTextHasNoRuns() {
        IcuBidiRunIterator("", 0).use { iterator ->
            assertFalse(iterator.hasNext())
        }
    }
}
