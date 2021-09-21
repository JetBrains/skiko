package org.jetbrains.skiko

import org.junit.Test

internal class VersionTest {
    @Test
    fun `skiko version`() {
        assert(org.jetbrains.skiko.Version.skiko.isNotBlank())
    }

    @Test
    fun `skia version`() {
        assert( org.jetbrains.skiko.Version.skia.isNotBlank())
    }
}