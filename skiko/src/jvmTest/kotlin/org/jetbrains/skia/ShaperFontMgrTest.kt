package org.jetbrains.skia

import org.jetbrains.skiko.Library
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import kotlin.test.Test
import kotlin.test.assertTrue

class ShaperFontMgrTest {
    @Test
    fun linuxFallbackManagerIsReusedPerShaperAndReleasedWithItsOwner() {
        if (hostOs != OS.Linux) return
        Library.load()
        assertTrue(checkFontManagerOwnership())
    }
}

private external fun checkFontManagerOwnership(): Boolean
