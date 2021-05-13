package org.jetbrains.skiko

import org.junit.Test

internal class BaseTest {
    @Test
    fun loadLibrary() {
        Library.load()
    }
}