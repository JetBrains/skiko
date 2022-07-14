package org.jetbrains.skia

import org.jetbrains.skiko.tests.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GraphicsTest {

    @Test
    fun canSetAndGetProperties() = runTest {
        Graphics.fontCacheCountLimit = 7
        assertEquals(7, Graphics.fontCacheCountLimit)

        Graphics.fontCacheLimit = 42
        assertEquals(42, Graphics.fontCacheLimit)

        Graphics.resourceCacheSingleAllocationByteLimit = 123
        assertEquals(123, Graphics.resourceCacheSingleAllocationByteLimit)

        Graphics.resourceCacheTotalLimit = 321
        assertEquals(321, Graphics.resourceCacheTotalLimit)
    }

    @Test
    fun canPurgeFontCache() = runTest {
        Graphics.purgeFontCache()

        assertEquals(0, Graphics.fontCacheCountUsed)
        assertEquals(0, Graphics.fontCacheUsed)
    }

    @Test
    fun canPurgeResourceCache() = runTest {
        Graphics.purgeResourceCache()

        assertEquals(0, Graphics.resourceCacheTotalUsed)
    }

    @Test
    fun canPurgeAllCaches() = runTest {
        Graphics.purgeAllCaches()

        assertEquals(0, Graphics.fontCacheCountUsed)
        assertEquals(0, Graphics.fontCacheUsed)
        assertEquals(0, Graphics.resourceCacheTotalUsed)
    }

}
