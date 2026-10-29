package org.jetbrains.skiko

import org.junit.Test

/**
 * Verifies that the native Skiko library is correctly loaded in various scenarios.
 */
class LibraryLoadedTest {
    @Test
    fun getSystemTheme() {
        currentSystemTheme
    }
}