package org.jetbrains.skia

import org.jetbrains.skiko.tests.TestGlContext
import kotlin.test.Test
import kotlin.test.assertEquals

class DirectContextNativeTest {

    @Test
    fun resourceCacheLimitTest() {
        if (!TestGlContext.isAvailabale()) return

        TestGlContext.run {
            DirectContext.makeGL().useContext { context ->
                context.resourceCacheLimit = 1024
                assertEquals(1024, context.resourceCacheLimit)
            }
        }
    }
}